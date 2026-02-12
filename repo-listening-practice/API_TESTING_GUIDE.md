# 🧪 Listening Practice API Testing Guide

## 📋 Hướng Dẫn Test Backend APIs

### 1️⃣ Chuẩn Bị

#### Start Services (IN ORDER!)
```bash
# 1. Start Eureka Server (port 8761) - Service Discovery
cd d:\DOANSP26\BACKEND\LMS_Backend\repo-eureka
mvn spring-boot:run

# 2. Start API Gateway (port 8080) - ALL requests go through here ⭐
cd d:\DOANSP26\BACKEND\LMS_Backend\repo-api-gateway
mvn spring-boot:run

# 3. Start Identity Service - FOR AUTHENTICATION
cd d:\DOANSP26\BACKEND\LMS_Backend\repo-identity
mvn spring-boot:run

# 4. Start Multimedia Service (for video metadata)
cd d:\DOANSP26\BACKEND\LMS_Backend\repo-multimedia
mvn spring-boot:run

# 5. Start Listening Practice Service
cd d:\DOANSP26\BACKEND\LMS_Backend\repo-listening-practice
mvn spring-boot:run

# ✅ ALL services register with Eureka
# ✅ Gateway routes requests to appropriate services
```

## 🎯 Quick Start

```bash
# 1. Start required services (IN ORDER!)
cd d:\DOANSP26\BACKEND\LMS_Backend\repo-eureka
mvn spring-boot:run  # Port 8761

cd d:\DOANSP26\BACKEND\LMS_Backend\repo-api-gateway
mvn spring-boot:run  # Port 8080 ⭐ ALL requests go here

cd d:\DOANSP26\BACKEND\LMS_Backend\repo-identity
mvn spring-boot:run  # Registers with Eureka

cd d:\DOANSP26\BACKEND\LMS_Backend\repo-multimedia  
mvn spring-boot:run  # Registers with Eureka

cd d:\DOANSP26\BACKEND\LMS_Backend\repo-listening-practice
mvn spring-boot:run  # Registers with Eureka

# 2. Import updated collection into Postman
# 3. Run "0. Authentication → Login (Get Token)"
# 4. Start testing APIs via Gateway (localhost:8080)!
```

**Base URL**: `http://localhost:8080` (API Gateway)  
**Auth**: `http://localhost:8080/api/auth/login`  
**Listening**: `http://localhost:8080/api/listening/**`

✅ Collection is ready for testing! 🚀API calls go through Gateway at `localhost:8080`!

#### Import Collection
1. Mở **Postman** hoặc **Thunder Client**
2. Import file: `listening-practice-api-tests.postman_collection.json`
3. Collection sẽ tự động lưu IDs (packageId, folderId, studySetId, videoCode)

---

## 🔄 Test Flow (Theo Thứ Tự)

### ✅ Step 0: Authentication (REQUIRED FIRST!)

#### 0.1. Start Identity Service
```bash
cd d:\DOANSP26\BACKEND\LMS_Backend\repo-identity
mvn spring-boot:run  # Port 8080
```

#### 0.2. Login to Get Token
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "teacher@lms.com",
  "password": "Teacher@123"
}
```
**Expected Response**: JWT token auto-saved to `{{authToken}}` variable

> **💡 TIP**: Tất cả CREATE/UPDATE/DELETE requests đều cần `Authorization: Bearer {{authToken}}` header!

---

### ✅ Step 1: Package Management

#### 1.1. Get Package Types
```
GET http://localhost:8089/api/packages/types
```
**Expected Response**: Danh sách các types:
- `FREE` - Tự do
- `LEARNING_PATH` - Ôn luyện
- `VIDEO_COURSE` - Video khóa học
- `LEARNING` - 1-1, 1-n

#### 1.2. Create Package
```
POST http://localhost:8089/api/packages
Content-Type: application/json

{
  "name": "Listening Practice Package",
  "description": "Package for listening practice content",
  "type": "LEARNING_PATH",
  "classId": null
}
```
**Expected Response**: Package created with ID → Tự động lưu vào `{{packageId}}`
**Note**: `type` phải là một trong 4 giá trị trên (không có `MULTIMEDIA`)

#### 1.3. Get Package By ID
```
GET http://localhost:8089/api/packages/{{packageId}}
```

---

### ✅ Step 2: Folder Management

#### 2.1. Create Folder
```
POST http://localhost:8089/api/folders
Content-Type: application/json

{
  "name": "Beginner Listening",
  "description": "Listening exercises for beginners",
  "packageId": "{{packageId}}"
}
```
**Expected Response**: Folder created → Tự động lưu `{{folderId}}`

#### 2.2. Add Folder to Package
```
POST http://localhost:8089/api/packages/{{packageId}}/folders/{{folderId}}
```

#### 2.3. Get Folders By Package
```
GET http://localhost:8089/api/folders?packageId={{packageId}}
```

---

### ✅ Step 3: StudySet Management

#### 3.1. Create StudySet
```
POST http://localhost:8089/api/study-sets
Content-Type: application/json

