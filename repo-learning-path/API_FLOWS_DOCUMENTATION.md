# Tài Liệu Tổng Hợp Các Luồng API - Learning Path Service

## Tổng Quan Hệ Thống

Repo **learning-path** quản lý hệ thống học tập có cấu trúc theo dạng cây phân cấp:
```
Package (Gói học) 
  └─ Folder (Thư mục)
      └─ StudySet (Bộ học)
          └─ LearningPath (Lộ trình học)
              └─ Step (Bước học)
                  └─ StepModule (Module thực hành)
```

## Các Entity Chính

### 1. Package (Gói học)
- Đại diện cho một gói học lớn (VD: "Tiếng Trung HSK", "Tiếng Nhật JLPT")
- Chứa nhiều Folder

### 2. Folder (Thư mục)
- Nhóm các StudySet theo chủ đề (VD: "Giáo Trình Chuẩn HSK", "Giáo Trình Hán Ngữ")
- Thuộc về một Package
- Chứa nhiều StudySet

### 3. StudySet (Bộ học)
- Đại diện cho một bộ học cụ thể (VD: "HSK 1", "HSK 2")
- Thuộc về một Folder
- Chứa nhiều LearningPath

### 4. LearningPath (Lộ trình học)
- Đại diện cho một lộ trình học trong StudySet
- Chứa nhiều Step theo thứ tự
- Có thể track progress của user

### 5. Step (Bước học)
- Một bước cụ thể trong LearningPath (VD: "Bài 1: Chào hỏi", "Bài 2: Số đếm")
- Chứa nhiều StepModule (các module thực hành)
- Có thể có unlock rule (phải hoàn thành Step trước mới mở được)
- Có thứ tự (stepOrder)

### 6. StepModule (Module thực hành)
- Liên kết đến các module từ microservices khác (flashcard, writing, kanji-origin)
- Có thể bắt buộc (isRequired) hoặc tùy chọn
- Có thứ tự (moduleOrder)
- User phải hoàn thành để progress

---

## I. PACKAGE APIs

### 1.1. Tạo Package
**Endpoint:** `POST /packages`  
**Quyền:** Authenticated user  
**Request Body:**
```json
{
  "name": "Tiếng Trung HSK",
  "description": "Gói học Tiếng Trung chuẩn HSK",
  "type": "CHINESE"
}
```

**Luồng xử lý:**
1. Validate request (name, type bắt buộc)
2. Lấy userId từ Authentication
3. Gọi PackageApiDelegate.createPackage()
4. Tạo Package entity với:
   - name, description, type
   - createdBy = userId
   - isActive = true
   - createdAt, updatedAt = now
5. Lưu vào database
6. Trả về PackageResponse (201 Created)

---

### 1.2. Lấy Package theo ID
**Endpoint:** `GET /packages/{id}`  
**Quyền:** Public  

**Luồng xử lý:**
1. Tìm Package theo id
2. Nếu không tìm thấy → throw ResourceNotFoundException
3. Map entity sang PackageResponse
4. Trả về (200 OK)

---

### 1.3. Lấy tất cả Package
**Endpoint:** `GET /packages?type={type}`  
**Quyền:** Public  
**Query Params:**
- `type` (optional): Lọc theo loại (CHINESE, JAPANESE, etc.)

**Luồng xử lý:**
1. Nếu có type:
   - Tìm Package theo type và isActive=true
2. Nếu không có type:
   - Lấy tất cả Package có isActive=true
3. Map list entity sang list PackageResponse
4. Trả về (200 OK)

---

### 1.4. Cập nhật Package
**Endpoint:** `PUT /packages/{id}`  
**Quyền:** Authenticated user  
**Request Body:**
```json
{
  "name": "Tiếng Trung HSK (Updated)",
  "description": "Mô tả mới",
  "type": "CHINESE"
}
```

**Luồng xử lý:**
1. Validate request
2. Tìm Package theo id
3. Kiểm tra quyền (chỉ người tạo hoặc admin)
4. Cập nhật các trường có giá trị mới
5. Cập nhật updatedAt
6. Lưu vào database
7. Trả về PackageResponse (200 OK)

---

### 1.5. Xóa Package
**Endpoint:** `DELETE /packages/{id}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Tìm Package theo id
2. Kiểm tra quyền (chỉ người tạo hoặc admin)
3. Soft delete: set isActive = false
4. Cập nhật updatedAt
5. Lưu vào database
6. Trả về (200 OK)

---

### 1.6. Thêm Folder vào Package
**Endpoint:** `POST /packages/{packageId}/folders/{folderId}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Tìm Package theo packageId
2. Tìm Folder theo folderId
3. Kiểm tra quyền
4. Kiểm tra Folder chưa thuộc Package nào khác
5. Set folder.packageId = packageId
6. Lưu Folder
7. Trả về PackageResponse với danh sách folders (200 OK)

---

