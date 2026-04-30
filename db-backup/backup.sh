#!/bin/sh
# =============================================================================
# LMS MySQL Scheduled Backup Script
# Thực hiện mysqldump từng DB riêng lẻ → lưu .sql.gz có timestamp
# Chạy bởi crond bên trong container (unsync / fire-and-forget)
# =============================================================================

set -e

# ── Config (override qua env vars trong docker-compose) ──────────────────────
MYSQL_HOST="${MYSQL_HOST:-mysql}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"
BACKUP_DIR="${BACKUP_DIR:-/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"

# Danh sách databases cần backup (space-separated)
DATABASES="${DATABASES:-lms_identity_db lms_flashcard_db lms_writing_db lms_dictionary_db lms_kanji_origin_db lms_pronunciation_db lms_quiz_db lms_learning_path_db lms_onl_learning_db lms_notification_db lms_media_db lms_listening_db lms_video_course_db lms_ai_practice_db lms_payment_db}"

# ── Helpers ──────────────────────────────────────────────────────────────────
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_PREFIX="[LMS-BACKUP][${TIMESTAMP}]"

log()  { echo "${LOG_PREFIX} $*"; }
warn() { echo "${LOG_PREFIX} WARN: $*" >&2; }
fail() { echo "${LOG_PREFIX} ERROR: $*" >&2; exit 1; }

# ── Tạo thư mục backup ───────────────────────────────────────────────────────
mkdir -p "${BACKUP_DIR}"

# ── Kiểm tra MySQL có sẵn sàng chưa ─────────────────────────────────────────
log "Checking MySQL connectivity at ${MYSQL_HOST}:${MYSQL_PORT} ..."
WAIT=0
until mysqladmin ping -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" --silent 2>/dev/null; do
  WAIT=$((WAIT + 1))
  if [ ${WAIT} -ge 30 ]; then
    fail "MySQL không phản hồi sau 30 giây, hủy backup."
  fi
  log "Đợi MySQL... (${WAIT}s)"
  sleep 1
done
log "MySQL sẵn sàng."

# ── Backup từng database ─────────────────────────────────────────────────────
SUCCESS_COUNT=0
FAIL_COUNT=0

for DB in ${DATABASES}; do
  OUTFILE="${BACKUP_DIR}/${DB}_${TIMESTAMP}.sql.gz"

  log "Bắt đầu backup: ${DB} → ${OUTFILE}"

  # Kiểm tra database có tồn tại không
  DB_EXISTS=$(mysql -h"${MYSQL_HOST}" -P"${MYSQL_PORT}" -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" \
    -e "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='${DB}';" \
    --skip-column-names 2>/dev/null)

  if [ -z "${DB_EXISTS}" ]; then
    warn "Database '${DB}' không tồn tại, bỏ qua."
    continue
  fi

  # Thực hiện dump và nén ngay (pipe)
  if mysqldump \
      -h"${MYSQL_HOST}" \
      -P"${MYSQL_PORT}" \
      -u"${MYSQL_USER}" \
      -p"${MYSQL_PASSWORD}" \
      --single-transaction \
      --routines \
      --triggers \
      --set-gtid-purged=OFF \
      --column-statistics=0 \
      "${DB}" \
    | gzip -9 > "${OUTFILE}"; then

    SIZE=$(du -sh "${OUTFILE}" | cut -f1)
    log "✓ Backup thành công: ${DB} (${SIZE})"
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
  else
    warn "✗ Backup thất bại: ${DB}"
    rm -f "${OUTFILE}"
    FAIL_COUNT=$((FAIL_COUNT + 1))
  fi
done

# ── Dọn dẹp backup cũ ────────────────────────────────────────────────────────
log "Xóa backup cũ hơn ${RETENTION_DAYS} ngày..."
find "${BACKUP_DIR}" -name "*.sql.gz" -mtime "+${RETENTION_DAYS}" -exec rm -f {} \; -print | \
  while read -r f; do log "  Đã xóa: ${f}"; done

# ── Tổng kết ─────────────────────────────────────────────────────────────────
log "======================================="
log "Hoàn tất backup lúc $(date '+%Y-%m-%d %H:%M:%S')"
log "  Thành công : ${SUCCESS_COUNT} databases"
log "  Thất bại   : ${FAIL_COUNT} databases"
log "======================================="

# Trả về exit code lỗi nếu có bất kỳ DB nào fail
[ "${FAIL_COUNT}" -eq 0 ] || exit 1
