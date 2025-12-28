# Tóm Tắt Thay Đổi: MySQL → MongoDB

## 📋 Tổng Quan

Project đã được chuyển đổi từ MySQL (JPA/Hibernate) sang MongoDB. Tài liệu này mô tả tất cả các thay đổi đã thực hiện.

---

## 🔄 Các Thay Đổi Chính

### 1. Dependencies (pom.xml)

**File: `repo-identity-api/pom.xml`**

**Trước:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

**Sau:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

**File: `repo-common-framework/pom.xml`**

**Thêm:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

**Xóa:**
```xml
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
</dependency>
```

---

### 2. BaseEntity

**File: `repo-common-framework/src/main/java/com/lms/common/jpa/BaseEntity.java`**

**Thay đổi:**
- Từ JPA annotations (`@MappedSuperclass`, `@Column`, `@PrePersist`, `@PreUpdate`)
- Sang MongoDB annotations (`@Id`, `@CreatedDate`, `@LastModifiedDate`)
- Loại bỏ `@PrePersist` và `@PreUpdate` (xử lý bằng event listener)

**Mới:**
- File `repo-common-framework/src/main/java/com/lms/common/mongodb/MongoEventListener.java`
- Tự động gọi `onCreate()` và `onUpdate()` khi save entity

---

### 3. Entities

#### User Entity

**File: `repo-identity-api/src/main/java/com/lms/identity/entity/User.java`**

**Thay đổi:**
- `@Entity` → `@Document(collection = "users")`
- `@Table(name = "user")` → `@Document(collection = "users")`
- `@Column` → Loại bỏ (MongoDB không cần)
- `@ManyToMany` + `@JoinTable` → `@DBRef` (MongoDB reference)
- `@Indexed(unique = true)` cho email

#### Role Entity

**File: `repo-identity-api/src/main/java/com/lms/identity/entity/Role.java`**

**Thay đổi:**
- `@Entity` → `@Document(collection = "roles")`
- `@Table(name = "role")` → `@Document(collection = "roles")`
- `@Column` → Loại bỏ
- `@Indexed(unique = true)` cho name

---

### 4. Repositories

#### UserRepository

**File: `repo-identity-api/src/main/java/com/lms/identity/repository/UserRepository.java`**

**Thay đổi:**
- `JpaRepository<User, String>` → `MongoRepository<User, String>`
- `JpaSpecificationExecutor<User>` → Loại bỏ
- Thêm các query methods cho MongoDB:
  - `findByEmailContainingIgnoreCaseAndStatus()`
  - `findByEmailContainingIgnoreCase()`
  - `findByStatus()`
  - `findByRolesName()`

#### RoleRepository

**File: `repo-identity-api/src/main/java/com/lms/identity/repository/RoleRepository.java`**

**Thay đổi:**
- `JpaRepository<Role, String>` → `MongoRepository<Role, String>`

---

### 5. Services

#### AdminServiceImpl

**File: `repo-identity-api/src/main/java/com/lms/identity/service/impl/AdminServiceImpl.java`**

**Thay đổi:**
- Loại bỏ `Specification<User>` (JPA)
- Thêm `MongoTemplate` để build queries động
- `buildSpec()` → `buildQuery()` sử dụng MongoDB Criteria
- `resolveRoles()` - Cập nhật để fetch roles từ database thay vì tạo mới

**Trước:**
```java
Specification<User> spec = buildSpec(filter);
Page<User> result = userRepository.findAll(spec, page);
```

**Sau:**
```java
Query query = buildQuery(filter);
long total = mongoTemplate.count(query, User.class);
query.with(pageRequest);
List<User> users = mongoTemplate.find(query, User.class);
Page<User> result = new PageImpl<>(users, pageRequest, total);
```

---

### 6. Configuration

#### application.yml

**File: `repo-identity-api/src/main/resources/application.yml`**

**Trước:**
```yaml
spring:
  sql:
    init:
      mode: always
      data-locations: classpath:init-data.sql
  datasource:
    url: jdbc:mysql://localhost:3306/id_mysql_database?...
    username: root
    password: mysql_root_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

**Sau:**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/lms_identity_db
```

---

### 7. Data Initialization

**Trước:**
- File SQL: `init-data.sql`
- Chạy tự động khi ứng dụng khởi động

**Sau:**
- File Java: `repo-identity-api/src/main/java/com/lms/identity/config/MongoDataInitializer.java`
- Implement `CommandLineRunner`
- Tự động tạo roles và admin user khi ứng dụng khởi động lần đầu

---

## 📊 So Sánh Cấu Trúc Dữ Liệu

### MySQL (Trước)