### 1.7. Xóa Folder khỏi Package
**Endpoint:** `DELETE /packages/{packageId}/folders/{folderId}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Tìm Package và Folder
2. Kiểm tra quyền
3. Kiểm tra Folder có thuộc Package này không
4. Set folder.packageId = null
5. Lưu Folder
6. Trả về PackageResponse (200 OK)

---

## II. FOLDER APIs

### 2.1. Tạo Folder
**Endpoint:** `POST /folders`  
**Quyền:** Authenticated user  
**Request Body:**
```json
{
  "name": "Giáo Trình Chuẩn HSK",
  "description": "Giáo trình HSK chính thức",
  "packageId": "package-id-123"
}
```

**Luồng xử lý:**
1. Validate request (name bắt buộc)
2. Lấy userId từ Authentication
3. Nếu có packageId:
   - Kiểm tra Package tồn tại
4. Gọi FolderApiDelegate.createFolder()
5. Tạo Folder entity với:
   - name, description, packageId
   - createdBy = userId
   - isActive = true
6. Lưu vào database
7. Trả về FolderResponse (201 Created)

---

### 2.2. Lấy Folder theo ID
**Endpoint:** `GET /folders/{id}`  
**Quyền:** Public  

**Luồng xử lý:**
1. Tìm Folder theo id
2. Load các StudySet liên quan (nếu cần)
3. Map sang FolderResponse
4. Trả về (200 OK)

---

### 2.3. Lấy Folder theo Package
**Endpoint:** `GET /folders/package/{packageId}`  
**Quyền:** Public  

**Luồng xử lý:**
1. Tìm tất cả Folder theo packageId và isActive=true
2. Sắp xếp theo createdAt
3. Map sang list FolderResponse
4. Trả về (200 OK)

---

### 2.4. Lấy Folder của user hiện tại
**Endpoint:** `GET /folders/my-folders`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Lấy userId từ Authentication
2. Tìm tất cả Folder có createdBy = userId và isActive=true
3. Map sang list FolderResponse
4. Trả về (200 OK)

---

### 2.5. Cập nhật Folder
**Endpoint:** `PUT /folders/{id}`  
**Quyền:** Authenticated user  
**Request Body:**
```json
{
  "name": "Giáo Trình HSK (Updated)",
  "description": "Mô tả mới"
}
```

**Luồng xử lý:**
1. Tìm Folder theo id
2. Kiểm tra quyền
3. Cập nhật name, description
4. Lưu vào database
5. Trả về FolderResponse (200 OK)

---

### 2.6. Xóa Folder
**Endpoint:** `DELETE /folders/{id}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Tìm Folder theo id
2. Kiểm tra quyền
3. Soft delete: set isActive = false
4. Lưu vào database
5. Trả về (200 OK)

---

### 2.7. Thêm StudySet vào Folder
**Endpoint:** `POST /folders/{folderId}/study-sets/{studySetId}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Tìm Folder và StudySet
2. Kiểm tra quyền
3. Kiểm tra StudySet chưa thuộc Folder nào khác
4. Set studySet.folderId = folderId
5. Lưu StudySet
6. Trả về FolderResponse (200 OK)

---

### 2.8. Xóa StudySet khỏi Folder
**Endpoint:** `DELETE /folders/{folderId}/study-sets/{studySetId}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Tìm Folder và StudySet
2. Kiểm tra quyền
3. Set studySet.folderId = null
4. Lưu StudySet
5. Trả về FolderResponse (200 OK)

---

## III. STUDY SET APIs

### 3.1. Tạo StudySet
**Endpoint:** `POST /study-sets`  
**Quyền:** Authenticated user  
**Request Body:**
```json
{
  "name": "HSK 1",
  "description": "Bộ học HSK cấp độ 1",
  "folderId": "folder-id-123",
  "imageUrl": "https://example.com/hsk1.jpg"
}
```

**Luồng xử lý:**
1. Validate request (name bắt buộc)
2. Lấy userId từ Authentication
3. Nếu có folderId:
   - Kiểm tra Folder tồn tại
4. Gọi StudySetApiDelegate.createStudySet()
5. Tạo StudySet entity
6. Lưu vào database
7. Trả về StudySetResponse (201 Created)

---

### 3.2. Lấy StudySet theo ID
**Endpoint:** `GET /study-sets/{id}`  
**Quyền:** Public  

**Luồng xử lý:**
1. Tìm StudySet theo id
2. Load các LearningPath liên quan (nếu cần)
3. Map sang StudySetResponse
4. Trả về (200 OK)

---

### 3.3. Lấy tất cả StudySet
**Endpoint:** `GET /study-sets?q={searchQuery}`  
**Quyền:** Public  
**Query Params:**
- `q` (optional): Từ khóa tìm kiếm

**Luồng xử lý:**
1. Nếu có query `q`:
   - Tìm kiếm StudySet theo name hoặc description (LIKE %q%)
2. Nếu không có `q`:
   - Lấy tất cả StudySet có isActive=true
3. Map sang list StudySetResponse
4. Trả về (200 OK)

---

### 3.4. Lấy StudySet theo Folder
**Endpoint:** `GET /study-sets/folder/{folderId}`  
**Quyền:** Public  

**Luồng xử lý:**
1. Tìm tất cả StudySet theo folderId và isActive=true
2. Sắp xếp theo createdAt
3. Map sang list StudySetResponse
4. Trả về (200 OK)

---

### 3.5. Lấy StudySet của user
**Endpoint:** `GET /study-sets/my-sets`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Lấy userId từ Authentication
2. Tìm tất cả StudySet có createdBy = userId và isActive=true
3. Map sang list StudySetResponse
4. Trả về (200 OK)

---

### 3.6. Cập nhật StudySet
**Endpoint:** `PUT /study-sets/{id}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Tìm StudySet theo id
2. Kiểm tra quyền
3. Cập nhật các trường
4. Lưu vào database
5. Trả về StudySetResponse (200 OK)

---

### 3.7. Xóa StudySet
**Endpoint:** `DELETE /study-sets/{id}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Tìm StudySet theo id
2. Kiểm tra quyền
3. Soft delete: set isActive = false
4. Lưu vào database
5. Trả về (200 OK)

---

## IV. LEARNING PATH APIs

### 4.1. Tạo Learning Path
**Endpoint:** `POST /learning-paths`  
**Quyền:** ADMIN hoặc TEACHER  
**Request Body:**
```json
{
  "studySetId": "studyset-id-123",
  "title": "Lộ trình tiêu chuẩn",
  "description": "Lộ trình học theo thứ tự bài học"
}
```

**Luồng xử lý:**
1. Validate request (studySetId, title bắt buộc)
2. Kiểm tra quyền (chỉ ADMIN/TEACHER)
3. Lấy userId từ Authentication
4. Kiểm tra StudySet tồn tại
5. Tạo LearningPath entity:
   - studySetId, title, description
   - createdBy = userId
   - isActive = true
6. Lưu vào database
7. Trả về LearningPathResponse (201 Created)

---

