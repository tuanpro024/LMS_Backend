-- V11__remove_approval_add_completed.sql
-- Bỏ luồng admin duyệt (APPROVED/REJECTED), thêm trạng thái COMPLETED cho đăng ký lại.

-- 1. Thêm cột completed_at
ALTER TABLE lead_registration
    ADD COLUMN completed_at DATETIME NULL
        COMMENT 'Thời điểm hoàn thành khóa học (null nếu chưa COMPLETED)'
        AFTER status;

-- 2. Chuyển các bản ghi APPROVED/REJECTED về PENDING_SALES (dọn dữ liệu cũ)
UPDATE lead_registration SET status = 'PENDING_SALES' WHERE status IN ('APPROVED', 'REJECTED');

-- 3. Cập nhật comment cho cột status
ALTER TABLE lead_registration
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SALES'
        COMMENT 'Trạng thái đăng ký: PENDING_SALES | COMPLETED';

-- 4. Xóa các cột approval không còn dùng
ALTER TABLE lead_registration
    DROP COLUMN approved_at,
    DROP COLUMN approved_by;

-- 5. Xóa index cũ liên quan đến approval guard check
ALTER TABLE lead_registration
    DROP INDEX idx_status,
    DROP INDEX idx_user_status;
