# Hướng dẫn tích hợp Event Progress với Learning Path

Tài liệu này hướng dẫn cách các service (Flashcard, Quiz, Writing, Listening, ...) phát event tiến độ học tập để đồng bộ với module Learning Path.

## 1. Mục tiêu
- Đồng bộ tiến độ học tập từ các service nguồn sang Learning Path qua Kafka event.
- Đảm bảo idempotency (mỗi event chỉ xử lý 1 lần).

## 2. Định dạng event chuẩn
- Trường bắt buộc:
  - `eventId` (String): UUID duy nhất cho mỗi event
  - `userId` (String): user thực hiện
  - `studySetId` (String): dùng để map tới module trong Learning Path
  - `occurredAt` (Instant): thời điểm phát sinh event
- Trường nghiệp vụ: tuỳ module (ví dụ: số thẻ đã học, điểm quiz, ...)

## 3. Kafka topic
- Đặt tên theo chuẩn: `<module>.progress.events`
  - Ví dụ: `flashcard.progress.events`, `writing.progress.events`, ...

## 4. Quy trình phát event
- Sau khi cập nhật tiến độ thành công (sau commit DB), phát event bằng `@TransactionalEventListener(AFTER_COMMIT)`
- Sử dụng KafkaTemplate gửi event dạng JSON (key = userId)
- Đảm bảo không phát event nếu transaction rollback

## 5. Quy trình consume ở Learning Path
- Lắng nghe topic Kafka tương ứng
- Parse event, kiểm tra idempotency qua eventId
- Map studySetId vào module tương ứng, cập nhật tiến độ
- Lưu lại eventId đã xử lý để tránh duplicate

## 6. Checklist cho service mới
- [x] Có event record với 4 trường bắt buộc
- [x] Phát event sau commit
- [x] Kafka key = userId
- [x] Cấu hình topic qua env var
- [x] Test duplicate eventId (idempotency)
- [x] Test rollback transaction (không phát event)
- [x] Logging đầy đủ eventId, userId, studySetId

## 7. Tham khảo
- Xem chi tiết mẫu code và hướng dẫn: [LEARNING_PATH_EVENT_INTEGRATION_GUIDE.md](LEARNING_PATH_EVENT_INTEGRATION_GUIDE.md)
- Producer mẫu: repo-flashcard, repo-quiz
- Consumer mẫu: repo-learning-path

---

> **Lưu ý:** Khi tích hợp module mới, cần đảm bảo phát event đúng chuẩn để Learning Path đồng bộ tiến độ chính xác.