### 4.2. Lấy Learning Path theo ID
**Endpoint:** `GET /learning-paths/{id}`  
**Quyền:** Public (có thể authenticated để có progress)  

**Luồng xử lý:**

#### Trường hợp 1: User đã login
1. Lấy userId từ Authentication
2. Gọi getLearningPathWithProgress(id, userId)
3. Tìm LearningPath theo id
4. Tìm LearningPathProgress của user
5. Load tất cả Steps:
   - Tìm các Step theo learningPathId, sắp xếp theo stepOrder
   - Load StepProgress cho từng Step
   - Load unlock status (isLocked, lockReason)
6. Map sang LearningPathResponse với progress data
7. Trả về (200 OK)

#### Trường hợp 2: User chưa login
1. Gọi getLearningPathById(id)
2. Tìm LearningPath theo id
3. Load tất cả Steps (không có progress)
4. Map sang LearningPathResponse
5. Trả về (200 OK)

---

### 4.3. Lấy Learning Paths theo StudySet
**Endpoint:** `GET /learning-paths/study-set/{studySetId}`  
**Quyền:** Public (có thể authenticated để có progress)  

**Luồng xử lý:**

#### Trường hợp 1: User đã login
1. Lấy userId từ Authentication
2. Tìm tất cả LearningPath theo studySetId và isActive=true
3. Cho mỗi LearningPath:
   - Load LearningPathProgress của user
   - Load Steps với progress
4. Map sang list LearningPathResponse
5. Trả về (200 OK)

#### Trường hợp 2: User chưa login
1. Tìm tất cả LearningPath theo studySetId và isActive=true
2. Load Steps (không có progress)
3. Map sang list LearningPathResponse
4. Trả về (200 OK)

---

### 4.4. Cập nhật Learning Path
**Endpoint:** `PUT /learning-paths/{id}`  
**Quyền:** ADMIN hoặc TEACHER  
**Request Body:**
```json
{
  "title": "Lộ trình chuẩn (Updated)",
  "description": "Mô tả mới"
}
```

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Tìm LearningPath theo id
3. Kiểm tra người tạo hoặc là admin
4. Cập nhật title, description
5. Cập nhật updatedAt
6. Lưu vào database
7. Trả về LearningPathResponse (200 OK)

---

### 4.5. Xóa Learning Path
**Endpoint:** `DELETE /learning-paths/{id}`  
**Quyền:** ADMIN hoặc TEACHER  

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Tìm LearningPath theo id
3. Kiểm tra người tạo hoặc là admin
4. Soft delete: set isActive = false
5. Soft delete tất cả Steps liên quan
6. Lưu vào database
7. Trả về (200 OK)

---

## V. STEP APIs

### 5.1. Tạo Step
**Endpoint:** `POST /steps`  
**Quyền:** ADMIN hoặc TEACHER  
**Request Body:**
```json
{
  "learningPathId": "learning-path-id-123",
  "title": "Bài 1: Chào hỏi",
  "description": "Học cách chào hỏi cơ bản",
  "stepOrder": 1
}
```

**Luồng xử lý:**
1. Validate request (learningPathId, title, stepOrder bắt buộc)
2. Kiểm tra quyền (ADMIN/TEACHER)
3. Lấy userId từ Authentication
4. Kiểm tra LearningPath tồn tại
5. Kiểm tra stepOrder chưa bị trùng
6. Tạo Step entity:
   - learningPathId, title, description, stepOrder
   - createdBy = userId
   - isActive = true
7. Lưu vào database
8. Tự động tạo StepUnlockRule:
   - Nếu stepOrder > 1: requiredStepId = step trước đó
   - Nếu stepOrder = 1: không có requiredStepId (luôn unlock)
9. Trả về StepResponse (201 Created)

---

### 5.2. Lấy Step theo ID
**Endpoint:** `GET /steps/{id}`  
**Quyền:** Public (có thể authenticated để có progress)  

**Luồng xử lý:**

#### Trường hợp 1: User đã login
1. Lấy userId từ Authentication
2. Gọi getStepWithProgress(id, userId)
3. Tìm Step theo id
4. Tìm StepProgress của user
5. Load tất cả StepModules:
   - Sắp xếp theo moduleOrder
   - Load ModuleProgress cho từng module
6. Kiểm tra unlock status:
   - Gọi UnlockService.isStepUnlocked(userId, stepId)
   - Nếu locked: lấy lockReason
7. Map sang StepResponse với progress data
8. Trả về (200 OK)

#### Trường hợp 2: User chưa login
1. Tìm Step theo id
2. Load StepModules (không có progress)
3. Map sang StepResponse
4. Trả về (200 OK)

---

### 5.3. Lấy Steps theo Learning Path
**Endpoint:** `GET /steps/learning-path/{learningPathId}`  
**Quyền:** Public (có thể authenticated để có progress)  

**Luồng xử lý:**

#### Trường hợp 1: User đã login
1. Lấy userId từ Authentication
2. Tìm tất cả Step theo learningPathId, sắp xếp theo stepOrder
3. Cho mỗi Step:
   - Load StepProgress của user
   - Load StepModules với ModuleProgress
   - Kiểm tra unlock status
4. Map sang list StepResponse
5. Trả về (200 OK)

#### Trường hợp 2: User chưa login
1. Tìm tất cả Step theo learningPathId, sắp xếp theo stepOrder
2. Load StepModules (không có progress)
3. Map sang list StepResponse
4. Trả về (200 OK)

---

### 5.4. Cập nhật Step
**Endpoint:** `PUT /steps/{id}`  
**Quyền:** ADMIN hoặc TEACHER  
**Request Body:**
```json
{
  "title": "Bài 1: Chào hỏi (Updated)",
  "description": "Mô tả mới",
  "stepOrder": 1
}
```

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Tìm Step theo id
3. Kiểm tra người tạo hoặc admin
4. Nếu đổi stepOrder:
   - Kiểm tra không trùng
   - Cập nhật unlock rules liên quan
