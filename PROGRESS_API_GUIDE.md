# Hướng dẫn API tiến độ hoàn thành các module LMS

## 1. Flashcard
- **API lấy tiến độ học bộ thẻ:**
  - `GET /study-sets/{studySetId}/progress`
  - Trả về tiến độ học của user với bộ thẻ (số thẻ đã học, tổng số thẻ, phần trăm hoàn thành...)
- **Event progress:**
  - Khi user học xong thẻ, service phát event `flashcard.progress.events` lên Kafka để đồng bộ với Learning Path.

## 2. Pronunciation
- **API đánh dấu đã nghe item:**
  - `POST /pronunciation-progress/items/{itemId}/listen`
- **API lấy tiến độ học bộ phát âm:**
  - `GET /pronunciation-progress/study-sets/{studySetId}`
- **API lấy tiến độ từng item:**
  - `GET /pronunciation-progress/study-sets/{studySetId}/items`

## 3. Listening Practice
- **API bắt đầu xem video:**
  - `POST /api/listening-practice/progress/study-sets/{studySetId}/videos/{videoCode}/start`
- **API cập nhật tiến độ xem:**
  - `POST /api/listening-practice/progress/study-sets/{studySetId}/videos/{videoCode}/update`
- **API đánh dấu hoàn thành:**
  - `POST /api/listening-practice/progress/study-sets/{studySetId}/videos/{videoCode}/complete`
- **API lấy tiến độ video:**
  - `GET /api/listening-practice/progress/study-sets/{studySetId}/videos/{videoCode}`
- **API lấy tiến độ bộ video:**
  - `GET /api/listening-practice/progress/study-sets/{studySetId}`
- **Event progress:**
  - Khi user hoàn thành video, phát event `listening.progress.events` lên Kafka.

## 4. Writing
- **API lấy từ đã học/chưa học:**
  - `GET /study-sets/{id}/words/learned`
  - `GET /study-sets/{id}/words/unlearned`
- **API cập nhật trạng thái từ:**
  - Khi user học từ, cập nhật trạng thái và phát event `writing.progress.events` lên Kafka.

## 5. Video Course
- **API cập nhật tiến độ xem video module:**
  - `POST /progress/watch` (body: moduleId, watchedSeconds, duration)
  - Khi đạt >=80% sẽ tự động hoàn thành module
- **API lấy tiến độ module:**
  - `GET /progress/modules/{MODULE_ID}`
- **API lấy tiến độ step:**
  - `GET /progress/steps/{STEP_ID}`
- **API lấy tiến độ course:**
  - `GET /progress/courses/{COURSE_ID}`

## 6. Learning Path
- **API bắt đầu module:**
  - `POST /user/progress/module/{moduleId}/start`
- **API cập nhật tiến độ module:**
  - `POST /user/progress/module/{moduleId}/update`
- **API hoàn thành module:**
  - `POST /user/progress/module/{moduleId}/complete`

## 7. Event Progress tích hợp với Learning Path
- Các service (Flashcard, Quiz, Writing, Listening, ...) phát event lên Kafka với các trường bắt buộc: `eventId`, `userId`, `studySetId`, `occurredAt`.
- Learning Path consume event, map vào module tương ứng để cập nhật tiến độ.
- Tham khảo chi tiết: [LEARNING_PATH_EVENT_INTEGRATION_GUIDE.md](LEARNING_PATH_EVENT_INTEGRATION_GUIDE.md)

---

> **Lưu ý:**
> - Các API đều yêu cầu xác thực JWT.
> - Để lấy tiến độ, luôn truyền đúng userId (lấy từ token).
> - Khi tích hợp module mới, cần phát event progress theo chuẩn để đồng bộ với Learning Path.