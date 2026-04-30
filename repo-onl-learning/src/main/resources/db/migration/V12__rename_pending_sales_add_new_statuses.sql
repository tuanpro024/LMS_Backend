-- V12__rename_pending_sales_add_new_statuses.sql
-- Đổi PENDING_SALES → PENDING, thêm IN_PROGRESS và CANCELED.

-- 1. Đổi tên status PENDING_SALES → PENDING
UPDATE lead_registration SET status = 'PENDING' WHERE status = 'PENDING_SALES';

-- 2. Cập nhật default và comment
ALTER TABLE lead_registration
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT 'Trạng thái đăng ký: PENDING | IN_PROGRESS | COMPLETED | CANCELED';
