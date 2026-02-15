# LUỒNG HOẠT ĐỘNG REPO-QUIZ

## Tổng quan

Repo Quiz là microservice quản lý hệ thống quiz/trắc nghiệm trong hệ thống LMS. Repo này cung cấp các API để tạo, quản lý và thực hiện các bài quiz với nhiều loại câu hỏi khác nhau.

## Cấu trúc dữ liệu phân tầng

```
Package (Gói học)
    ├── Subject (Môn học)
    │   ├── Slot (Buổi học)
    │   │   ├── Folder (Thư mục)
    │   │   │   ├── StudySet (Bộ học)
    │   │   │   │   ├── Quiz (Bài quiz)
    │   │   │   │   │   └── QuizQuestion (Câu hỏi)
    │   │   │   │   │       ├── QuizOption (Lựa chọn - Multiple Choice)
    │   │   │   │   │       ├── QuizBlank (Chỗ trống - Fill in Blank)
    │   │   │   │   │       ├── MatchingPair (Cặp ghép - Matching)
    │   │   │   │   │       └── SentenceChunk (Mảnh câu - Sentence Builder)
```

---

## 1. PACKAGE MANAGEMENT (Quản lý Gói học)

### 1.1. Lấy danh sách loại Package
**Endpoint:** `GET /api/quiz/packages/types`  
**Quyền:** Public  
**Mô tả:** Lấy danh sách các loại package có sẵn

**Flow:**
1. Client gọi API
2. Hệ thống trả về danh sách TypeName enum:
   - `FREE` - Tự do
   - `LEARNING_PATH` - Ôn luyện
   - `VIDEO_COURSE` - Video khóa học
   - `LEARNING` - 1-1, 1-n

**Response:**
```json
{
  "data": [
    {"id": "FREE", "name": "Free"},
    {"id": "LEARNING_PATH", "name": "Learning Path"}
  ]
}
```

---

### 1.2. Tạo Package mới
**Endpoint:** `POST /api/quiz/packages`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  
**Request Body:**
```json
{
  "name": "JLPT N5 Preparation",
  "description": "Khóa học luyện thi JLPT N5",
  "type": "FREE",
  "thumbnail": "https://example.com/image.jpg"
}
```

**Flow:**
1. Client gửi request với bearer token (JWT)
2. Hệ thống xác thực quyền (ROLE_TEACHER hoặc ROLE_ADMIN)
3. Validate dữ liệu đầu vào:
   - `name` (bắt buộc, không được trống)
   - `type` (bắt buộc, phải là một trong các TypeName enum)
   - `description` (tùy chọn)
   - `thumbnail` (tùy chọn)
4. Tạo Package với userId từ JWT token
5. Trả về PackageResponse với status 201 Created

**Success Response (201):**
```json
{
  "data": {
    "id": "01ABC...",
    "name": "JLPT N5 Preparation",
    "type": "FREE",
    "description": "Khóa học luyện thi JLPT N5",
    "thumbnail": "https://example.com/image.jpg",
    "createdBy": "01USER...",
    "createdAt": "2026-02-14T20:00:00Z"
  }
}
```

**Error Cases:**
- 400 Bad Request: Thiếu field bắt buộc hoặc type không hợp lệ
- 401 Unauthorized: Không có token
- 403 Forbidden: Không có quyền (không phải TEACHER/ADMIN)

---

### 1.3. Lấy thông tin Package
**Endpoint:** `GET /api/quiz/packages/{id}`  
**Quyền:** Public  

**Flow:**
1. Client gửi request với packageId
2. Hệ thống tìm kiếm Package theo ID
3. Trả về thông tin chi tiết Package

**Error Cases:**
- 404 Not Found: Package không tồn tại

---

### 1.4. Lấy danh sách tất cả Packages
**Endpoint:** `GET /api/quiz/packages`  
**Query Params:** `?type=FREE` (tùy chọn)  
**Quyền:** Public  

**Flow:**
1. Client gọi API (có thể filter theo type)
2. Nếu có param `type`: Lọc packages theo type
3. Nếu không có param: Trả về tất cả packages
4. Trả về danh sách PackageResponse

---

### 1.5. Cập nhật Package
**Endpoint:** `PUT /api/quiz/packages/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  
**Request Body:** Tương tự Create Package

**Flow:**
1. Client gửi request với packageId và dữ liệu mới
2. Xác thực quyền
3. **Kiểm tra ownership:** Verify user là người tạo Package
4. Validate dữ liệu
5. Cập nhật thông tin Package
6. Trả về PackageResponse đã cập nhật

**Error Cases:**
- 403 Forbidden: User không phải là owner
- 404 Not Found: Package không tồn tại

---

### 1.6. Xóa Package
**Endpoint:** `DELETE /api/quiz/packages/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Client gửi request với packageId
2. Xác thực quyền
3. Kiểm tra ownership
4. Xóa Package (cascade delete các Subject liên quan)
5. Trả về status 200

---