```sql
CREATE TABLE user (
    id VARCHAR(26) PRIMARY KEY,
    email VARCHAR(180) UNIQUE,
    password VARCHAR(100),
    ...
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted BOOLEAN
);

CREATE TABLE role (
    id VARCHAR(26) PRIMARY KEY,
    name VARCHAR(64) UNIQUE,
    ...
);

CREATE TABLE user_role (
    user_id VARCHAR(26),
    role_id VARCHAR(26),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);
```

### MongoDB (Sau)

```javascript
// Collection: users
{
  "_id": "01JFZC5Y3K1M7X9C6T2B4N8PS",
  "email": "admin@local",
  "password": "$2a$12$...",
  "fullName": "Admin",
  "status": "ACTIVE",
  "emailVerified": true,
  "roles": [
    {
      "$ref": "roles",
      "$id": "01JFZC5Y3K1M7X9C6T2B4N8PQ"
    }
  ],
  "createdAt": ISODate("2024-01-01T00:00:00Z"),
  "updatedAt": ISODate("2024-01-01T00:00:00Z"),
  "deleted": false
}

// Collection: roles
{
  "_id": "01JFZC5Y3K1M7X9C6T2B4N8PQ",
  "name": "ROLE_ADMIN",
  "createdAt": ISODate("2024-01-01T00:00:00Z"),
  "updatedAt": ISODate("2024-01-01T00:00:00Z"),
  "deleted": false
}
```

---

## ⚠️ Lưu Ý Quan Trọng

### 1. @DBRef vs Embedded

- Hiện tại sử dụng `@DBRef` cho roles (reference)
- Có thể chuyển sang embedded nếu cần performance tốt hơn
- `@DBRef` giúp tránh duplicate data nhưng cần query thêm

### 2. Transactions

- MongoDB hỗ trợ transactions từ version 4.0+
- Cần replica set để sử dụng transactions
- `@Transactional` vẫn hoạt động nhưng cần cấu hình đúng

### 3. Queries

- JPA Specification → MongoDB Criteria API
- Một số query phức tạp cần viết lại
- Sử dụng `MongoTemplate` cho queries động

### 4. Indexes

- MongoDB tự động tạo index cho `_id`
- Cần tạo index cho `email` (đã có `@Indexed(unique = true)`)
- Có thể tạo thêm indexes trong `MongoDataInitializer` nếu cần

### 5. Migration Dữ Liệu

- Nếu có dữ liệu MySQL cũ, cần script migration
- Export từ MySQL → Import vào MongoDB
- Hoặc viết script Java để migrate

---

## 🚀 Các Bước Tiếp Theo (Nếu Cần)

1. **Tạo Indexes:**
   ```java
   @PostConstruct
   public void createIndexes() {
       mongoTemplate.indexOps(User.class)
           .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
   }
   ```

2. **Tối Ưu Queries:**
   - Review các query phức tạp
   - Thêm indexes nếu cần
   - Sử dụng aggregation pipeline cho queries phức tạp

3. **Testing:**
   - Test tất cả API endpoints
   - Test pagination và filtering
   - Test transactions (nếu có)

4. **Monitoring:**
   - Monitor MongoDB performance
   - Check slow queries
   - Monitor connection pool

---

## ✅ Checklist Migration

- [x] Cập nhật dependencies
- [x] Chuyển đổi BaseEntity
- [x] Chuyển đổi User entity
- [x] Chuyển đổi Role entity
- [x] Cập nhật repositories
- [x] Cập nhật services (AdminServiceImpl)
- [x] Cập nhật application.yml
- [x] Tạo MongoDB data initializer
- [x] Tạo MongoDB event listener
- [x] Xóa/loại bỏ init-data.sql (không cần nữa)
- [x] Tạo hướng dẫn build và chạy

---

## 📝 Files Đã Thay Đổi

1. `repo-identity-api/pom.xml`
2. `repo-common-framework/pom.xml`
3. `repo-common-framework/src/main/java/com/lms/common/jpa/BaseEntity.java`
4. `repo-common-framework/src/main/java/com/lms/common/mongodb/MongoEventListener.java` (MỚI)
5. `repo-identity-api/src/main/java/com/lms/identity/entity/User.java`
6. `repo-identity-api/src/main/java/com/lms/identity/entity/Role.java`
7. `repo-identity-api/src/main/java/com/lms/identity/repository/UserRepository.java`
8. `repo-identity-api/src/main/java/com/lms/identity/repository/RoleRepository.java`
9. `repo-identity-api/src/main/java/com/lms/identity/service/impl/AdminServiceImpl.java`
10. `repo-identity-api/src/main/resources/application.yml`
11. `repo-identity-api/src/main/java/com/lms/identity/config/MongoDataInitializer.java` (MỚI)

---

**Migration hoàn tất! 🎉**