5. Cập nhật title, description, stepOrder
6. Lưu vào database
7. Trả về StepResponse (200 OK)

---

### 5.5. Xóa Step
**Endpoint:** `DELETE /steps/{id}`  
**Quyền:** ADMIN hoặc TEACHER  

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Tìm Step theo id
3. Kiểm tra người tạo hoặc admin
4. Soft delete: set isActive = false
5. Soft delete tất cả StepModules liên quan
6. Xóa hoặc cập nhật unlock rules liên quan
7. Lưu vào database
8. Trả về (200 OK)

---

### 5.6. Sắp xếp lại Steps
**Endpoint:** `PUT /steps/learning-path/{learningPathId}/reorder`  
**Quyền:** ADMIN hoặc TEACHER  
**Request Body:**
```json
{
  "itemOrders": [
    {"id": "step-1", "order": 1},
    {"id": "step-2", "order": 2},
    {"id": "step-3", "order": 3}
  ]
}
```

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Tìm LearningPath theo learningPathId
3. Validate tất cả step IDs thuộc LearningPath này
4. Cho mỗi item trong itemOrders:
   - Tìm Step theo id
   - Set stepOrder = order
   - Lưu Step
5. Cập nhật unlock rules theo thứ tự mới
6. Trả về (200 OK)

---

## VI. STEP MODULE APIs

### 6.1. Thêm Module vào Step
**Endpoint:** `POST /step-modules`  
**Quyền:** ADMIN hoặc TEACHER  
**Request Body:**
```json
{
  "stepId": "step-id-123",
  "moduleType": "FLASHCARD",
  "moduleRefId": "flashcard-set-id-456",
  "isRequired": true,
  "moduleOrder": 1
}
```

**Luồng xử lý:**
1. Validate request (stepId, moduleType, moduleRefId bắt buộc)
2. Kiểm tra quyền (ADMIN/TEACHER)
3. Lấy userId từ Authentication
4. Kiểm tra Step tồn tại
5. Validate moduleType (FLASHCARD, WRITING, KANJI_ORIGIN)
6. Verify module tồn tại trong microservice tương ứng:
   - Gọi API của repo-flashcard/writing/kanji để verify moduleRefId
7. Tạo StepModule entity:
   - stepId, moduleType, moduleRefId
   - isRequired (default = true)
   - moduleOrder (auto-increment nếu không có)
   - isActive = true
8. Lưu vào database
9. Map sang StepModuleResponse
10. Trả về (201 Created)

---

### 6.2. Lấy Module theo ID
**Endpoint:** `GET /step-modules/{id}`  
**Quyền:** Public  

**Luồng xử lý:**
1. Tìm StepModule theo id
2. Load thông tin module từ microservice tương ứng:
   - Nếu FLASHCARD: gọi repo-flashcard API
   - Nếu WRITING: gọi repo-writing API
   - Nếu KANJI_ORIGIN: gọi repo-kanji-origin API
3. Map sang StepModuleResponse với module details
4. Trả về (200 OK)

---

### 6.3. Lấy Modules theo Step
**Endpoint:** `GET /step-modules/step/{stepId}`  
**Quyền:** Public  

**Luồng xử lý:**
1. Tìm tất cả StepModule theo stepId và isActive=true
2. Sắp xếp theo moduleOrder
3. Cho mỗi module:
   - Load thông tin chi tiết từ microservice
4. Map sang list StepModuleResponse
5. Trả về (200 OK)

---

### 6.4. Xóa Module khỏi Step
**Endpoint:** `DELETE /step-modules/{id}`  
**Quyền:** ADMIN hoặc TEACHER  

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Tìm StepModule theo id
3. Kiểm tra quyền (người tạo Step hoặc admin)
4. Soft delete: set isActive = false
5. Lưu vào database
6. Trả về (200 OK)

---

### 6.5. Sắp xếp lại Modules
**Endpoint:** `PUT /step-modules/step/{stepId}/reorder`  
**Quyền:** ADMIN hoặc TEACHER  
**Request Body:**
```json
{
  "itemOrders": [
    {"id": "module-1", "order": 1},
    {"id": "module-2", "order": 2},
    {"id": "module-3", "order": 3}
  ]
}
```

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Tìm Step theo stepId
3. Validate tất cả module IDs thuộc Step này
4. Cho mỗi item:
   - Tìm StepModule theo id
   - Set moduleOrder = order
   - Lưu StepModule
5. Trả về (200 OK)

---

## VII. AVAILABLE MODULE APIs (Admin Only)

### 7.1. Lấy tất cả Available Modules
**Endpoint:** `GET /admin/available-modules?q={searchQuery}`  
**Quyền:** ADMIN hoặc TEACHER  
**Query Params:**
- `q` (optional): Từ khóa tìm kiếm

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Gọi API của tất cả microservices:
   - repo-flashcard: GET /api/flashcard/sets
   - repo-writing: GET /api/writing/sets
   - repo-kanji-origin: GET /api/kanji/sets
3. Nếu có query `q`:
   - Filter kết quả theo name hoặc description
4. Merge tất cả kết quả thành list AvailableModuleResponse
5. Mỗi response chứa:
   - id, name, description
   - moduleType (FLASHCARD, WRITING, KANJI_ORIGIN)
   - source (repo name)
6. Trả về (200 OK)

---

### 7.2. Lấy Available Flashcard Sets
**Endpoint:** `GET /admin/available-modules/flashcard?q={searchQuery}`  
**Quyền:** ADMIN hoặc TEACHER  

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Gọi repo-flashcard API: GET /api/flashcard/sets
3. Nếu có query `q`: filter theo name/description
4. Map sang list AvailableModuleResponse
5. Set moduleType = FLASHCARD
6. Trả về (200 OK)

---

### 7.3. Lấy Available Writing Sets
**Endpoint:** `GET /admin/available-modules/writing?q={searchQuery}`  
**Quyền:** ADMIN hoặc TEACHER  

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Gọi repo-writing API: GET /api/writing/sets
3. Nếu có query `q`: filter theo name/description
4. Map sang list AvailableModuleResponse
5. Set moduleType = WRITING
6. Trả về (200 OK)

