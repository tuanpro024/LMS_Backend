-- V9__add_registration_status_to_lead.sql
-- Thêm trường trạng thái đăng ký vào bảng lead_registration

ALTER TABLE lead_registration
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SALES'
        COMMENT 'Trạng thái đăng ký: PENDING_SALES | APPROVED | REJECTED'
        AFTER note,
    ADD COLUMN approved_at DATETIME NULL
        COMMENT 'Thời điểm được kích hoạt quyền học'
        AFTER status,
    ADD COLUMN approved_by VARCHAR(26) NULL
        COMMENT 'userId admin/manager đã kích hoạt'
        AFTER approved_at;

-- Index hỗ trợ guard check timetable: "user có ít nhất 1 APPROVED lead không?"
ALTER TABLE lead_registration
    ADD INDEX idx_status (status),
    ADD INDEX idx_user_status (user_id, status);
