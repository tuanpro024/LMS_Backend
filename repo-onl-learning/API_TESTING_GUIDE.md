# Repo Online Learning - API Testing Guide

Tai lieu nay mo ta chi tiet cac API trong repo-onl-learning (bo qua SyllabusSyncController) va huong dan test bang Postman.

## 1. Muc tieu

- Cung cap bo Postman test co script assertion.
- Mo ta ro luong xu ly cua tung API.
- Bao gom role, validate, behavior dac biet (idempotency, rate limit, soft delete).

## 2. File da tao

- Postman collection: onl-learning-api-tests.postman_collection.json
- Huong dan: API_TESTING_GUIDE.md

## 3. Chuan bi he thong

Can khoi dong cac service sau:

1. repo-eureka
2. repo-api-gateway (port 8080)
3. repo-identity
4. repo-onl-learning
5. Redis (cho rate limit + cache)
6. MySQL (schema lms_onl_learning_db)

Base URL su dung trong collection:

- Gateway: {{gatewayUrl}} = http://localhost:8080
- Online-learning route: {{onlBase}} = {{gatewayUrl}}/api/online-learning
- Identity route: {{identityBase}} = {{gatewayUrl}}/api/id

## 4. Thu tu chay collection

1. 0. Authentication
2. 1. Syllabus Public APIs
3. 2. Course Public APIs
4. 3. Course Admin APIs
5. 4. Lead APIs (Authenticated User)
6. 5. Lead Admin APIs

Collection tu dong luu cac bien:

- adminToken, userToken
- syllabusId, syllabusName
- courseId
- leadId

## 5. Mo ta chi tiet tung nhom API

## 5.1. Authentication

### POST /api/id/auth/login

Muc dich:
- Lay JWT cho admin va user test.

Test script:
- Assert HTTP 200.
- Lay token tu mot trong cac field: data.accessToken hoac data.token hoac data.jwtToken.
- Luu vao bien collection.

## 5.2. Syllabus Public APIs

### GET /api/online-learning/syllabuses

Controller:
- SyllabusController.getAllSyllabuses

Business flow:
1. Controller goi syllabusService.getAllSyllabuses().
2. Service goi CmsClient.getSyllabuses().
3. Response tra ve theo envelope:
   - data: danh sach syllabus
   - meta: source (CMS/CACHE), isStale, cmsUnavailable, scheduled

Test script:
- Assert 200, success = true.
- Kiem tra data co dang CmsEnvelope.
- Neu co item dau tien thi luu syllabusId, syllabusName de test tiep.

### GET /api/online-learning/syllabuses/{id}

Controller:
- SyllabusController.getSyllabusDetail

Business flow:
1. Controller nhan syllabusId tu path.
2. Service goi CmsClient.getSyllabusDetail(id).
3. Tra ve chi tiet syllabus + schedule + gradingStructure trong CmsEnvelope.

Test script:
- Assert 200.
- Kiem tra envelope co data + meta.

## 5.3. Course Public APIs

### GET /api/online-learning/courses

Controller:
- OnlineCourseController.getAll

Business flow:
1. Service query online_course voi deleted = false, order theo createdAt desc.
2. Mapping entity -> OnlineCourseResponse.
3. Tra ve ApiResponse.success=true, data la array.

Test script:
- Assert 200, success=true.
- Kiem tra data la array.
- Neu chua co courseId, luu id phan tu dau tien.

### GET /api/online-learning/courses/{id}

Controller:
- OnlineCourseController.getById

Business flow:
1. Tim course theo id va deleted=false.
2. Neu khong tim thay -> EntityNotFoundException.
3. Tra ve OnlineCourseResponse.

Test script:
- Assert 200.
- Kiem tra data.id ton tai.

### GET /api/online-learning/courses/{id}/full-detail

Controller:
- OnlineCourseController.getFullDetail

Business flow:
1. Lay course tu DB.
2. Lay syllabus detail tu CMS bang syllabusId cua course.
3. Tra ve object gom:
   - course: OnlineCourseResponse
   - syllabus: CmsEnvelope<SyllabusDetailResponse>

Test script:
- Assert 200.
- Kiem tra data.co course va syllabus.

## 5.4. Course Admin APIs

Yeu cau role:
- ADMIN hoac TEACHER_MANAGER cho create/update.
- Delete duoc config strict hon trong SecurityConfig (thuong dung ADMIN).

### POST /api/online-learning/courses

Controller:
- OnlineCourseController.create