---

### 7.4. Lấy Available Kanji Sets
**Endpoint:** `GET /admin/available-modules/kanji?q={searchQuery}`  
**Quyền:** ADMIN hoặc TEACHER  

**Luồng xử lý:**
1. Kiểm tra quyền (ADMIN/TEACHER)
2. Gọi repo-kanji-origin API: GET /api/kanji/sets
3. Nếu có query `q`: filter theo name/description
4. Map sang list AvailableModuleResponse
5. Set moduleType = KANJI_ORIGIN
6. Trả về (200 OK)

---

## VIII. USER PROGRESS APIs

### 8.1. Bắt đầu Module
**Endpoint:** `POST /user/progress/module/{moduleId}/start`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Lấy userId từ Authentication
2. Tìm StepModule theo moduleId
3. Kiểm tra Step có unlock không:
   - Gọi UnlockService.isStepUnlocked(userId, stepId)
   - Nếu locked → throw LockedException
4. Tìm hoặc tạo ModuleProgress:
   - Tìm theo userId và moduleId
   - Nếu chưa có: tạo mới với status = IN_PROGRESS
5. Set:
   - firstStartedAt = now (nếu lần đầu)
   - status = IN_PROGRESS
6. Lưu ModuleProgress
7. Cập nhật StepProgress:
   - Gọi updateStepProgress(userId, stepId)
   - Tính toán progress của Step
8. Trả về ModuleProgressDto (201 Created)

**ModuleProgressDto:**
```json
{
  "id": "progress-id-123",
  "userId": "user-id-456",
  "moduleId": "module-id-789",
  "status": "IN_PROGRESS",
  "score": null,
  "totalAttempts": 0,
  "firstStartedAt": "2026-02-01T10:00:00Z",
  "lastAttemptAt": null,
  "completedAt": null
}
```

---

### 8.2. Cập nhật Progress của Module
**Endpoint:** `POST /user/progress/module/{moduleId}/update`  
**Quyền:** Authenticated user  
**Request Body:**
```json
{
  "score": 75
}
```

**Luồng xử lý:**
1. Lấy userId từ Authentication
2. Tìm ModuleProgress theo userId và moduleId
3. Nếu không tìm thấy → throw ResourceNotFoundException
4. Cập nhật:
   - score = request.score (nếu có)
   - totalAttempts += 1
   - lastAttemptAt = now
5. Lưu ModuleProgress
6. Trả về ModuleProgressDto (200 OK)

---

### 8.3. Hoàn thành Module
**Endpoint:** `POST /user/progress/module/{moduleId}/complete`  
**Quyền:** Authenticated user  
**Request Body:**
```json
{
  "score": 95
}
```

**Luồng xử lý:**
1. Lấy userId từ Authentication
2. Tìm StepModule theo moduleId
3. Tìm ModuleProgress theo userId và moduleId
4. Nếu không tìm thấy → throw ResourceNotFoundException
5. Cập nhật ModuleProgress:
   - status = COMPLETED
   - score = request.score (nếu có)
   - totalAttempts += 1
   - completedAt = now
   - lastAttemptAt = now
6. Lưu ModuleProgress
7. Cập nhật StepProgress:
   - Gọi updateStepProgress(userId, stepId)
   - Đếm số module đã hoàn thành
   - Đếm số required module đã hoàn thành
   - Tính toán status của Step:
     * NOT_STARTED: chưa hoàn thành module nào
     * IN_PROGRESS: đã hoàn thành ít nhất 1 module nhưng chưa đủ required
     * COMPLETED: đã hoàn thành tất cả required modules
8. Cập nhật LearningPathProgress:
   - Gọi updateLearningPathProgress(userId, learningPathId)
   - Đếm số Step đã hoàn thành
   - Tính toán overall progress %
   - Cập nhật status của LearningPath
9. Gửi realtime notification:
   - Gọi RealtimeNotificationService.sendProgressUpdate()
   - Broadcast qua WebSocket về client
10. Trả về ModuleProgressDto (200 OK)

---

