# Video Course API Guide

## 1. Tóm tắt chung

Repository `repo-video-course` quản lý luồng video học và các thực thể: Package -> Folder -> StudySet -> VideoStep -> VideoModule.

- Public GET: `/packages`, `/folders`, `/study-sets`, `/video-steps`, `/video-modules`
- Admin/Teacher (có JWT, role ADMIN/TEACHER): tạo/sửa/xóa nội dung và /admin/*
- Người dùng đã đăng nhập (JWT): `/progress/**` + tất cả POST/PUT/DELETE.

## 2. Authentication

- JWT được validate qua `SecurityConfig` + `BaseJwtFilter`.
- Public GET đã được mở cho các endpoint course metadata.
- Các endpoint create/update/delete và `/progress/**` yêu cầu JWT hợp lệ.

Header mẫu:

```
Authorization: Bearer <token>
Content-Type: application/json
```

## 3. Luồng chính (học viên)

1. Lấy cấu trúc chương trình:
   - `GET /syllabus/all` -> danh sách package + folder + study sets.
2. Lấy danh sách study-sets:
   - `GET /study-sets` (q optional) hoặc `GET /folders/{folderId}`, `GET /study-sets/my-sets`
3. Lấy các bước video của 1 study set:
   - `GET /video-steps/study-set/{studySetId}`
4. Lấy danh sách module của bước:
   - `GET /video-modules/step/{stepId}`
5. Khởi hành xem video module:
   - `GET /progress/modules/{moduleId}/start` (Authentication required)
6. Cập nhật progress xem:
   - `PUT /progress/modules/{moduleId}/watch` (body `UpdateWatchProgressRequest`)
7. Complete video thực nghiệm:
   - `POST /progress/modules/{moduleId}/complete` (body `CompleteWatchRequest`)
8. Kiểm tra progress:
   - `GET /progress/modules/{moduleId}`
   - `GET /progress/steps/{stepId}`
   - `GET /progress/study-sets/{studySetId}`
   - `GET /progress/courses?studySetId={studySetId}`

## 4. Luồng quản trị (Admin/Teacher)

### 4.1 Quản lý VideoModule
- `POST /video-modules` create
- `GET /video-modules/{id}` read
- `GET /video-modules/step/{stepId}` list
- `PUT /video-modules/{id}` update
- `DELETE /video-modules/{id}` delete (soft-delete)

### 4.2 Quản lý VideoStep
- `POST /video-steps` create
- `GET /video-steps/{id}` read
- `GET /video-steps/study-set/{studySetId}` list
- `PUT /video-steps/{id}` update
- `DELETE /video-steps/{id}` delete

### 4.3 Quản lý StudySet/Folder/Package (delegate repo-content-common)

StudySet
- `POST /study-sets`
- `GET /study-sets/{id}`
- `GET /study-sets` (q optional)
- `GET /study-sets/folder/{folderId}`
- `GET /study-sets/my-sets`
- `PUT /study-sets/{id}`
- `DELETE /study-sets/{id}`

Folder
- `POST /folders`
- `GET /folders/{id}`
- `GET /folders/package/{packageId}`
- `GET /folders/my-folders`
- `PUT /folders/{id}`
- `DELETE /folders/{id}`
- `POST /folders/{folderId}/study-sets/{studySetId}` add link
- `DELETE /folders/{folderId}/study-sets/{studySetId}` remove link

Package
- `POST /packages`
- `GET /packages/{id}`
- `GET /packages` (filter `type`, `category`)
- `GET /packages/categories`
- `PUT /packages/{id}`
- `DELETE /packages/{id}`
- `POST /packages/{packageId}/folders/{folderId}`
- `DELETE /packages/{packageId}/folders/{folderId}`

### 4.4 Available module/video cho lựa chọn nội dung liên kết
- `GET /admin/available-modules`
- `GET /admin/available-modules/{type}` (flashcard, writing, kanji, quiz, listening, pronunciation, learning-path)
- `GET /admin/available-videos`
- `GET /admin/available-videos/{videoCode}`

## 5. Model request cơ bản

- `CreateVideoModuleRequest`:
  - `stepId` (string, required)
  - `title` (string, required)
  - `description` (string)
  - `moduleOrder` (integer, required)
  - `videoUrl`, `thumbnailUrl`, `duration`, `subtitles` (optional)
  - `videoCode`, `moduleType`, `contentSetId`, `isRequired` (optional)

- `UpdateVideoModuleRequest`: tất cả trường optional
  - `title`, `description`, `moduleOrder`, `videoUrl`, `thumbnailUrl`, `duration`, `subtitles`, `videoCode`, `moduleType`, `contentSetId`, `isRequired`, `isActive`

- `CreateVideoStepRequest`:
  - `studySetId` (string, required)
  - `title` (string, required)
  - `description`, `stepOrder` (integer >=1, required), `icon`, `color`, `estimatedMinutes`, `isRequired`

- `UpdateVideoStepRequest`: tất cả trường optional
  - `title`, `description`, `stepOrder`, `icon`, `color`, `estimatedMinutes`, `isRequired`, `isActive`

- `UpdateWatchProgressRequest`:
  - `watchedSeconds` (integer >=0, required)
  - `lastPositionSeconds` (integer >=0, optional)

- `CompleteWatchRequest`:
  - `forceComplete` (boolean, default true)


## 5.1 Điểm nhấn request/response theo endpoint

### `/video-modules`
- `POST /video-modules` (Admin/Teacher)
  - request: `CreateVideoModuleRequest`
  - response: `ApiResponse<VideoModuleResponse>`
  - công dụng: thêm module video (một bài học trong step).

- `GET /video-modules/{id}` (public)
  - request: none
  - response: `ApiResponse<VideoModuleResponse>`
  - công dụng: đọc chi tiết module; nếu đã đăng nhập thì `watchProgress` đi kèm.

- `GET /video-modules/step/{stepId}` (public)
  - request: none
  - response: `ApiResponse<List<VideoModuleResponse>>`
  - công dụng: lấy danh sách module trong step.

- `PUT /video-modules/{id}` (Admin/Teacher)
  - request: `UpdateVideoModuleRequest`
  - response: `ApiResponse<VideoModuleResponse>`
  - công dụng: cập nhật module.

- `DELETE /video-modules/{id}` (Admin/Teacher)
  - request: none
  - response: `ApiResponse<Void>`
  - công dụng: xóa mềm module.

### `/video-steps`
- `POST /video-steps` (Admin/Teacher)
  - request: `CreateVideoStepRequest`
  - response: `ApiResponse<VideoStepResponse>`
  - công dụng: tạo bước mới trong học phần.

- `GET /video-steps/{id}` (public)
  - response: `ApiResponse<VideoStepResponse>`
  - công dụng: xem chi tiết step.

- `GET /video-steps/study-set/{studySetId}` (public)
  - response: `ApiResponse<List<VideoStepResponse>>`
  - công dụng: lấy các bước trong một study set (kèm unlock/progress nếu đăng nhập).

- `PUT /video-steps/{id}` (Admin/Teacher)
  - request: `UpdateVideoStepRequest`
  - response: `ApiResponse<VideoStepResponse>`

- `DELETE /video-steps/{id}` (Admin/Teacher)
  - response: `ApiResponse<Void>`

### `/progress`
- `GET /progress/modules/{moduleId}/start` (auth)
  - response: `ApiResponse<VideoWatchProgressResponse>`
  - công dụng: khởi tạo theo dõi module.

- `PUT /progress/modules/{moduleId}/watch` (auth)
  - request: `UpdateWatchProgressRequest`
  - response: `ApiResponse<VideoWatchProgressResponse>`
  - công dụng: cập nhật số giây đã xem (tự complete ở ~80%).

- `POST /progress/modules/{moduleId}/complete` (auth)
  - request: `CompleteWatchRequest`
  - response: `ApiResponse<VideoWatchProgressResponse>`
  - công dụng: đánh dấu hoàn thành video (force).

- `GET /progress/modules/{moduleId}` (auth)
  - response: `ApiResponse<VideoWatchProgressResponse>`

- `GET /progress/steps/{stepId}` (auth)
  - response: `ApiResponse<VideoStepProgressResponse>`

- `GET /progress/study-sets/{studySetId}` (auth)
  - response: `ApiResponse<VideoCourseProgressResponse>`

- `GET /progress/courses?studySetId={studySetId}` (auth)
  - response: `ApiResponse<List<VideoCourseProgressResponse>>`

### `/syllabus/all`
- `GET /syllabus/all` (public)
  - response: `ApiResponse<List<SyllabusTreeDTO>>`
  - công dụng: tree toàn bộ hierarchy package/folder/study-set.

### `/study-sets` + `folders` + `packages`
- `GET /study-sets`, `GET /folders`, `GET /packages` (public): list/tra cứu.
- `GET /study-sets/{id}`, `GET /folders/{id}`, `GET /packages/{id}`
- `POST /study-sets`, `/folders`, `/packages` (Admin/Teacher): tạo.
- `PUT /study-sets/{id}`, `/folders/{id}`, `/packages/{id}` (Admin/Teacher): update.
- `DELETE /study-sets/{id}`, `/folders/{id}`, `/packages/{id}` (Admin/Teacher): xóa.
- `POST /folders/{folderId}/study-sets/{studySetId}`, `POST /packages/{packageId}/folders/{folderId}` (Admin/Teacher): link.
- `DELETE /folders/{folderId}/study-sets/{studySetId}`, `DELETE /packages/{packageId}/folders/{folderId}` (Admin/Teacher): unlink.

### `/admin/available-modules`, `/admin/available-videos`
- `GET /admin/available-modules` (q optional) -> `ApiResponse<List<AvailableModuleResponse>>`
- `GET /admin/available-videos` (q optional) -> `ApiResponse<List<AvailableVideoResponse>>`
- `GET /admin/available-videos/{videoCode}` -> `ApiResponse<AvailableVideoResponse>`

> Lưu ý: chi tiết DTO đầy đủ xem trong `repo-video-course/src/main/java/com/lms/videocourse/dto` và API spec Postman `video-course-api.postman_collection.json`.

## 6. Mẹo test thủ công

1. Chạy dịch vụ: `mvn spring-boot:run` tại `repo-video-course`.
2. Mở Postman và load `video-course-api.postman_collection.json`.
3. Thực hiện lần lượt:
   - `GET /syllabus/all`
   - `GET /study-sets?q=...`
   - `GET /video-steps/study-set/{id}`
   - `GET /video-modules/step/{id}`
4. Làm progress:
   - `GET /progress/modules/{id}/start` with token
   - `PUT /progress/modules/{id}/watch`
   - `GET /progress/modules/{id}`

## 7. Ghi chú cấu hình

- Bật `security.jwt.public-key-path` trong `application.yml` hoặc `bootstrap.yml`.
- Ràng buộc `ROLE_TEACHER`, `ROLE_ADMIN` cho write operation.

---

Document by: auto-generated guide