Input validation (OnlineCourseRequest):
- name: not blank, max 255.
- syllabusId: max 26.
- price >= 0.
- rating trong [0.0, 5.0] neu co.

Business flow:
1. Validate request body.
2. Tao entity moi.
3. Neu rating null -> mac dinh 0.0.
4. Save DB, tra 201.

Test script:
- Assert 201.
- Kiem tra success=true.
- Luu courseId moi.

### PUT /api/online-learning/courses/{id}

Controller:
- OnlineCourseController.update

Business flow:
1. Tim course theo id (deleted=false).
2. Ghi de cac field theo request.
3. Neu rating null thi giu nguyen rating cu.
4. Save DB, tra 200.

Test script:
- Assert 200.
- Check ten khoa hoc da update.

### DELETE /api/online-learning/courses/{id}

Controller:
- OnlineCourseController.delete

Business flow:
1. Tim course theo id.
2. Khong xoa vat ly, chi set deleted = true (soft delete).
3. Save va tra success=true.

Test script:
- Assert 200.
- Check success=true.

## 5.5. Lead APIs (Authenticated User)

### POST /api/online-learning/leads/register

Controller:
- LeadController.register

Input validation (LeadRegistrationRequest):
- syllabusId: not blank
- syllabusName: not blank
- fullName: 2-100 ky tu
- email: format email
- phone: regex ^(0|+84)[0-9]{8,10}$
- note: max 500

Business flow chi tiet:
1. Lay userId tu JWT (authentication.getName), khong lay tu client.
2. Kiem tra rate limit bang ca IP bucket va user bucket:
   - IP: 10 req/phut
   - User: 5 req/phut
3. Neu vuot nguong -> tra 429 + errorCode=RATE_LIMIT_EXCEEDED + Retry-After=60.
4. Neu pass rate limit:
   - Check duplicate theo (userId, syllabusId).
   - Neu da ton tai: tra lead cu (idempotent), khong tao ban ghi moi.
   - Neu chua co: tao lead moi, sinh id 26 ky tu, save DB.
5. Tra HTTP 201 voi LeadRegistrationResponse.

Test script trong collection:
- Chap nhan 201 hoac 429 (de phu hop khi test lap lai nhieu lan).
- Neu 201: luu leadId.
- Request thu 2 cung payload:
  - Neu 201 thi assert id khong doi (idempotent).
  - Neu 429 thi assert dung error response.

## 5.6. Lead Admin APIs

Yeu cau role:
- ADMIN hoac TEACHER_MANAGER

### GET /api/online-learning/leads

Controller:
- LeadController.getLeads

Query params:
- syllabusId (optional)
- from (yyyy-MM-dd, optional)
- to (yyyy-MM-dd, optional)
- page, size, sort (Spring Pageable)

Business flow:
1. Chuyen from -> from.atStartOfDay.
2. Chuyen to -> to.atTime(LocalTime.MAX).
3. Query DB co dieu kien linh hoat + sap xep theo registeredAt desc.
4. Tra Spring Page<LeadRegistrationResponse>.

Test script:
- Assert 200, success=true.
- Kiem tra data co content va totalElements.

### GET /api/online-learning/leads/export

Controller:
- LeadController.exportExcel

Business flow:
1. Loc du lieu nhu GET /leads.
2. Dung SXSSFWorkbook de streaming du lieu, tranh OOM.
3. Tao file xlsx va tra binary.
4. Header:
   - Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
   - Content-Disposition: attachment; filename="leads_YYYY-MM-DD.xlsx"

Test script:
- Assert 200.
- Check Content-Type la excel.
- Check Content-Disposition co attachment.

## 6. Cac tinh huong loi nen test bo sung

1. Token khong hop le hoac thieu token cho endpoint can auth -> 401/403.
2. Tao course voi rating > 5 -> validation error.
3. Tao lead voi phone sai regex -> validation error.
4. GET course theo id da soft-delete -> not found.
5. Test qua nguong 5 request/phut/user cho /leads/register -> 429.

## 7. Luu y ve route

Collection su dung route qua API Gateway:

- /api/online-learning/** -> repo-onl-learning
- /api/id/** -> repo-identity

Neu ban test truc tiep service (khong qua gateway), can doi base URL theo port cua repo-onl-learning (mac dinh 8095) va path bo prefix gateway.

## 8. Ghi chu pham vi

- Da bo qua hoan toan cac endpoint trong SyllabusSyncController theo yeu cau.
- Collection tap trung vao test API behavior va contract response.