### 8.4. Lấy Progress của Step
**Endpoint:** `GET /user/progress/step/{stepId}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Lấy userId từ Authentication
2. Tìm StepProgress theo userId và stepId
3. Nếu không tìm thấy → trả về null hoặc empty response
4. Load tất cả ModuleProgress của Step:
   - Tìm các StepModule theo stepId
   - Load ModuleProgress cho từng module
5. Map sang StepProgressResponse:
   - stepId, status
   - completedModules, totalModules
   - requiredCompletedModules, totalRequiredModules
   - progressPercentage
   - isLocked, lockReason
   - modules: list ModuleProgressDto
6. Trả về (200 OK)

**StepProgressResponse:**
```json
{
  "stepId": "step-id-123",
  "status": "IN_PROGRESS",
  "completedModules": 2,
  "totalModules": 5,
  "requiredCompletedModules": 1,
  "totalRequiredModules": 3,
  "progressPercentage": 40.0,
  "isLocked": false,
  "lockReason": null,
  "modules": [...]
}
```

---

### 8.5. Lấy Progress của Learning Path
**Endpoint:** `GET /user/progress/learning-path/{learningPathId}`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Lấy userId từ Authentication
2. Tìm LearningPathProgress theo userId và learningPathId
3. Nếu không tìm thấy → trả về null hoặc empty
4. Load tất cả StepProgress:
   - Tìm các Step theo learningPathId
   - Load StepProgress cho từng Step
5. Map sang LearningPathProgressResponse:
   - learningPathId, status
   - completedSteps, totalSteps
   - progressPercentage
   - steps: list StepProgressResponse
6. Trả về (200 OK)

**LearningPathProgressResponse:**
```json
{
  "learningPathId": "lp-id-123",
  "status": "IN_PROGRESS",
  "completedSteps": 3,
  "totalSteps": 10,
  "progressPercentage": 30.0,
  "steps": [...]
}
```

---

### 8.6. Lấy Progress tất cả Learning Paths của StudySet
**Endpoint:** `GET /user/progress/study-set/{studySetId}/learning-paths`  
**Quyền:** Authenticated user  

**Luồng xử lý:**
1. Lấy userId từ Authentication
2. Tìm tất cả LearningPath theo studySetId và isActive=true
3. Sắp xếp theo createdAt
4. Cho mỗi LearningPath:
   - Tìm LearningPathProgress của user
   - Nếu có: map sang LearningPathProgressResponse
5. Filter chỉ giữ các progress khác null
6. Trả về list LearningPathProgressResponse (200 OK)

---

## IX. BUSINESS LOGIC - Chi Tiết Các Service Methods

### 9.1. UnlockService - Kiểm tra Unlock Step

#### isStepUnlocked(userId, stepId)
**Mục đích:** Kiểm tra Step có unlock cho user chưa

**Logic:**
1. Tìm Step theo stepId
2. Nếu stepOrder = 1:
   - Return true (Step đầu luôn unlock)
3. Tìm StepUnlockRule theo stepId và isActive=true
4. Nếu không có rule:
   - Return true (không có rule = unlock)
5. Nếu có rule:
   - Lấy requiredStepId từ rule
   - Nếu requiredStepId = null:
     * Return true
   - Nếu có requiredStepId:
     * Tìm StepProgress của user cho requiredStepId
     * Nếu status = COMPLETED:
       + Return true
     * Ngược lại:
       + Return false

---

#### getLockReason(userId, stepId)
**Mục đích:** Lấy lý do tại sao Step bị lock

**Logic:**
1. Nếu isStepUnlocked = true:
   - Return null
2. Tìm StepUnlockRule
3. Nếu có requiredStepId:
   - Tìm required Step
   - Return "Complete '{requiredStepTitle}' to unlock this step"
4. Ngược lại:
   - Return "Complete the previous step to unlock this step"

---

### 9.2. ProgressTrackingService - Tính toán Progress

#### updateStepProgress(userId, stepId)
**Mục đích:** Cập nhật progress của một Step dựa trên các Module đã hoàn thành

**Logic:**
1. Tìm Step theo stepId
2. Lấy tất cả StepModule của Step (isActive=true, sắp xếp theo moduleOrder)
3. Đếm:
   - totalModules = số lượng tất cả module
   - totalRequired = số lượng module có isRequired=true
4. Lấy tất cả ModuleProgress của user cho các module này
5. Filter các ModuleProgress có status = COMPLETED
6. Đếm:
   - completedCount = số module đã hoàn thành
   - completedRequiredCount = số required module đã hoàn thành
7. Tính status:
   - Nếu completedCount = 0:
     * status = NOT_STARTED
   - Nếu completedRequiredCount >= totalRequired:
     * status = COMPLETED
   - Ngược lại:
     * status = IN_PROGRESS
8. Tìm hoặc tạo StepProgress:
   - Tìm theo userId và stepId
   - Nếu chưa có: tạo mới với status = NOT_STARTED
9. Cập nhật StepProgress:
   - status (như đã tính ở trên)
   - completedModules = completedCount
   - totalModules = totalModules
   - requiredCompletedModules = completedRequiredCount
   - totalRequiredModules = totalRequired
   - firstStartedAt = now (nếu chưa set và có module đã start)
   - completedAt = now (nếu status = COMPLETED)
10. Lưu StepProgress
11. Return StepProgress

---

#### updateLearningPathProgress(userId, learningPathId)
**Mục đích:** Cập nhật progress của Learning Path dựa trên các Step đã hoàn thành

**Logic:**
1. Tìm LearningPath theo learningPathId
2. Lấy tất cả Step của LearningPath (isActive=true, sắp xếp theo stepOrder)
3. Đếm totalSteps = số lượng Step
4. Lấy tất cả StepProgress của user cho các Step này
5. Filter các StepProgress có status = COMPLETED
6. Đếm completedSteps = số Step đã hoàn thành
7. Tính status:
   - Nếu completedSteps = 0:
     * status = NOT_STARTED
   - Nếu completedSteps = totalSteps:
     * status = COMPLETED
   - Ngược lại:
     * status = IN_PROGRESS
8. Tính progressPercentage = (completedSteps / totalSteps) * 100
9. Tìm hoặc tạo LearningPathProgress:
   - Tìm theo userId và learningPathId
   - Nếu chưa có: tạo mới
10. Cập nhật LearningPathProgress:
    - status
    - completedSteps, totalSteps
    - progressPercentage
    - firstStartedAt = now (nếu chưa set và có step đã start)
    - completedAt = now (nếu status = COMPLETED)
11. Lưu LearningPathProgress
12. Return LearningPathProgress

---

### 9.3. RealtimeNotificationService - Gửi Notification

#### sendProgressUpdate(userId, progressData)
**Mục đích:** Gửi thông báo realtime về tiến độ học tập qua WebSocket

**Logic:**
1. Tạo notification message:
   - type = "PROGRESS_UPDATE"
   - userId
   - data: progressData (ModuleProgress, StepProgress, LearningPathProgress)
   - timestamp = now
2. Gọi repo-realtime API:
   - POST /api/realtime/notify
   - Body: notification message
3. repo-realtime sẽ broadcast qua WebSocket tới client của user
4. Client nhận và cập nhật UI realtime

---

## X. UNLOCK MECHANISM - Cơ chế Mở khóa

### 10.1. Quy tắc Unlock

**Step Unlock:**
- Step đầu tiên (stepOrder = 1) luôn unlock
- Các Step tiếp theo phải hoàn thành Step trước mới unlock
- StepUnlockRule lưu trữ quan hệ requiredStepId

**Ví dụ:**
```
Step 1 (stepOrder=1) → Luôn unlock
Step 2 (stepOrder=2) → Unlock khi hoàn thành Step 1
Step 3 (stepOrder=3) → Unlock khi hoàn thành Step 2
```

---

### 10.2. Khi nào Step được coi là "Completed"?

Step được coi là COMPLETED khi:
- Tất cả các module **required** (isRequired=true) đã hoàn thành
- Module optional (isRequired=false) không bắt buộc

**Ví dụ:**
```
Step 1 có 5 modules:
- Module 1: Flashcard (required) → COMPLETED
- Module 2: Writing (required) → COMPLETED
- Module 3: Kanji (required) → COMPLETED
- Module 4: Flashcard Extra (optional) → NOT_STARTED
- Module 5: Writing Extra (optional) → IN_PROGRESS

