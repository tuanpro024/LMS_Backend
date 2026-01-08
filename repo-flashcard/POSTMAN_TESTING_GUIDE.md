# Hướng dẫn Test API Flashcard bằng Postman

## Bước 1: Chuẩn bị

### 1.1. Cài đặt Postman
- Download tại: https://www.postman.com/downloads/
- Hoặc sử dụng Postman Web

### 1.2. Khởi động các services
```bash
# Chạy MySQL
docker-compose up mysql

# Chạy Identity API (để login)
cd BE_LMS
java -jar repo-identity-api/target/repo-identity-api-1.0.0.jar

# Chạy Flashcard API
java -jar repo-flashcard/target/repo-flashcard-1.0.0.jar
```

Hoặc dùng Docker:
```bash
docker-compose up identity-api flashcard-api
```

## Bước 2: Login để lấy Access Token

### 2.1. Tạo request LOGIN
1. Mở Postman
2. Tạo request mới
3. Cấu hình:
   - **Method**: `POST`
   - **URL**: `http://localhost:8081/api/auth/login`
   - **Headers**:
     ```
     Content-Type: application/json
     ```
   - **Body** (chọn raw → JSON):
     ```json
     {
       "email": "user@example.com",
       "password": "password123"
     }
     ```

4. Click **Send**

### 2.2. Lấy Access Token
Response sẽ có dạng:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwMUhRUlM4RzlWM...",
    "refreshToken": "...",
    "user": {
      "id": "01HQRS8G9V3XKZM2N4P6Q7",
      "email": "user@example.com",
      "fullName": "Test User"
    }
  }
}
```

**Copy giá trị `accessToken`** để dùng cho các request tiếp theo.

## Bước 3: Test Study Set APIs

### 3.1. Tạo Study Set mới (POST)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `POST`
   - **URL**: `http://localhost:8082/study-sets`
   - **Headers**:
     ```
     Content-Type: application/json
     Authorization: Bearer eyJhbGciOiJSUzI1NiJ9... (paste access token vào đây)
     ```
   - **Body** (raw → JSON):
     ```json
     {
       "title": "English Vocabulary - Lesson 1",
       "description": "Basic English words for beginners",
       "isPrivate": false,
       "cards": [
         {
           "term": "Hello",
           "definition": "A greeting or expression of goodwill",
           "cardIndex": 0,
           "imageUrl": null
         },
         {
           "term": "Goodbye",
           "definition": "A farewell expression",
           "cardIndex": 1,
           "imageUrl": null
         },
         {
           "term": "Thank you",
           "definition": "An expression of gratitude",
           "cardIndex": 2,
           "imageUrl": null
         }
       ]
     }
     ```

3. Click **Send**

**Expected Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": "01HQRS8G9V3XKZM2N4P6Q8",
    "title": "English Vocabulary - Lesson 1",
    "description": "Basic English words for beginners",
    "isPrivate": false,
    "userId": "01HQRS8G9V3XKZM2N4P6Q7",
    "cards": [
      {
        "id": "01HQRS8G9V3XKZM2N4P6Q9",
        "term": "Hello",
        "definition": "A greeting or expression of goodwill",
        "cardIndex": 0,
        "imageUrl": null,
        "createdAt": "2024-01-08T09:00:00Z",
        "updatedAt": "2024-01-08T09:00:00Z"
      },
      // ... other cards
    ],
    "createdAt": "2024-01-08T09:00:00Z",
    "updatedAt": "2024-01-08T09:00:00Z"
  }
}
```

**Lưu ý**: Copy `id` của study set vừa tạo để dùng cho các test tiếp theo.

### 3.2. Lấy danh sách Study Sets công khai (GET)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `GET`
   - **URL**: `http://localhost:8082/study-sets`
   - **Headers**: Không cần (public endpoint)

3. Click **Send**

### 3.3. Lấy Study Set theo ID (GET)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `GET`
   - **URL**: `http://localhost:8082/study-sets/01HQRS8G9V3XKZM2N4P6Q8` (thay ID thật)
   - **Headers**: Không cần nếu study set là public

3. Click **Send**

### 3.4. Tìm kiếm Study Sets (GET)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `GET`
   - **URL**: `http://localhost:8082/study-sets?q=English`
   - **Headers**: Không cần

3. Click **Send**

### 3.5. Cập nhật Study Set (PUT)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `PUT`
   - **URL**: `http://localhost:8082/study-sets/01HQRS8G9V3XKZM2N4P6Q8`
   - **Headers**:
     ```
     Content-Type: application/json
     Authorization: Bearer {access_token}
     ```
   - **Body** (raw → JSON):
     ```json
     {
       "title": "English Vocabulary - Lesson 1 (Updated)",
       "description": "Updated description",
       "isPrivate": true,
       "cards": [
         {
           "term": "Hello",
           "definition": "Updated definition",
           "cardIndex": 0
         }
       ]
     }
     ```

3. Click **Send**

### 3.6. Xóa Study Set (DELETE)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `DELETE`
   - **URL**: `http://localhost:8082/study-sets/01HQRS8G9V3XKZM2N4P6Q8`
   - **Headers**:
     ```
     Authorization: Bearer {access_token}
     ```

3. Click **Send**

## Bước 4: Test Folder APIs

### 4.1. Tạo Folder (POST)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `POST`
   - **URL**: `http://localhost:8082/folders`
   - **Headers**:
     ```
     Content-Type: application/json
     Authorization: Bearer {access_token}
     ```
   - **Body** (raw → JSON):
     ```json
     {
       "name": "My English Study Materials",
       "description": "Collection of English vocabulary sets",
       "color": "#0080ff",
       "isPrivate": false,
       "parentFolderId": null
     }
     ```