### 1.7. Thêm Folder vào Package
**Endpoint:** `POST /api/quiz/packages/{packageId}/folders/{folderId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Client gửi packageId và folderId
2. Xác thực quyền
3. Kiểm tra ownership của Package
4. Kiểm tra Folder tồn tại
5. Liên kết Folder với Package
6. Trả về PackageResponse cập nhật

---

### 1.8. Xóa Folder khỏi Package
**Endpoint:** `DELETE /api/quiz/packages/{packageId}/folders/{folderId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:** Tương tự 1.7 nhưng xóa liên kết

---

## 2. SUBJECT MANAGEMENT (Quản lý Môn học)

### 2.1. Tạo Subject
**Endpoint:** `POST /api/quiz/subjects`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  
**Request Body:**
```json
{
  "name": "Japanese Grammar",
  "description": "Ngữ pháp tiếng Nhật",
  "packageId": "01ABC...",
  "thumbnail": "url"
}
```

**Flow:**
1. Xác thực quyền
2. Validate dữ liệu (name bắt buộc)
3. Kiểm tra Package tồn tại
4. Tạo Subject liên kết với Package
5. Trả về SubjectResponse (201)

---

### 2.2. Lấy thông tin Subject
**Endpoint:** `GET /api/quiz/subjects/{id}`  
**Quyền:** Public  

---

### 2.3. Lấy tất cả Subjects
**Endpoint:** `GET /api/quiz/subjects`  
**Quyền:** Public  

---

### 2.4. Lấy Subjects theo Package
**Endpoint:** `GET /api/quiz/subjects/package/{packageId}`  
**Quyền:** Public  

**Flow:**
1. Tìm tất cả Subject có packageId
2. Trả về danh sách SubjectResponse

---

### 2.5. Cập nhật Subject
**Endpoint:** `PUT /api/quiz/subjects/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Xác thực quyền
2. Kiểm tra ownership
3. Cập nhật thông tin
4. Trả về SubjectResponse

---

### 2.6. Xóa Subject
**Endpoint:** `DELETE /api/quiz/subjects/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

---

### 2.7. Thêm Folder vào Subject
**Endpoint:** `POST /api/quiz/subjects/{subjectId}/folders/{folderId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

---

### 2.8. Xóa Folder khỏi Subject
**Endpoint:** `DELETE /api/quiz/subjects/{subjectId}/folders/{folderId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

---

## 3. SLOT MANAGEMENT (Quản lý Buổi học)

### 3.1. Tạo Slot
**Endpoint:** `POST /api/quiz/slots`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  
**Request Body:**
```json
{
  "name": "Week 1 - Lesson 1",
  "description": "Hiragana basics",
  "subjectId": "01SUB...",
  "orderIndex": 1
}
```

**Flow:**
1. Xác thực quyền
2. Validate dữ liệu
3. Kiểm tra Subject tồn tại
4. Tạo Slot liên kết với Subject
5. Trả về SlotResponse (201)

---

### 3.2. Lấy Slot theo ID
**Endpoint:** `GET /api/quiz/slots/{id}`  
**Quyền:** Public  

---

### 3.3. Lấy tất cả Slots
**Endpoint:** `GET /api/quiz/slots`  
**Quyền:** Public  

---

### 3.4. Lấy Slots theo Subject
**Endpoint:** `GET /api/quiz/slots/subject/{subjectId}`  
**Quyền:** Public  

---

### 3.5. Cập nhật Slot
**Endpoint:** `PUT /api/quiz/slots/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

---

### 3.6. Xóa Slot
**Endpoint:** `DELETE /api/quiz/slots/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

---

### 3.7. Thêm Folder vào Slot
**Endpoint:** `POST /api/quiz/slots/{slotId}/folders/{folderId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

---

### 3.8. Xóa Folder khỏi Slot
**Endpoint:** `DELETE /api/quiz/slots/{slotId}/folders/{folderId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

---

## 4. FOLDER MANAGEMENT (Quản lý Thư mục)

### 4.1. Tạo Folder
**Endpoint:** `POST /api/quiz/folders`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  
**Request Body:**
```json
{
  "name": "Vocabulary Quiz",
  "description": "Từ vựng cơ bản",
  "isPrivate": false
}
```

**Flow:**
1. Xác thực quyền
2. Validate dữ liệu (name bắt buộc)
3. Tạo Folder với userId
4. Trả về FolderResponse (201)

---

### 4.2. Lấy Folder theo ID
**Endpoint:** `GET /api/quiz/folders/{id}`  
**Quyền:** Public (nếu public), Owner (nếu private)  

---

### 4.3. Lấy tất cả Folders
**Endpoint:** `GET /api/quiz/folders`  
**Query Params:** `?userId=01USER...` (tùy chọn)  
**Quyền:** Public  

**Flow:**
1. Nếu có `userId`: Filter folders theo userId
2. Nếu không: Trả về tất cả folders (chỉ public)
3. Trả về danh sách FolderResponse

---

### 4.4. Lấy Folders theo Package
**Endpoint:** `GET /api/quiz/folders/package/{packageId}`  
**Quyền:** Public  

---

### 4.5. Cập nhật Folder
**Endpoint:** `PUT /api/quiz/folders/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Xác thực quyền
2. Kiểm tra ownership
3. Cập nhật thông tin
4. Trả về FolderResponse

---

### 4.6. Cập nhật Privacy của Folder
**Endpoint:** `PATCH /api/quiz/folders/{id}/privacy`  
**Query Params:** `?isPrivate=true`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Xác thực quyền và ownership
2. Cập nhật trạng thái isPrivate
3. Trả về status 200

---

### 4.7. Xóa Folder
**Endpoint:** `DELETE /api/quiz/folders/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Kiểm tra ownership
2. Xóa Folder (cascade delete StudySets)

---

### 4.8. Thêm StudySet vào Folder
**Endpoint:** `POST /api/quiz/folders/{folderId}/study-sets/{studySetId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Kiểm tra ownership của Folder
2. Kiểm tra StudySet tồn tại
3. Liên kết StudySet với Folder
4. Trả về FolderResponse

---

### 4.9. Xóa StudySet khỏi Folder
**Endpoint:** `DELETE /api/quiz/folders/{folderId}/study-sets/{studySetId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

---

## 5. STUDY SET MANAGEMENT (Quản lý Bộ học)

### 5.1. Tạo StudySet
**Endpoint:** `POST /api/quiz/study-sets`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  
**Request Body:**
```json
{
  "name": "JLPT N5 Grammar Quiz Set",
  "description": "Bài tập ngữ pháp N5",
  "thumbnail": "url"
}
```

**Flow:**
1. Xác thực quyền
2. Validate dữ liệu (name bắt buộc)
3. Tạo StudySet với userId
4. Trả về StudySetResponse (201)

---

### 5.2. Lấy StudySet theo ID
**Endpoint:** `GET /api/quiz/study-sets/{id}`  
**Quyền:** Public  

---

### 5.3. Lấy tất cả StudySets
**Endpoint:** `GET /api/quiz/study-sets`  
**Query Params:** 
- `?userId=01USER...` (tùy chọn)
- `?q=search term` (tùy chọn - tìm kiếm)
**Quyền:** Public  

**Flow:**
1. Nếu có `userId`: Filter theo userId
2. Nếu có `q`: Tìm kiếm theo keyword
3. Nếu không có param: Trả về tất cả
4. Trả về danh sách StudySetResponse

---

### 5.4. Lấy StudySets theo Folder
**Endpoint:** `GET /api/quiz/study-sets/folder/{folderId}`  
**Quyền:** Public  

---

### 5.5. Cập nhật StudySet
**Endpoint:** `PUT /api/quiz/study-sets/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Kiểm tra ownership
2. Cập nhật thông tin
3. Trả về StudySetResponse

---

### 5.6. Xóa StudySet
**Endpoint:** `DELETE /api/quiz/study-sets/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Kiểm tra ownership
2. Xóa StudySet (cascade delete Quizzes)

---

## 6. QUIZ MANAGEMENT (Quản lý Bài Quiz)

### 6.1. Tạo Quiz
**Endpoint:** `POST /api/quiz/quizzes`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  
**Request Body:**
```json
{
  "title": "N5 Grammar Test 1",
  "description": "Test ngữ pháp cơ bản",
  "instruction": "Chọn đáp án đúng nhất",
  "studySetId": "01STUDY...",
  "difficulty": "MEDIUM",
  "timeLimitSeconds": 1800,
  "passingScore": 70,
  "shuffleQuestions": true
}
```

**Flow:**
1. Xác thực quyền (ADMIN/TEACHER)
2. Validate dữ liệu:
   - `title` (bắt buộc)
   - `studySetId` (bắt buộc)
   - `difficulty`: EASY, MEDIUM, HARD (mặc định: MEDIUM)
   - `timeLimitSeconds` (mặc định: 0 - không giới hạn)
   - `passingScore` (mặc định: 70)
   - `shuffleQuestions` (mặc định: true)
3. Kiểm tra StudySet tồn tại
4. Tạo Quiz với userId
5. Trả về QuizDetailResponse (201)

**Response:**
```json
{
  "data": {
    "id": "01QUIZ...",
    "title": "N5 Grammar Test 1",
    "description": "Test ngữ pháp cơ bản",
    "difficulty": "MEDIUM",
    "timeLimitSeconds": 1800,
    "passingScore": 70,
    "questions": [],
    "totalQuestions": 0,
    "totalPoints": 0
  }
}
```

---

### 6.2. Lấy Quiz theo ID
**Endpoint:** `GET /api/quiz/quizzes/{id}`  
**Quyền:** Public  

**Flow:**
1. Tìm Quiz theo ID
2. Trả về QuizDetailResponse (bao gồm tất cả questions và đáp án)

---

### 6.3. Lấy Quizzes theo StudySet
**Endpoint:** `GET /api/quiz/quizzes/study-set/{studySetId}`  
**Quyền:** Public  

**Flow:**
1. Tìm tất cả Quiz trong StudySet
2. Trả về danh sách QuizResponse (không bao gồm questions)

---

### 6.4. Lấy Quiz để làm bài (Attempt)
**Endpoint:** `GET /api/quiz/quizzes/{id}/attempt`  
**Quyền:** Public (Authenticated user)  

**Flow:**
1. Tìm Quiz theo ID
2. **Shuffle questions** nếu `shuffleQuestions = true`
3. **Ẩn đáp án đúng** trong response
4. Trả về QuizDetailResponse (chỉ hiển thị câu hỏi, không có correctAnswer)

**Response (questions không có correctAnswer):**
```json
{
  "data": {
    "id": "01QUIZ...",
    "title": "N5 Grammar Test 1",
    "timeLimitSeconds": 1800,
    "questions": [
      {
        "id": "01Q1...",
        "questionType": "MULTIPLE_CHOICE",
        "questionText": "Watashi ___ gakusei desu",
        "options": [
          {"id": "01OPT1", "text": "wa", "optionIndex": 0},
          {"id": "01OPT2", "text": "ga", "optionIndex": 1},
          {"id": "01OPT3", "text": "wo", "optionIndex": 2}
        ]
      }
    ]
  }
}
```

---

### 6.5. Cập nhật Quiz
**Endpoint:** `PUT /api/quiz/quizzes/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  
**Request Body:** Tương tự Create Quiz

**Flow:**
1. Xác thực quyền
2. Kiểm tra ownership (userId)
3. Validate dữ liệu
4. Cập nhật Quiz
5. Trả về QuizDetailResponse

**Error Cases:**
- 403 Forbidden: Không phải owner
- 404 Not Found: Quiz không tồn tại

---

### 6.6. Xóa Quiz
**Endpoint:** `DELETE /api/quiz/quizzes/{id}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Kiểm tra ownership
2. Xóa Quiz (cascade delete tất cả QuizQuestions)
3. Trả về 204 No Content

---

## 7. QUIZ QUESTION MANAGEMENT (Quản lý Câu hỏi)

### 7.1. Các loại câu hỏi (QuestionType)

#### 7.1.1. MULTIPLE_CHOICE (Trắc nghiệm)
**Cấu trúc:**
- `questionText`: Nội dung câu hỏi
- `options[]`: Danh sách lựa chọn
  - `text`: Nội dung lựa chọn
  - `isCorrect`: Đáp án đúng (true/false)
  - `optionIndex`: Thứ tự hiển thị

**Ví dụ:**
```json
{
  "questionType": "MULTIPLE_CHOICE",
  "questionText": "Watashi ___ gakusei desu",
  "questionIndex": 0,
  "points": 1,
  "difficulty": "EASY",
  "options": [
    {"text": "wa", "isCorrect": true, "optionIndex": 0},
    {"text": "ga", "isCorrect": false, "optionIndex": 1},
    {"text": "wo", "isCorrect": false, "optionIndex": 2},
    {"text": "ni", "isCorrect": false, "optionIndex": 3}
  ]
}
```

---

#### 7.1.2. FILL_IN_BLANK (Điền vào chỗ trống)
**Cấu trúc:**
- `sentenceTemplate`: Câu có chỗ trống (dùng `{blank}` để đánh dấu)
- `fillBlankMode`: 
  - `SELECT` - Chọn từ danh sách
  - `TYPE` - Gõ tự do
- `blanks[]`: Danh sách chỗ trống
  - `blankIndex`: Thứ tự chỗ trống
  - `correctAnswer`: Đáp án đúng
  - `acceptableAnswers[]`: Các đáp án được chấp nhận

**Ví dụ SELECT mode:**
```json
{
  "questionType": "FILL_IN_BLANK",
  "sentenceTemplate": "Watashi {blank} gakusei desu. Anata {blank} sensei desu.",
  "fillBlankMode": "SELECT",
  "questionIndex": 1,
  "points": 2,
  "blanks": [
    {
      "blankIndex": 0,
      "correctAnswer": "wa",
      "acceptableAnswers": ["wa"]
    },
    {
      "blankIndex": 1,
      "correctAnswer": "wa",
      "acceptableAnswers": ["wa", "mo"]
    }
  ],
  "options": [
    {"text": "wa", "optionIndex": 0},
    {"text": "ga", "optionIndex": 1},
    {"text": "mo", "optionIndex": 2}
  ]
}
```

**Ví dụ TYPE mode:**
```json
{
  "questionType": "FILL_IN_BLANK",
  "sentenceTemplate": "I {blank} to school every day.",
  "fillBlankMode": "TYPE",
  "questionIndex": 2,
  "blanks": [
    {
      "blankIndex": 0,
      "correctAnswer": "go",
      "acceptableAnswers": ["go", "walk"]
    }
  ]
}
```

---

#### 7.1.3. MATCHING_PAIRS (Ghép cặp)
**Cấu trúc:**
- `matchingPairs[]`: Danh sách cặp cần ghép
  - `leftItem`: Phần tử bên trái
  - `rightItem`: Phần tử bên phải tương ứng
  - `pairIndex`: Thứ tự cặp

**Ví dụ:**
```json
{
  "questionType": "MATCHING_PAIRS",
  "questionText": "Ghép từ tiếng Nhật với nghĩa tiếng Anh",
  "questionIndex": 3,
  "points": 4,
  "matchingPairs": [
    {"pairIndex": 0, "leftItem": "ありがとう", "rightItem": "Thank you"},
    {"pairIndex": 1, "leftItem": "おはよう", "rightItem": "Good morning"},
    {"pairIndex": 2, "leftItem": "さようなら", "rightItem": "Goodbye"},
    {"pairIndex": 3, "leftItem": "すみません", "rightItem": "Excuse me"}
  ]
}
```

**Cách làm bài:**
- Hệ thống hiển thị `leftItem` và `rightItem` riêng biệt
- User kéo thả hoặc chọn để ghép đôi
- Chấm điểm: Tất cả cặp đúng mới được điểm

---

#### 7.1.4. SENTENCE_BUILDER (Sắp xếp câu)
**Cấu trúc:**
- `sentenceChunks[]`: Các mảnh câu cần sắp xếp
  - `chunkText`: Nội dung mảnh câu
  - `correctPosition`: Vị trí đúng trong câu
- `correctSentence`: Câu hoàn chỉnh đúng
- `translationHint`: Gợi ý dịch nghĩa

**Ví dụ:**
```json
{
  "questionType": "SENTENCE_BUILDER",
  "questionText": "Sắp xếp các từ thành câu hoàn chỉnh",
  "translationHint": "Tôi là học sinh",
  "correctSentence": "Watashi wa gakusei desu",
  "questionIndex": 4,
  "points": 3,
  "sentenceChunks": [
    {"chunkText": "Watashi", "correctPosition": 0},
    {"chunkText": "wa", "correctPosition": 1},
    {"chunkText": "gakusei", "correctPosition": 2},
    {"chunkText": "desu", "correctPosition": 3}
  ]
}
```

**Cách làm bài:**
- Hệ thống hiển thị các chunks ngẫu nhiên
- User sắp xếp lại theo thứ tự đúng
- Chấm điểm: Tất cả vị trí đúng mới được điểm

---

### 7.2. Thêm câu hỏi vào Quiz
**Endpoint:** `POST /api/quiz/questions/{quizId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Request Body (MULTIPLE_CHOICE):**
```json
{
  "questionType": "MULTIPLE_CHOICE",
  "questionText": "Choose the correct particle",
  "questionIndex": 0,
  "points": 1,
  "difficulty": "EASY",
  "explanation": "Giải thích: wa dùng để chỉ chủ đề",
  "options": [
    {"text": "wa", "isCorrect": true, "optionIndex": 0},
    {"text": "ga", "isCorrect": false, "optionIndex": 1}
  ]
}
```

**Flow:**
1. Xác thực quyền
2. Kiểm tra Quiz tồn tại
3. Kiểm tra ownership của Quiz
4. Validate theo questionType:
   - MULTIPLE_CHOICE: Cần ít nhất 2 options, phải có 1 isCorrect
   - FILL_IN_BLANK: Cần sentenceTemplate và blanks
   - MATCHING_PAIRS: Cần ít nhất 2 pairs
   - SENTENCE_BUILDER: Cần correctSentence và chunks
5. Tạo QuizQuestion và các entities con tương ứng
6. Trả về QuestionResponse (201)

---

### 7.3. Lấy câu hỏi theo Quiz
**Endpoint:** `GET /api/quiz/questions/quiz/{quizId}`  
**Quyền:** Public  

**Flow:**
1. Tìm tất cả Question của Quiz
2. Sắp xếp theo `questionIndex`
3. Trả về danh sách QuestionResponse đầy đủ

---

### 7.4. Cập nhật câu hỏi
**Endpoint:** `PUT /api/quiz/questions/{questionId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Kiểm tra ownership của Quiz
2. Validate dữ liệu mới
3. Cập nhật Question
4. **Xóa và tạo lại** các entities con (options, blanks, pairs, chunks)
5. Trả về QuestionResponse

---

### 7.5. Xóa câu hỏi
**Endpoint:** `DELETE /api/quiz/questions/{questionId}`  
**Quyền:** ROLE_TEACHER, ROLE_ADMIN  

**Flow:**
1. Kiểm tra ownership
2. Xóa Question (cascade delete tất cả options, blanks, pairs, chunks)
3. Trả về 204 No Content

---

## 8. QUIZ ATTEMPT (Làm bài và Chấm điểm)

### 8.1. Nộp bài Quiz
**Endpoint:** `POST /api/quiz/attempts/submit`  
**Quyền:** Authenticated user  

**Request Body:**
```json
{
  "quizId": "01QUIZ...",
  "answers": [
    {
      "questionId": "01Q1...",
      "questionType": "MULTIPLE_CHOICE",
      "selectedOptionIds": ["01OPT1..."]
    },
    {
      "questionId": "01Q2...",
      "questionType": "FILL_IN_BLANK",
      "blankAnswers": [
        {"blankIndex": 0, "answer": "wa"},
        {"blankIndex": 1, "answer": "ga"}
      ]
    },
    {
      "questionId": "01Q3...",
      "questionType": "MATCHING_PAIRS",
      "pairMatches": [
        {"leftItem": "ありがとう", "rightItem": "Thank you"},
        {"leftItem": "おはよう", "rightItem": "Good morning"}
      ]
    },
    {
      "questionId": "01Q4...",
      "questionType": "SENTENCE_BUILDER",
      "orderedChunks": ["Watashi", "wa", "gakusei", "desu"]
    }
  ]
}
```

---

### 8.2. Flow chấm điểm

#### 8.2.1. Khởi tạo
1. Client gửi request với `quizId` và `answers[]`
2. Hệ thống lấy `userId` từ JWT token
3. Validate Quiz tồn tại
4. Lấy tất cả Questions của Quiz

---

#### 8.2.2. Chấm từng câu hỏi

**MULTIPLE_CHOICE:**
```
1. Lấy selectedOptionIds từ user answer
2. Lấy correctOptionIds từ database (WHERE isCorrect = true)
3. So sánh:
   IF selectedOptionIds == correctOptionIds:
     isCorrect = true
     earnedPoints = question.points
   ELSE:
     isCorrect = false
     earnedPoints = 0
```

**FILL_IN_BLANK:**
```
1. Lấy blankAnswers[] từ user
2. FOR EACH blank:
     userAnswer = blankAnswers[blankIndex].answer.toLowerCase().trim()
     acceptableAnswers = blank.acceptableAnswers (from DB)
     
     IF userAnswer IN acceptableAnswers:
       blankCorrect = true
     ELSE:
       blankCorrect = false
3. IF tất cả blanks đều correct:
     isCorrect = true
     earnedPoints = question.points
   ELSE:
     isCorrect = false
     earnedPoints = 0
```

**MATCHING_PAIRS:**
```
1. Lấy pairMatches[] từ user
2. FOR EACH correctPair trong database:
     userMatch = tìm trong pairMatches WHERE leftItem == correctPair.leftItem
     
     IF userMatch.rightItem == correctPair.rightItem:
       pairCorrect = true
     ELSE:
       pairCorrect = false
3. IF tất cả pairs đều correct:
     isCorrect = true
     earnedPoints = question.points
   ELSE:
     isCorrect = false
     earnedPoints = 0
```

**SENTENCE_BUILDER:**
```
1. Lấy orderedChunks[] từ user
2. Lấy correctOrder từ database (sort by correctPosition)
3. FOR i = 0 to orderedChunks.length:
     IF orderedChunks[i] != correctOrder[i]:
       isCorrect = false
       BREAK
4. IF isCorrect:
     earnedPoints = question.points
   ELSE:
     earnedPoints = 0
```

---

#### 8.2.3. Tính tổng điểm
```
totalPoints = SUM(question.points) // Tổng điểm tối đa
earnedPoints = SUM(earnedPoints cho mỗi câu)
percentageScore = (earnedPoints / totalPoints) * 100

IF percentageScore >= quiz.passingScore:
  passed = true
ELSE:
  passed = false
```

---

#### 8.2.4. Lưu kết quả (Tùy chọn)
Tạo QuizAttempt record:
```json
{
  "quizId": "01QUIZ...",
  "userId": "01USER...",
  "totalPoints": 10,
  "earnedPoints": 8,
  "percentageScore": 80,
  "passed": true,
  "completedAt": "2026-02-14T20:30:00Z",
  "timeSpent": 450
}
```

---

### 8.3. Response Nộp bài
**Success Response (200):**
```json
{
  "data": {
    "quizId": "01QUIZ...",
    "quizTitle": "N5 Grammar Test 1",
    "totalQuestions": 10,
    "totalPoints": 10,
    "earnedPoints": 8,
    "percentageScore": 80,
    "passed": true,
    "passingScore": 70,
    "results": [
      {
        "questionId": "01Q1...",
        "questionType": "MULTIPLE_CHOICE",
        "questionText": "Watashi ___ gakusei desu",
        "isCorrect": true,
        "earnedPoints": 1,
        "maxPoints": 1,
        "userAnswer": {
          "selectedOptionIds": ["01OPT1..."]
        },
        "correctAnswer": {
          "correctOptionIds": ["01OPT1..."]
        },
        "explanation": "wa dùng để chỉ chủ đề"
      },
      {
        "questionId": "01Q2...",
        "questionType": "FILL_IN_BLANK",
        "questionText": "Watashi {blank} sensei desu",
        "isCorrect": false,
        "earnedPoints": 0,
        "maxPoints": 1,
        "userAnswer": {
          "blankAnswers": [{"blankIndex": 0, "answer": "ga"}]
        },
        "correctAnswer": {
          "blanks": [{"blankIndex": 0, "correctAnswer": "wa", "acceptableAnswers": ["wa"]}]
        }
      }
    ],
    "completedAt": "2026-02-14T20:30:00Z"
  }
}
```

---

## 9. AUTHORIZATION & AUTHORIZATION MATRIX

### 9.1. Roles
- **PUBLIC**: Không cần đăng nhập
- **ROLE_STUDENT**: Học viên (đã đăng nhập)
- **ROLE_TEACHER**: Giáo viên
- **ROLE_ADMIN**: Quản trị viên

### 9.2. Permission Matrix

| Endpoint | Method | Public | Student | Teacher | Admin | Ownership Check |
|----------|--------|--------|---------|---------|-------|-----------------|
| `/packages/types` | GET | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/packages` | POST | ❌ | ❌ | ✅ | ✅ | ❌ |
| `/packages/{id}` | GET | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/packages` | GET | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/packages/{id}` | PUT | ❌ | ❌ | ✅ | ✅ | ✅ (createdBy) |
| `/packages/{id}` | DELETE | ❌ | ❌ | ✅ | ✅ | ✅ (createdBy) |
| `/packages/{id}/folders/{folderId}` | POST | ❌ | ❌ | ✅ | ✅ | ✅ |
| `/subjects` | POST | ❌ | ❌ | ✅ | ✅ | ❌ |
| `/subjects/{id}` | GET | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/subjects/{id}` | PUT/DELETE | ❌ | ❌ | ✅ | ✅ | ✅ |
| `/slots` | POST | ❌ | ❌ | ✅ | ✅ | ❌ |
| `/slots/{id}` | GET | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/slots/{id}` | PUT/DELETE | ❌ | ❌ | ✅ | ✅ | ✅ |
| `/folders` | POST | ❌ | ❌ | ✅ | ✅ | ❌ |
| `/folders/{id}` | GET | ✅ | ✅ | ✅ | ✅ | Privacy check |
| `/folders/{id}` | PUT/DELETE | ❌ | ❌ | ✅ | ✅ | ✅ (createdBy) |
| `/folders/{id}/privacy` | PATCH | ❌ | ❌ | ✅ | ✅ | ✅ |
| `/study-sets` | POST | ❌ | ❌ | ✅ | ✅ | ❌ |
| `/study-sets/{id}` | GET | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/study-sets` | GET (search) | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/study-sets/{id}` | PUT/DELETE | ❌ | ❌ | ✅ | ✅ | ✅ |
| `/quizzes` | POST | ❌ | ❌ | ✅ | ✅ | ❌ |
| `/quizzes/{id}` | GET | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/quizzes/{id}/attempt` | GET | ❌ | ✅ | ✅ | ✅ | ❌ |
| `/quizzes/{id}` | PUT/DELETE | ❌ | ❌ | ✅ | ✅ | ✅ (owner) |
| `/questions/{quizId}` | POST | ❌ | ❌ | ✅ | ✅ | Quiz ownership |
| `/questions/{questionId}` | PUT/DELETE | ❌ | ❌ | ✅ | ✅ | Quiz ownership |
| `/attempts/submit` | POST | ❌ | ✅ | ✅ | ✅ | ❌ |

---

## 10. ERROR HANDLING & STATUS CODES

### 10.1. HTTP Status Codes

**Success:**
- `200 OK` - Request thành công (GET, PUT, PATCH, DELETE)
- `201 Created` - Tạo mới thành công (POST)
- `204 No Content` - Xóa thành công

**Client Errors:**
- `400 Bad Request` - Validation error (thiếu field, sai format)
- `401 Unauthorized` - Không có token hoặc token hết hạn
- `403 Forbidden` - Không có quyền hoặc không phải owner
- `404 Not Found` - Resource không tồn tại

**Server Errors:**
- `500 Internal Server Error` - Lỗi hệ thống

---

### 10.2. Error Response Format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Type is required",
    "details": {
      "field": "type",
      "rejectedValue": null
    }
  },
  "timestamp": "2026-02-14T20:21:26.095+07:00",
  "path": "/api/quiz/packages"
}
```

---

## 11. COMMON WORKFLOWS

### 11.1. Tạo khóa học hoàn chỉnh (End-to-End)

```
1. Tạo Package
   POST /api/quiz/packages
   → packageId

2. Tạo Subject trong Package
   POST /api/quiz/subjects
   Body: {packageId: "..."}
   → subjectId

3. Tạo Slot trong Subject
   POST /api/quiz/slots
   Body: {subjectId: "..."}
   → slotId

4. Tạo Folder
   POST /api/quiz/folders
   → folderId

5. Liên kết Folder với Slot
   POST /api/quiz/slots/{slotId}/folders/{folderId}

6. Tạo StudySet
   POST /api/quiz/study-sets
   → studySetId

7. Thêm StudySet vào Folder
   POST /api/quiz/folders/{folderId}/study-sets/{studySetId}

8. Tạo Quiz trong StudySet
   POST /api/quiz/quizzes
   Body: {studySetId: "..."}
   → quizId

9. Thêm Questions vào Quiz
   POST /api/quiz/questions/{quizId}
   (Lặp lại cho mỗi câu hỏi)
```

---

### 11.2. Học viên làm bài Quiz

```
1. Browse Packages
   GET /api/quiz/packages?type=FREE

2. Chọn Package → Xem Subjects
   GET /api/quiz/subjects/package/{packageId}

3. Chọn Subject → Xem Slots
   GET /api/quiz/slots/subject/{subjectId}

4. Chọn Slot → Xem Folders
   GET /api/quiz/folders/package/{packageId}
   (hoặc filter logic phía client)

5. Chọn Folder → Xem StudySets
   GET /api/quiz/study-sets/folder/{folderId}

6. Chọn StudySet → Xem Quizzes
   GET /api/quiz/quizzes/study-set/{studySetId}

7. Chọn Quiz → Bắt đầu làm bài
   GET /api/quiz/quizzes/{quizId}/attempt
   (Response: questions không có đáp án, có thể shuffle)

8. Làm bài → Nộp bài
   POST /api/quiz/attempts/submit
   Body: {quizId, answers[]}

9. Nhận kết quả
   Response: {earnedPoints, percentageScore, passed, results[]}
```

---

### 11.3. Teacher quản lý Quiz

```
1. Xem tất cả Quizzes của mình
   GET /api/quiz/quizzes/study-set/{studySetId}

2. Chỉnh sửa Quiz
   PUT /api/quiz/quizzes/{id}

3. Thêm/Sửa/Xóa Questions
   POST /api/quiz/questions/{quizId}
   PUT /api/quiz/questions/{questionId}
   DELETE /api/quiz/questions/{questionId}

4. Preview Quiz
   GET /api/quiz/quizzes/{id}
   (Xem đầy đủ kể cả đáp án)

5. Test Quiz như học viên
   GET /api/quiz/quizzes/{id}/attempt
   POST /api/quiz/attempts/submit
```

---

## 12. BUSINESS RULES

### 12.1. Validation Rules
- **Package:** `name` bắt buộc, `type` phải là enum hợp lệ
- **Subject:** Phải thuộc một Package
- **Slot:** Phải thuộc một Subject
- **Folder:** Có thể public hoặc private
- **StudySet:** Có thể thuộc nhiều Folders
- **Quiz:** Phải thuộc một StudySet
- **QuizQuestion:** 
  - MULTIPLE_CHOICE: Cần ít nhất 2 options, ít nhất 1 correct
  - FILL_IN_BLANK: Cần sentenceTemplate và ít nhất 1 blank
  - MATCHING_PAIRS: Cần ít nhất 2 pairs
  - SENTENCE_BUILDER: Cần correctSentence và ít nhất 2 chunks

### 12.2. Ownership Rules
- Chỉ owner có thể UPDATE/DELETE Package, Subject, Slot, Folder, StudySet
- Chỉ Quiz owner có thể thêm/sửa/xóa Questions
- Folder privacy: 
  - Public: Ai cũng xem được
  - Private: Chỉ owner xem được

### 12.3. Cascade Delete Rules
- Xóa Package → Xóa tất cả Subjects
- Xóa Subject → Xóa tất cả Slots
- Xóa Folder → Xóa liên kết với StudySets (không xóa StudySets)
- Xóa StudySet → Xóa tất cả Quizzes
- Xóa Quiz → Xóa tất cả QuizQuestions
- Xóa QuizQuestion → Xóa tất cả Options/Blanks/Pairs/Chunks

---

## 13. NOTES & BEST PRACTICES

### 13.1. Common Mistakes
1. **Gửi `typeId` thay vì `type`**
   - ❌ `"typeId": "01ABC..."`
   - ✅ `"type": "FREE"`

2. **Thiếu Bearer Token**
   - Header: `Authorization: Bearer <token>`

3. **Quên validate ownership**
   - Luôn check `createdBy == userId` trước khi UPDATE/DELETE

4. **Questions validation**
   - MULTIPLE_CHOICE: Phải có `options[]`
   - FILL_IN_BLANK: Phải có `sentenceTemplate` và `blanks[]`
   - Không được mix fields của các QuestionType khác nhau

### 13.2. Performance Tips
1. Sử dụng pagination cho list endpoints (chưa implement)
2. Cache Package types (không thay đổi thường xuyên)
3. Index trên `userId`, `packageId`, `studySetId`, `quizId`

### 13.3. Security Considerations
1. Luôn validate ownership trước khi sửa/xóa
2. Ẩn đáp án khi gọi `/quizzes/{id}/attempt`
3. Rate limiting cho submit quiz (tránh spam)
4. Validate timeLimit phía server khi chấm bài

---

## Tài liệu tham khảo
- Postman Collection: `repo-quiz.postman_collection.json`
- API Base URL: `http://localhost:8080/api/quiz`
- JWT Token: Lấy từ repo-identity `/auth/login`

---

**Version:** 1.0  
**Last Updated:** 2026-02-14  
**Author:** AI Assistant