{
  "title": "Daily Conversations",
  "description": "Practice listening to daily conversations",
  "thumbnailUrl": null,
  "isPublic": true,
  "folderId": "{{folderId}}"
}
```
**Expected Response**: StudySet created → Tự động lưu `{{studySetId}}`

#### 3.2. Add StudySet to Folder
```
POST http://localhost:8089/api/folders/{{folderId}}/study-sets/{{studySetId}}
```

---

### ✅ Step 4: Video Selection (Core Feature)

#### 4.1. **Get Video Code from Multimedia Service**
Trước tiên, lấy videoCode từ multimedia service:
```
GET http://localhost:8087/api/videos
```
Copy một `code` từ response (ví dụ: `vid_abc123`)

#### 4.2. Add Video to StudySet
```
POST http://localhost:8089/api/listening/study-sets/{{studySetId}}/videos
Content-Type: application/json

{
  "videoCode": "vid_abc123",  # ← Thay bằng code thực tế
  "displayOrder": 1
}
```

**Expected Behavior**:
- ✅ Service gọi `http://localhost:8087/api/videos/vid_abc123`
- ✅ Fetch metadata (name, description, thumbnail, duration, playlistUrl)
- ✅ Cache vào table `listening_video_metadata`
- ✅ Return VideoMetadataResponse

**Expected Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "01JGXXX...",
    "studySetId": "01JGYYY...",
    "videoCode": "vid_abc123",
    "name": "Video name from multimedia",
    "description": "Video description",
    "thumbnailPath": "/thumbnails/...",
    "duration": 180,
    "displayOrder": 1,
    "playlistUrl": "/playlists/...",
    "createdAt": "2026-02-11T...",
    "updatedAt": "2026-02-11T..."
  }
}
```

#### 4.3. Get Videos in StudySet
```
GET http://localhost:8089/api/listening/study-sets/{{studySetId}}/videos
```
**Expected Response**: Array of videos, ordered by `displayOrder`

#### 4.4. Update Video Display Order
```
PUT http://localhost:8089/api/listening/study-sets/{{studySetId}}/videos/{{videoCode}}/display-order?displayOrder=2
```

#### 4.5. Refresh Video Metadata
```
POST http://localhost:8089/api/listening/study-sets/{{studySetId}}/videos/{{videoCode}}/refresh
```
**Use Case**: Nếu metadata trong multimedia service thay đổi, refresh để cập nhật cache

---

## 🧹 Cleanup (Optional)

Xóa theo thứ tự ngược lại:

```bash
# 1. Delete Video from StudySet
DELETE http://localhost:8089/api/listening/study-sets/{{studySetId}}/videos/{{videoCode}}

# 2. Delete StudySet
DELETE http://localhost:8089/api/study-sets/{{studySetId}}

# 3. Delete Folder
DELETE http://localhost:8089/api/folders/{{folderId}}

# 4. Delete Package
DELETE http://localhost:8089/api/packages/{{packageId}}
```

---

## ❌ Common Errors & Solutions

### Error 1: `Video not found in multimedia service`
**Cause**: `videoCode` không tồn tại trong repo-multimedia  
**Solution**: Check multimedia service trước: `GET http://localhost:8087/api/videos`

### Error 2: `Connection refused to localhost:8087`
**Cause**: Multimedia service chưa chạy  
**Solution**: Start multimedia service trước

### Error 3: `Video already exists in this study set`
**Cause**: Video đã được add vào StudySet rồi  
**Solution**: Skip hoặc dùng video khác

### Error 4: `Study set not found`
**Cause**: StudySet ID không tồn tại  
**Solution**: Verify `{{studySetId}}` variable hoặc create StudySet trước

---

## 🎯 Success Criteria

### ✅ Backend APIs Hoạt Động Tốt Khi:

1. **Package/Folder/StudySet CRUD**: Tạo, đọc, cập nhật, xóa thành công
2. **Video Selection**:
   - ✅ Add video → Fetch metadata từ multimedia thành công
   - ✅ Metadata được cache vào `listening_video_metadata` table
   - ✅ Get videos → Trả về list đúng thứ tự
   - ✅ Update display order → Thay đổi thành công
   - ✅ Refresh metadata → Cập nhật từ multimedia
   - ✅ Remove video → Soft delete (deleted=true)

3. **Integration**:
   - ✅ Service register với Eureka trên port 8089
   - ✅ RestTemplate gọi multimedia service thành công
   - ✅ Database `lms_listening_db` được tạo tự động

---

## 📊 Database Verification

### Check Table Creation
```sql
USE lms_listening_db;

-- Verify table exists
SHOW TABLES;
# Expected: listening_video_metadata

-- Check data
SELECT * FROM listening_video_metadata;

-- Check relationships
SELECT 
  lvm.id,
  lvm.study_set_id,
  lvm.video_code,
  lvm.name,
  lvm.display_order,
  lvm.created_at
FROM listening_video_metadata lvm
WHERE lvm.deleted = false
ORDER BY lvm.display_order;
```

---

## 🚀 Sau Khi Test Thành Công

Khi tất cả APIs test OK:
1. ✅ Backend đã sẵn sàng cho frontend integration
2. ✅ Có thể bắt đầu implement Admin UI (video selection modal)
3. ✅ Implement Student UI (read-only view)

---

## 📝 Notes

- **Base URL**: `http://localhost:8089`
- **Database**: `lms_listening_db` (auto-created)
- **Eureka**: Service registered as `repo-listening-practice`
- **Dependencies**: Cần multimedia service (port 8087) để fetch video metadata