3. Click **Send**

**Lưu ý**: Copy `id` của folder vừa tạo.

### 4.2. Lấy danh sách Folders (GET)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `GET`
   - **URL**: `http://localhost:8082/folders`
   - **Headers**: Không cần

3. Click **Send**

### 4.3. Thêm Study Set vào Folder (POST)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `POST`
   - **URL**: `http://localhost:8082/folders/{folderId}/study-sets/{studySetId}`
     - Thay `{folderId}` bằng ID folder thật
     - Thay `{studySetId}` bằng ID study set thật
   - **Headers**:
     ```
     Authorization: Bearer {access_token}
     ```

3. Click **Send**

### 4.4. Xóa Study Set khỏi Folder (DELETE)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `DELETE`
   - **URL**: `http://localhost:8082/folders/{folderId}/study-sets/{studySetId}`
   - **Headers**:
     ```
     Authorization: Bearer {access_token}
     ```

3. Click **Send**

### 4.5. Xóa Folder (DELETE)

1. Tạo request mới
2. Cấu hình:
   - **Method**: `DELETE`
   - **URL**: `http://localhost:8082/folders/{folderId}`
   - **Headers**:
     ```
     Authorization: Bearer {access_token}
     ```

3. Click **Send**

## Bước 5: Test Scenarios (Kịch bản test)

### Scenario 1: Tạo và quản lý Study Set hoàn chỉnh

1. **Login** → Lấy access token
2. **Tạo Study Set** (POST /study-sets) → Lưu study set ID
3. **Xem Study Set** (GET /study-sets/{id})
4. **Cập nhật Study Set** (PUT /study-sets/{id})
5. **Tìm kiếm Study Set** (GET /study-sets?q=...)
6. **Xóa Study Set** (DELETE /study-sets/{id})

### Scenario 2: Tạo Folder và organize Study Sets

1. **Login** → Lấy access token
2. **Tạo Study Set 1** → Lưu ID1
3. **Tạo Study Set 2** → Lưu ID2
4. **Tạo Folder** → Lưu folder ID
5. **Thêm Study Set 1 vào Folder** (POST /folders/{folderId}/study-sets/{ID1})
6. **Thêm Study Set 2 vào Folder** (POST /folders/{folderId}/study-sets/{ID2})
7. **Xem Folder** (GET /folders/{folderId}) → Kiểm tra studySetIds có 2 IDs
8. **Xóa Study Set 1 khỏi Folder**
9. **Xem Folder** lại → Kiểm tra chỉ còn ID2

### Scenario 3: Test Permission (quyền truy cập)

1. **User A Login** → Token A
2. **User A tạo Private Study Set** → Lưu ID
3. **User B Login** → Token B
4. **User B cố GET Private Study Set** với Token B → Expected: 403 Forbidden
5. **User A GET Study Set** với Token A → Expected: 200 OK

## Bước 6: Tips & Tricks trong Postman

### 6.1. Tạo Environment Variables

1. Click vào biểu tượng **⚙️ (Settings)** → **Environments**
2. Tạo environment mới, ví dụ: "Flashcard Local"
3. Thêm variables:
   ```
   base_url = http://localhost:8082
   identity_url = http://localhost:8081
   access_token = (để trống, sẽ set sau khi login)
   ```

4. Sử dụng trong requests:
   - URL: `{{base_url}}/study-sets`
   - Header: `Bearer {{access_token}}`

### 6.2. Tự động lưu Access Token

1. Trong request **Login**, chuyển sang tab **Tests**
2. Thêm script:
   ```javascript
   var jsonData = pm.response.json();
   if (jsonData.success && jsonData.data.accessToken) {
       pm.environment.set("access_token", jsonData.data.accessToken);
   }
   ```

3. Sau khi login, access token sẽ tự động được lưu vào environment variable

### 6.3. Tạo Collection

1. Click **New** → **Collection**
2. Đặt tên: "Flashcard API"
3. Thêm folder: "Study Sets", "Folders", "Authentication"
4. Kéo thả các requests vào folder tương ứng
5. Export collection để chia sẻ hoặc backup

## Bước 7: Test Cases cần kiểm tra

### ✅ Study Set APIs
- [ ] Tạo study set thành công
- [ ] Tạo study set với dữ liệu không hợp lệ (title trống) → 400
- [ ] Tạo study set không có token → 401
- [ ] Lấy danh sách public study sets
- [ ] Tìm kiếm study sets theo từ khóa
- [ ] Cập nhật study set của người khác → 403
- [ ] Xóa study set của người khác → 403

### ✅ Folder APIs
- [ ] Tạo folder thành công
- [ ] Tạo subfolder (parentFolderId không null)
- [ ] Thêm study set vào folder
- [ ] Thêm study set không tồn tại → 404
- [ ] Xóa folder có subfolders
- [ ] Lấy root folders

## Troubleshooting

### Lỗi thường gặp:

1. **401 Unauthorized**
   - Kiểm tra access token có đúng không
   - Token có thể đã hết hạn (mặc định 5 phút)
   - Login lại để lấy token mới

2. **403 Forbidden**
   - Bạn không có quyền truy cập resource
   - Kiểm tra userId trong token có khớp với owner không

3. **404 Not Found**
   - ID không tồn tại
   - Kiểm tra lại ID đã copy đúng chưa

4. **500 Internal Server Error**
   - Kiểm tra logs của service
   - Database có chạy không
   - Kiểm tra lại request body format

5. **Connection Refused**
   - Service chưa chạy
   - Port bị change (8082 cho flashcard, 8081 cho identity)
   - Firewall block
