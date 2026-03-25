# Huong dan tich hop Event Progress vao Learning Path

Tai lieu nay la context chung de cac service khac truyen su kien progress ve `repo-learning-path` theo pattern dang dung o `repo-flashcard` va `repo-quiz`.

## 1) Muc tieu va nguyen tac

- Muc tieu: dong bo tien do hoc tap tu service nguon sang Learning Path theo co che event-driven (Kafka).
- Pattern chuan:
  - Service nguon cap nhat progress trong transaction local.
  - Sau khi commit thanh cong moi publish event (`@TransactionalEventListener(AFTER_COMMIT)`).
  - Learning Path consume event, map vao module progress, sau do cap nhat step va learning path progress.
- Bat buoc idempotency:
  - Moi event phai co `eventId` duy nhat (UUID).
  - Consumer luu `eventId` da xu ly de tranh xu ly trung lap.

## 2) Contract event can tuan theo

### 2.1 Truong bat buoc (toi thieu)

- `eventId` (String): UUID duy nhat cho moi lan phat event.
- `userId` (String): user tao tien do.
- `studySetId` (String): dung de map den module trong Learning Path.
- `occurredAt` (Instant): thoi diem phat sinh event tai service nguon.

### 2.2 Truong nghiep vu tuy module

Tuy loai module, service nguon gui them du lieu phuc vu tinh progress.

Vi du hien tai:

- Flashcard event:
  - `learnedCards`, `totalCards`, `progressPercentage`, `completed`
- Quiz event:
  - `attemptId`, `quizId`, `quizTitle`, `attemptsCount`, `earnedPoints`, `totalPoints`, `scorePercentage`, `passed`, `timeTakenSeconds`

Luu y:

- Learning Path dung `@JsonIgnoreProperties(ignoreUnknown = true)` voi DTO event, nen co the them field moi ma khong vo backward compatibility.
- Tuy nhien khong duoc bo cac field bat buoc o muc 2.1.

## 3) Topic Kafka chuan

- Flashcard progress topic mac dinh: `flashcard.progress.events`
- Quiz progress topic mac dinh: `quiz.progress.events`

De mo rong cho service moi, khuyen nghi dat ten:

- `<module>.progress.events`

Vi du:

- `writing.progress.events`
- `listening.progress.events`

## 4) Pattern implement ben service nguon

### Buoc 1: Dinh nghia Domain Event record

- Tao record event (co builder) trong package `event`.
- Co day du field bat buoc + field nghiep vu.

### Buoc 2: Phat event noi bo sau khi save thanh cong

- Trong service xu ly nghiep vu, sau khi save progress, goi:
  - `applicationEventPublisher.publishEvent(...)`

### Buoc 3: Transactional listener AFTER_COMMIT

- Tao listener:
  - `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
- Listener chi lam 1 viec: delegate sang Kafka publisher.

Ly do:

- Tranh publish event khi transaction DB bi rollback.

### Buoc 4: Kafka publisher

- Su dung `KafkaTemplate<String, String>` + `ObjectMapper`.
- Serialize event thanh JSON va send:
  - key: `userId`
  - value: payload JSON
- Bao ve null/blank cho event quan trong (`event`, `userId`).
- Bat exception khi publish, log warn (khong lam sap transaction da commit).

### Buoc 5: Config topic

Trong `application.yml` cua service nguon:

```yaml
<module>:
  progress:
    kafka:
      topic: ${<MODULE>_PROGRESS_KAFKA_TOPIC:<module>.progress.events}
```

## 5) Pattern consume ben Learning Path

### Buoc 1: Listener Kafka

- Tao `@KafkaListener` cho topic module.
- Nhan payload JSON string, parse ve DTO event bang `ObjectMapper`.
- Goi service dong bo progress tu event.

### Buoc 2: Dong bo progress vao module

Trong service dong bo:

- Validate event bat buoc (`eventId`, `userId`, `studySetId`).
- Check idempotency qua bang consumed event:
  - Neu da ton tai `eventId` thi bo qua.
- Tim danh sach `StepModule` theo:
  - `moduleType` tuong ung
  - `contentSetId = studySetId`
  - `isActive = true`
- Cap nhat/tai tao `ModuleProgress` cho user.
- Cap nhat lai `StepProgress` va `LearningPathProgress` lien quan.
- Luu consumed event (`eventId`, `consumedAt`).

### Buoc 3: Bang idempotency

- Tao entity consumed event voi unique constraint tren `event_id`.
- Tao repository `existsByEventId(...)`.

## 6) Mapping moduleType de service moi follow

Khi them service moi (vi du Writing, Listening):

1. Xac dinh `ModuleType` tuong ung trong Learning Path.
2. Dam bao module trong Step da duoc tao voi:
   - `moduleType` dung
   - `contentSetId` chinh la `studySetId` se gui trong event
3. Event tu service moi phai mang dung `studySetId` de map trung module.

## 7) Checklist cho service moi

- [ ] Co event record voi 4 field bat buoc: `eventId`, `userId`, `studySetId`, `occurredAt`.
- [ ] Publish event sau commit (`AFTER_COMMIT`).
- [ ] Kafka message key = `userId`.
- [ ] Co cau hinh topic qua env var.
- [ ] Co test case cho duplicate eventId (idempotency).
- [ ] Co test case rollback transaction (khong phat event).
- [ ] Co logging de trace: eventId, userId, studySetId.

## 8) Tham chieu implementation hien tai

Producer mau:

- `repo-flashcard`:
  - `FlashcardProgressUpdatedEvent`
  - `FlashcardProgressEventListener`
  - `FlashcardProgressEventPublisher`
- `repo-quiz`:
  - `QuizAttemptSubmittedEvent`
  - `QuizAttemptEventListener`
  - `QuizProgressEventPublisher`

Consumer mau:

- `repo-learning-path`:
  - `FlashcardProgressKafkaListener`
  - `QuizProgressKafkaListener`
  - `QuizProgressEvent`
  - `FlashcardProgressEvent`
  - `ConsumedQuizProgressEvent` / `ConsumedFlashcardProgressEvent`

## 9) Khuyen nghi mo rong cho service tiep theo

- Dung chung mot convention ten field va ten topic nhu tren.
- Uu tien bo sung field moi theo huong backward-compatible.
- Neu can retry va dam bao at-least-once, idempotency bang `eventId` la bat buoc.
- Co the bo sung dead-letter topic cho event loi parse JSON hoac loi nghiep vu.

---

Neu can, co the tao them mot file template starter (event record + publisher + listener + yml) de copy/paste cho service moi.