→ Step 1 status = COMPLETED (vì 3 required modules đã xong)
→ Step 2 unlock
```

---

### 10.3. Flow hoàn chỉnh khi User hoàn thành Module

```
1. User POST /user/progress/module/{moduleId}/complete
   ↓
2. Tìm ModuleProgress → set status = COMPLETED
   ↓
3. updateStepProgress(userId, stepId)
   - Đếm số module completed
   - Đếm số required module completed
   - Nếu đủ required → Step status = COMPLETED
   ↓
4. updateLearningPathProgress(userId, learningPathId)
   - Đếm số Step completed
   - Tính progressPercentage
   - Update LearningPath status
   ↓
5. Kiểm tra Step tiếp theo có unlock không
   - Nếu Step hiện tại = COMPLETED
   - Tìm Step tiếp theo (stepOrder + 1)
   - Unlock Step đó cho user
   ↓
6. sendProgressUpdate(userId, progressData)
   - Gửi notification realtime
   - Client nhận và update UI
   ↓
7. Return ModuleProgressDto
```

---

## XI. ERROR HANDLING

### 11.1. ResourceNotFoundException
**Khi nào throw:**
- Không tìm thấy entity theo ID
- Ví dụ: Package, Folder, StudySet, LearningPath, Step, StepModule không tồn tại

**Response:**
```json
{
  "success": false,
  "message": "Step module not found: module-id-123",
  "data": null
}
```

---

### 11.2. LockedException (Custom Exception)
**Khi nào throw:**
- User cố gắng start/complete module của Step đang bị lock
- Step chưa unlock vì chưa hoàn thành Step trước

**Response:**
```json
{
  "success": false,
  "message": "This step is locked. Complete 'Bài 1: Chào hỏi' to unlock this step",
  "data": null
}
```

---

### 11.3. UnauthorizedException
**Khi nào throw:**
- User không có quyền tạo/sửa/xóa (không phải ADMIN/TEACHER)
- User cố gắng sửa/xóa nội dung không phải của mình

**Response:**
```json
{
  "success": false,
  "message": "You don't have permission to perform this action",
  "data": null
}
```

---

### 11.4. ValidationException
**Khi nào throw:**
- Request body không hợp lệ (thiếu trường bắt buộc, format sai)
- stepOrder trùng
- moduleType không hợp lệ

**Response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "errors": [
      {"field": "title", "message": "Title is required"},
      {"field": "stepOrder", "message": "Step order already exists"}
    ]
  }
}
```

---

## XII. INTEGRATION VỚI MICROSERVICES KHÁC

### 12.1. repo-flashcard
**API calls:**
- GET /api/flashcard/sets → Lấy danh sách flashcard sets
- GET /api/flashcard/sets/{id} → Verify flashcard set tồn tại
- GET /api/flashcard/sets/{id}/details → Lấy chi tiết để hiển thị

**Khi nào gọi:**
- Admin browse available modules
- Thêm module vào Step
- Load chi tiết module khi user xem Step

---

### 12.2. repo-writing
**API calls:**
- GET /api/writing/sets → Lấy danh sách writing sets
- GET /api/writing/sets/{id} → Verify writing set tồn tại
- GET /api/writing/sets/{id}/details → Lấy chi tiết

**Khi nào gọi:**
- Admin browse available modules
- Thêm module vào Step
- Load chi tiết module

---

### 12.3. repo-kanji-origin
**API calls:**
- GET /api/kanji/sets → Lấy danh sách kanji sets
- GET /api/kanji/sets/{id} → Verify kanji set tồn tại
- GET /api/kanji/sets/{id}/details → Lấy chi tiết

**Khi nào gọi:**
- Admin browse available modules
- Thêm module vào Step
- Load chi tiết module

---

### 12.4. repo-realtime
**API calls:**
- POST /api/realtime/notify → Gửi notification realtime

**Payload:**
```json
{
  "type": "PROGRESS_UPDATE",
  "userId": "user-id-123",
  "data": {
    "moduleProgress": {...},
    "stepProgress": {...},
    "learningPathProgress": {...}
  },
  "timestamp": "2026-02-01T10:00:00Z"
}
```

**Khi nào gọi:**
- User hoàn thành module
- Progress thay đổi
- Step unlock

---

## XIII. DATABASE SCHEMA OVERVIEW

### Entities:
1. **Package**: id, name, description, type, createdBy, isActive, createdAt, updatedAt
2. **Folder**: id, name, description, packageId, createdBy, isActive, createdAt, updatedAt
3. **StudySet**: id, name, description, folderId, imageUrl, createdBy, isActive, createdAt, updatedAt
4. **LearningPath**: id, studySetId, title, description, createdBy, isActive, createdAt, updatedAt
5. **Step**: id, learningPathId, title, description, stepOrder, createdBy, isActive, createdAt, updatedAt
6. **StepModule**: id, stepId, moduleType, moduleRefId, isRequired, moduleOrder, isActive, createdAt
7. **StepUnlockRule**: id, stepId, requiredStepId, isActive, createdAt
8. **ModuleProgress**: id, userId, stepModuleId, stepId, status, score, totalAttempts, firstStartedAt, lastAttemptAt, completedAt
9. **StepProgress**: id, userId, stepId, learningPathId, status, completedModules, totalModules, requiredCompletedModules, totalRequiredModules, firstStartedAt, completedAt
10. **LearningPathProgress**: id, userId, learningPathId, status, completedSteps, totalSteps, progressPercentage, firstStartedAt, completedAt

