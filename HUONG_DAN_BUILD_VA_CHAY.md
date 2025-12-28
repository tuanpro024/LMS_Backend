# Hướng Dẫn Build và Chạy Project LMS Backend

## 📋 Mục Lục
1. [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
2. [Cài Đặt Dependencies](#cài-đặt-dependencies)
3. [Cấu Hình Database và Services](#cấu-hình-database-và-services)
4. [Cấu Hình JWT Keys](#cấu-hình-jwt-keys)
5. [Build Project](#build-project)
6. [Chạy Project](#chạy-project)
7. [Kiểm Tra](#kiểm-tra)
8. [Troubleshooting](#troubleshooting)

---

## 🖥️ Yêu Cầu Hệ Thống

### Phần Mềm Cần Thiết:
- **Java 21** (JDK 21)
- **Maven 3.6+**
- **MongoDB 4.4+** (đã thay thế MySQL)
- **Redis 6.0+**
- **Apache Kafka 2.8+**
- **Git** (để clone project)

### Kiểm Tra Phiên Bản:
```bash
java -version    # Phải là Java 21
mvn -version     # Phải là Maven 3.6+
mongod --version # MongoDB
redis-cli --version # Redis
```

---

## 📦 Cài Đặt Dependencies

### 1. Cài Đặt Java 21

**Windows:**
- Tải JDK 21 từ [Oracle](https://www.oracle.com/java/technologies/downloads/#java21) hoặc [OpenJDK](https://adoptium.net/)
- Cài đặt và thiết lập biến môi trường `JAVA_HOME`

**Kiểm tra:**
```bash
java -version
```

### 2. Cài Đặt Maven

**Windows:**
- Tải Maven từ [Apache Maven](https://maven.apache.org/download.cgi)
- Giải nén và thêm vào PATH
- Hoặc sử dụng Maven Wrapper (nếu có)

**Kiểm tra:**
```bash
mvn -version
```

### 3. Cài Đặt MongoDB

**Windows:**
- Tải MongoDB Community Server từ [MongoDB Download](https://www.mongodb.com/try/download/community)
- Cài đặt và chạy MongoDB service

**Hoặc sử dụng Docker:**
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

**Kiểm tra:**
```bash
mongosh
# Hoặc
mongo
```

### 4. Cài Đặt Redis

**Windows:**
- Tải Redis từ [Redis for Windows](https://github.com/microsoftarchive/redis/releases)
- Hoặc sử dụng WSL2 với Redis
- Hoặc sử dụng Docker:

```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

**Kiểm tra:**
```bash
docker exec -it redis redis-cli
ping
# Kết quả: PONG
```

### 5. Cài Đặt Apache Kafka

**Windows:**
- Tải Kafka từ [Apache Kafka](https://kafka.apache.org/downloads)
- Giải nén và chạy Zookeeper và Kafka Server

**Hoặc sử dụng Docker Compose:**
```yaml
# docker-compose.yml
services:
   kafka:
      image: apache/kafka:3.7.1
      container_name: kafka
      ports:
         - "9092:9092"
      environment:
         - KAFKA_NODE_ID=1
         - KAFKA_PROCESS_ROLES=broker,controller
         - KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093
         - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
         - KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER
         - KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
         - KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093
         - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
         - KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1
         - KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1
         - KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0localhost:9092
```

```bash
docker-compose up -d
```

---

## ⚙️ Cấu Hình Database và Services

### 1. Cấu Hình MongoDB

MongoDB sẽ tự động tạo database khi ứng dụng chạy lần đầu. Database mặc định: `lms_identity_db`

**Tạo database và user (tùy chọn):**
```javascript
// Kết nối MongoDB
mongosh

// Tạo database
use lms_identity_db

// Tạo user (nếu cần authentication)
db.createUser({
  user: "mongodb_user",
  pwd: "mongodb_password",
  roles: [{ role: "readWrite", db: "lms_identity_db" }]
})
```

**Cập nhật `application.yml` nếu dùng authentication:**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://mongodb_user:mongodb_password@localhost:27017/lms_identity_db?authSource=admin
```

### 2. Cấu Hình Redis

Redis mặc định không có password. Nếu bạn đặt password, cập nhật trong `application.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: redis_password  # Bỏ trống nếu không có password
```

**Đặt password cho Redis (tùy chọn):**
1. Mở file `redis.conf`
2. Tìm dòng `# requirepass foobared`
3. Bỏ comment và đổi thành: `requirepass redis_password`
4. Khởi động lại Redis

### 3. Cấu Hình Kafka

Đảm bảo Kafka đang chạy trên port 9092. Kiểm tra:
```bash
# Windows (PowerShell)
netstat -an | findstr 9092
```

---

## 🔐 Cấu Hình JWT Keys

### Tạo JWT Keys

Project cần 2 file PEM cho JWT:
- `secrets/jwt-private.pem` - Private key
- `secrets/jwt-public.pem` - Public key

**Tạo keys bằng OpenSSL:**

```bash
# Tạo thư mục secrets (nếu chưa có)
mkdir secrets

# Tạo private key
openssl genpkey -algorithm RSA -out secrets/jwt-private.pem -pkeyopt rsa_keygen_bits:2048

# Tạo public key từ private key
openssl rsa -pubout -in secrets/jwt-private.pem -out secrets/jwt-public.pem
```

**Hoặc sử dụng PowerShell (Windows):**
```powershell
# Tạo private key
openssl genpkey -algorithm RSA -out secrets/jwt-private.pem -pkeyopt rsa_keygen_bits:2048

# Tạo public key
openssl rsa -pubout -in secrets/jwt-private.pem -out secrets/jwt-public.pem
```

**Kiểm tra keys:**
```bash
# Xem private key
cat secrets/jwt-private.pem

# Xem public key
cat secrets/jwt-public.pem
```

---

## 🔨 Build Project

### Cách 1: Build Từng Module (Khuyến Nghị)

Project này là multi-module Maven. Build theo thứ tự:

```bash
# 1. Build common-parent (parent POM)
cd repo-common-parent
mvn clean install

# 2. Build common-framework
cd ../repo-common-framework
mvn clean install

# 3. Build eureka-server
cd ../repo-eureka
mvn clean install

# 4. Build identity-api
cd ../repo-identity-api
mvn clean install

# 5. Build api-gateway
cd ../repo-api-gateway
mvn clean install

# 6. Build realtime
cd ../repo-realtime
mvn clean install
```

### Cách 2: Build Tất Cả (Nếu có root POM)

Nếu có file `pom.xml` ở root:
```bash
mvn clean install
```

### Kiểm Tra Build Thành Công

Sau khi build, các file JAR sẽ được tạo trong thư mục `target/` của mỗi module:
- `repo-eureka/target/repo-eureka-1.0.0.jar`
- `repo-identity-api/target/repo-identity-api-1.0.0.jar`
- `repo-api-gateway/target/repo-api-gateway-1.0.0.jar`
- `repo-realtime/target/repo-realtime-1.0.0.jar`

---

## 🚀 Chạy Project

### Thứ Tự Khởi Động Services:

**1. Khởi động MongoDB:**
```bash
# Windows Service (nếu đã cài đặt)
net start MongoDB

# Hoặc chạy trực tiếp
mongod

# Hoặc Docker
docker start mongodb
```

**2. Khởi động Redis:**
```bash
# Windows
redis-server

# Hoặc Docker
docker start redis
```

**3. Khởi động Kafka:**
```bash
# Chạy Zookeeper (terminal 1)
bin/zookeeper-server-start.sh config/zookeeper.properties

# Chạy Kafka Server (terminal 2)
bin/kafka-server-start.sh config/server.properties

# Hoặc Docker
docker-compose up -d
```

**4. Khởi động Eureka Server:**
```bash
cd repo-eureka
mvn spring-boot:run

# Hoặc chạy JAR
java -jar target/repo-eureka-1.0.0.jar
```

Kiểm tra Eureka Dashboard: http://localhost:8761

**5. Khởi động Identity API:**
```bash
cd repo-identity-api
mvn spring-boot:run

# Hoặc chạy JAR
java -jar target/repo-identity-api-1.0.0.jar
```

**6. Khởi động API Gateway:**
```bash
cd repo-api-gateway
mvn spring-boot:run

# Hoặc chạy JAR
java -jar target/repo-api-gateway-1.0.0.jar
```

**7. Khởi động Realtime Service:**
```bash
cd repo-realtime
mvn spring-boot:run

# Hoặc chạy JAR
java -jar target/repo-realtime-1.0.0.jar
```

### Chạy Tất Cả Bằng Script (Windows PowerShell)

Tạo file `start-all.ps1`:

```powershell
# start-all.ps1
Write-Host "Starting MongoDB..."
Start-Process mongod

Start-Sleep -Seconds 3

Write-Host "Starting Redis..."
Start-Process redis-server

Start-Sleep -Seconds 3

Write-Host "Starting Eureka..."
Start-Process java -ArgumentList "-jar", "repo-eureka/target/repo-eureka-1.0.0.jar" -WorkingDirectory $PSScriptRoot

Start-Sleep -Seconds 10

Write-Host "Starting Identity API..."
Start-Process java -ArgumentList "-jar", "repo-identity-api/target/repo-identity-api-1.0.0.jar" -WorkingDirectory $PSScriptRoot

Start-Sleep -Seconds 5

Write-Host "Starting API Gateway..."
Start-Process java -ArgumentList "-jar", "repo-api-gateway/target/repo-api-gateway-1.0.0.jar" -WorkingDirectory $PSScriptRoot

Start-Sleep -Seconds 5

Write-Host "Starting Realtime Service..."
Start-Process java -ArgumentList "-jar", "repo-realtime/target/repo-realtime-1.0.0.jar" -WorkingDirectory $PSScriptRoot

Write-Host "All services started!"
```

Chạy script:
```powershell
.\start-all.ps1
```

---

## ✅ Kiểm Tra

### 1. Kiểm Tra Services Đang Chạy

**Eureka Dashboard:**
- URL: http://localhost:8761
- Kiểm tra các services đã đăng ký

**Health Checks:**
- Identity API: http://localhost:8081/actuator/health
- API Gateway: http://localhost:8080/actuator/health

### 2. Kiểm Tra MongoDB

```bash
mongosh
use lms_identity_db
show collections
# Kết quả mong đợi: users, roles

db.users.find()
db.roles.find()
```

### 3. Test API

**Đăng ký user mới:**
```bash
curl -X POST http://localhost:8080/api/id/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

**Đăng nhập với admin:**
```bash
curl -X POST http://localhost:8080/api/id/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@local",
    "password": "admin123"
  }'
```

**Tài khoản admin mặc định:**
- Email: `admin@local`
- Password: `admin123`

### 4. Kiểm Tra Logs

Xem logs của từng service để đảm bảo không có lỗi:
- MongoDB connection
- Redis connection
- Kafka connection
- Service registration với Eureka

---

## 🔧 Troubleshooting

### Lỗi: "Cannot connect to MongoDB"

**Nguyên nhân:**
- MongoDB chưa khởi động
- Port 27017 bị chặn
- Cấu hình connection string sai

**Giải pháp:**
```bash
# Kiểm tra MongoDB đang chạy
netstat -an | findstr 27017

# Khởi động MongoDB
mongod

# Kiểm tra logs trong application
```

### Lỗi: "Cannot connect to Redis"

**Nguyên nhân:**
- Redis chưa khởi động
- Password sai

**Giải pháp:**
```bash
# Kiểm tra Redis
redis-cli ping

# Kiểm tra password trong application.yml
```

### Lỗi: "JWT keys not found"

**Nguyên nhân:**
- File keys chưa được tạo
- Đường dẫn sai

**Giải pháp:**
```bash
# Tạo lại keys
openssl genpkey -algorithm RSA -out secrets/jwt-private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in secrets/jwt-private.pem -out secrets/jwt-public.pem

# Kiểm tra đường dẫn trong application.yml
security:
  jwt:
    public-key-path: file:../secrets/jwt-public.pem
    private-key-path: file:../secrets/jwt-private.pem
```

### Lỗi: "Service not registered in Eureka"

**Nguyên nhân:**
- Eureka chưa khởi động
- Cấu hình Eureka client sai

**Giải pháp:**
1. Đảm bảo Eureka đang chạy trên port 8761
2. Kiểm tra `application.yml` của service:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

### Lỗi: "Port already in use"

**Nguyên nhân:**
- Port đã được sử dụng bởi service khác

**Giải pháp:**
```bash
# Tìm process đang dùng port
netstat -ano | findstr :8081

# Kill process (thay PID bằng process ID)
taskkill /PID <PID> /F

# Hoặc đổi port trong application.yml
server:
  port: 8082
```

### Lỗi Build: "Could not resolve dependencies"

**Nguyên nhân:**
- Module chưa được build
- Maven repository chưa sync

**Giải pháp:**
```bash
# Build lại từ đầu theo thứ tự
cd repo-common-parent && mvn clean install
cd ../repo-common-framework && mvn clean install
# ... tiếp tục với các module khác

# Hoặc update Maven
mvn clean install -U
```

---

## 📝 Lưu Ý Quan Trọng

1. **Thứ tự khởi động:** Eureka → Identity API → API Gateway → Realtime
2. **MongoDB:** Database sẽ tự động được tạo khi chạy lần đầu
3. **Dữ liệu mẫu:** Admin user sẽ được tạo tự động khi Identity API khởi động lần đầu
4. **JWT Keys:** Phải tạo keys trước khi chạy Identity API
5. **Ports mặc định:**
   - Eureka: 8761
   - Identity API: 8081
   - API Gateway: 8080
   - Realtime: 8084
   - MongoDB: 27017
   - Redis: 6379
   - Kafka: 9092

---

## 🎯 Tóm Tắt Các Bước Nhanh

1. ✅ Cài đặt Java 21, Maven, MongoDB, Redis, Kafka
2. ✅ Tạo JWT keys trong thư mục `secrets/`
3. ✅ Khởi động MongoDB, Redis, Kafka
4. ✅ Build project: `mvn clean install` (từng module)
5. ✅ Chạy Eureka: `java -jar repo-eureka/target/repo-eureka-1.0.0.jar`
6. ✅ Chạy Identity API: `java -jar repo-identity-api/target/repo-identity-api-1.0.0.jar`
7. ✅ Chạy API Gateway: `java -jar repo-api-gateway/target/repo-api-gateway-1.0.0.jar`
8. ✅ Chạy Realtime: `java -jar repo-realtime/target/repo-realtime-1.0.0.jar`
9. ✅ Kiểm tra: http://localhost:8761 (Eureka Dashboard)

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề, kiểm tra:
- Logs của từng service
- Cấu hình trong `application.yml`
- Kết nối network giữa các services
- Firewall/antivirus có chặn ports không

**Chúc bạn thành công! 🎉**

