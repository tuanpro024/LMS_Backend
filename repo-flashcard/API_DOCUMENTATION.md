# API Documentation - Repo Flashcard

## Thông tin chung
- **Base URL**: `http://localhost:8082`
- **Authentication**: Bearer Token (JWT từ repo-identity-api)
- **Response Format**: `{ "success": true/false, "data": {...}, "errorCode": "...", "errorMessage": "..." }`

## Danh sách API Endpoints

| STT | API Endpoint | Method | Controller | Mô tả chức năng | Request Body/Params | Trạng thái |
|-----|-------------|--------|------------|-----------------|---------------------|------------|
| **STUDY SET APIs** |
| 1 | `/study-sets` | POST | StudySetController | Tạo study set mới | **Body**: `{ "title": string, "description": string, "isPrivate": boolean, "cards": [{ "term": string, "definition": string, "cardIndex": number, "imageUrl": string }] }`<br/>**Auth**: Required | ✅ Hoàn thành |
| 2 | `/study-sets` | GET | StudySetController | Lấy danh sách study sets public | **Params**: Không<br/>**Auth**: Optional | ✅ Hoàn thành |
| 3 | `/study-sets?userId={userId}` | GET | StudySetController | Lấy danh sách study sets của một user | **Params**: `userId` (String)<br/>**Auth**: Optional (nếu có auth thì thấy cả private sets của chính mình) | ✅ Hoàn thành |
| 4 | `/study-sets?q={query}` | GET | StudySetController | Tìm kiếm study sets theo từ khóa | **Params**: `q` (String) - từ khóa tìm kiếm<br/>**Auth**: Optional (nếu có auth thì tìm cả private sets của mình) | ✅ Hoàn thành |
| 5 | `/study-sets/{id}` | GET | StudySetController | Lấy chi tiết một study set | **Path**: `id` (String) - ID của study set<br/>**Auth**: Optional (cần auth nếu study set là private) | ✅ Hoàn thành |
| 6 | `/study-sets/{id}` | PUT | StudySetController | Cập nhật study set | **Path**: `id` (String)<br/>**Body**: `{ "title": string, "description": string, "isPrivate": boolean, "cards": [...] }`<br/>**Auth**: Required (chỉ owner mới update được) | ✅ Hoàn thành |
| 7 | `/study-sets/{id}` | DELETE | StudySetController | Xóa study set | **Path**: `id` (String)<br/>**Auth**: Required (chỉ owner mới xóa được) | ✅ Hoàn thành |
| **FOLDER APIs** |
| 8 | `/folders` | POST | FolderController | Tạo folder mới | **Body**: `{ "name": string, "description": string, "color": string, "isPrivate": boolean, "parentFolderId": string }`<br/>**Auth**: Required | ✅ Hoàn thành |
| 9 | `/folders` | GET | FolderController | Lấy danh sách folders public | **Params**: Không<br/>**Auth**: Optional | ✅ Hoàn thành |
| 10 | `/folders?userId={userId}` | GET | FolderController | Lấy danh sách folders của một user | **Params**: `userId` (String)<br/>**Auth**: Optional (nếu có auth thì thấy cả private folders của chính mình) | ✅ Hoàn thành |
| 11 | `/folders?rootOnly=true` | GET | FolderController | Lấy danh sách root folders của user hiện tại | **Params**: `rootOnly=true`<br/>**Auth**: Required | ✅ Hoàn thành |
| 12 | `/folders/{id}` | GET | FolderController | Lấy chi tiết một folder | **Path**: `id` (String) - ID của folder<br/>**Auth**: Optional (cần auth nếu folder là private) | ✅ Hoàn thành |
| 13 | `/folders/{folderId}/study-sets/{studySetId}` | POST | FolderController | Thêm study set vào folder | **Path**: `folderId` (String), `studySetId` (String)<br/>**Auth**: Required (phải là owner của folder) | ✅ Hoàn thành |
| 14 | `/folders/{folderId}/study-sets/{studySetId}` | DELETE | FolderController | Xóa study set khỏi folder | **Path**: `folderId` (String), `studySetId` (String)<br/>**Auth**: Required (phải là owner của folder) | ✅ Hoàn thành |
| 15 | `/folders/{id}` | DELETE | FolderController | Xóa folder | **Path**: `id` (String)<br/>**Auth**: Required (chỉ owner mới xóa được) | ✅ Hoàn thành |

## Chi tiết Request/Response Models

### CreateStudySetRequest
```json
{
  "title": "English Vocabulary",
  "description": "Common English words",
  "isPrivate": false,
  "cards": [
    {
      "term": "Hello",
      "definition": "A greeting",
      "cardIndex": 0,
      "imageUrl": "https://example.com/image.jpg"
    }
  ]
}
```

### StudySetResponse
```json
{
  "success": true,
  "data": {
    "id": "01HQRS8G9V3XKZM2N4P6Q8",
    "title": "English Vocabulary",
    "description": "Common English words",
    "isPrivate": false,
    "userId": "01HQRS8G9V3XKZM2N4P6Q7",
    "cards": [
      {
        "id": "01HQRS8G9V3XKZM2N4P6Q9",
        "term": "Hello",
        "definition": "A greeting",
        "cardIndex": 0,
        "imageUrl": "https://example.com/image.jpg",
        "createdAt": "2024-01-08T08:30:00Z",
        "updatedAt": "2024-01-08T08:30:00Z"
      }
    ],
    "createdAt": "2024-01-08T08:30:00Z",
    "updatedAt": "2024-01-08T08:30:00Z"
  }
}
```

### CreateFolderRequest
```json
{
  "name": "My Study Materials",
  "description": "Collection of study sets",
  "color": "#0080ff",
  "isPrivate": false,
  "parentFolderId": "01HQRS8G9V3XKZM2N4P6Q5"
}
```

### FolderResponse
```json
{
  "success": true,
  "data": {
    "id": "01HQRS8G9V3XKZM2N4P6Q6",
    "name": "My Study Materials",
    "description": "Collection of study sets",
    "color": "#0080ff",
    "isPrivate": false,
    "userId": "01HQRS8G9V3XKZM2N4P6Q7",
    "parentFolderId": "01HQRS8G9V3XKZM2N4P6Q5",
    "studySetIds": [
      "01HQRS8G9V3XKZM2N4P6Q8",
      "01HQRS8G9V3XKZM2N4P6Q9"
    ],
    "createdAt": "2024-01-08T08:30:00Z",
    "updatedAt": "2024-01-08T08:30:00Z"
  }
}
```

## Authentication Flow

### 1. Login (via repo-identity-api)
```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

# Response
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
    "user": {...}
  }
}
```

### 2. Use Access Token
```bash
GET http://localhost:8082/study-sets
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

## Error Codes

| Error Code | HTTP Status | Mô tả |
|-----------|-------------|-------|
| E227 | 404 | Không tìm thấy dữ liệu (Study set/Folder not found) |
| E240 | 403 | Không có quyền truy cập |
| E234 | 401 | Token hết hạn |
| E241 | 401 | Token không hợp lệ |
| BAD_REQUEST | 400 | Dữ liệu đầu vào không hợp lệ |
| INTERNAL_ERROR | 500 | Lỗi server |

## Notes

- **Port**: Service chạy trên port `8082`
- **Authentication**: 
  - Public GET endpoints không cần auth
  - POST/PUT/DELETE endpoints yêu cầu auth
  - Private resources chỉ owner mới truy cập được
- **User ID**: Được extract từ JWT token, không cần gửi trong request body
- **Timestamps**: Tự động sinh bởi JPA auditing
- **ID Format**: ULID (26 ký tự)