---

## XIV. BEST PRACTICES & NOTES

### 14.1. Khi tạo Learning Path mới:
1. Tạo LearningPath
2. Tạo các Step theo thứ tự (stepOrder: 1, 2, 3, ...)
3. Hệ thống tự động tạo StepUnlockRule cho mỗi Step (trừ Step đầu)
4. Thêm StepModule vào mỗi Step
5. Set isRequired cho các module bắt buộc

### 14.2. Progress Tracking:
- Progress được tính tự động khi user complete module
- Không cần manual update StepProgress hay LearningPathProgress
- Service tự động cascade update từ Module → Step → LearningPath

### 14.3. Unlock Logic:
- Step đầu luôn unlock
- Các Step khác unlock tuần tự khi hoàn thành Step trước
- Client nên check isLocked trước khi cho user start module

### 14.4. Realtime Updates:
- Sử dụng WebSocket để push progress updates
- Client subscribe vào channel riêng của user
- Nhận notification ngay khi progress thay đổi

---

## XV. API SUMMARY TABLE

| Endpoint | Method | Quyền | Mục đích |
|----------|--------|-------|----------|
| `/packages` | POST | Auth | Tạo package |
| `/packages/{id}` | GET | Public | Lấy package |
| `/packages` | GET | Public | Lấy tất cả package |
| `/packages/{id}` | PUT | Auth | Cập nhật package |
| `/packages/{id}` | DELETE | Auth | Xóa package |
| `/packages/{packageId}/folders/{folderId}` | POST | Auth | Thêm folder vào package |
| `/packages/{packageId}/folders/{folderId}` | DELETE | Auth | Xóa folder khỏi package |
| `/folders` | POST | Auth | Tạo folder |
| `/folders/{id}` | GET | Public | Lấy folder |
| `/folders/package/{packageId}` | GET | Public | Lấy folder theo package |
| `/folders/my-folders` | GET | Auth | Lấy folder của user |
| `/folders/{id}` | PUT | Auth | Cập nhật folder |
| `/folders/{id}` | DELETE | Auth | Xóa folder |
| `/folders/{folderId}/study-sets/{studySetId}` | POST | Auth | Thêm studyset vào folder |
| `/folders/{folderId}/study-sets/{studySetId}` | DELETE | Auth | Xóa studyset khỏi folder |
| `/study-sets` | POST | Auth | Tạo studyset |
| `/study-sets/{id}` | GET | Public | Lấy studyset |
| `/study-sets` | GET | Public | Lấy tất cả studyset |
| `/study-sets/folder/{folderId}` | GET | Public | Lấy studyset theo folder |
| `/study-sets/my-sets` | GET | Auth | Lấy studyset của user |
| `/study-sets/{id}` | PUT | Auth | Cập nhật studyset |
| `/study-sets/{id}` | DELETE | Auth | Xóa studyset |
| `/learning-paths` | POST | ADMIN/TEACHER | Tạo learning path |
| `/learning-paths/{id}` | GET | Public | Lấy learning path |
| `/learning-paths/study-set/{studySetId}` | GET | Public | Lấy learning paths theo studyset |
| `/learning-paths/{id}` | PUT | ADMIN/TEACHER | Cập nhật learning path |
| `/learning-paths/{id}` | DELETE | ADMIN/TEACHER | Xóa learning path |
| `/steps` | POST | ADMIN/TEACHER | Tạo step |
| `/steps/{id}` | GET | Public | Lấy step |
| `/steps/learning-path/{learningPathId}` | GET | Public | Lấy steps theo learning path |
| `/steps/{id}` | PUT | ADMIN/TEACHER | Cập nhật step |
| `/steps/{id}` | DELETE | ADMIN/TEACHER | Xóa step |
| `/steps/learning-path/{learningPathId}/reorder` | PUT | ADMIN/TEACHER | Sắp xếp lại steps |
| `/step-modules` | POST | ADMIN/TEACHER | Thêm module vào step |
| `/step-modules/{id}` | GET | Public | Lấy module |
| `/step-modules/step/{stepId}` | GET | Public | Lấy modules theo step |
| `/step-modules/{id}` | DELETE | ADMIN/TEACHER | Xóa module |
| `/step-modules/step/{stepId}/reorder` | PUT | ADMIN/TEACHER | Sắp xếp lại modules |
| `/admin/available-modules` | GET | ADMIN/TEACHER | Lấy tất cả available modules |
| `/admin/available-modules/flashcard` | GET | ADMIN/TEACHER | Lấy flashcard sets |
| `/admin/available-modules/writing` | GET | ADMIN/TEACHER | Lấy writing sets |
| `/admin/available-modules/kanji` | GET | ADMIN/TEACHER | Lấy kanji sets |
| `/user/progress/module/{moduleId}/start` | POST | Auth | Bắt đầu module |
| `/user/progress/module/{moduleId}/update` | POST | Auth | Cập nhật progress |
| `/user/progress/module/{moduleId}/complete` | POST | Auth | Hoàn thành module |
| `/user/progress/step/{stepId}` | GET | Auth | Lấy progress của step |
| `/user/progress/learning-path/{learningPathId}` | GET | Auth | Lấy progress của learning path |
| `/user/progress/study-set/{studySetId}/learning-paths` | GET | Auth | Lấy progress tất cả learning paths |

---

**Tổng số APIs: 50+**

**Module Types:**
- FLASHCARD (từ repo-flashcard)
- WRITING (từ repo-writing)
- KANJI_ORIGIN (từ repo-kanji-origin)

**Progress Status:**
- NOT_STARTED
- IN_PROGRESS
- COMPLETED

---

*Tài liệu này mô tả đầy đủ các luồng xử lý API của repo-learning-path. Mọi thay đổi về business logic hoặc API mới cần được cập nhật vào tài liệu này.*
