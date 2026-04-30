# =============================================================================
# LMS DB Backup Container
# Base: alpine + mysql-client + dcron (BusyBox cron)
# =============================================================================
FROM alpine:3.19

LABEL maintainer="LMS Team"
LABEL description="Scheduled MySQL backup service (mysqldump + gzip, unsync)"

# Cài mysql-client, gzip, bash
RUN apk add --no-cache \
      mysql-client \
      gzip \
      bash \
      tzdata \
  && cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime \
  && echo "Asia/Ho_Chi_Minh" > /etc/timezone

# Tạo thư mục backup
RUN mkdir -p /backups

# Copy script backup
COPY backup.sh /usr/local/bin/backup.sh
RUN chmod +x /usr/local/bin/backup.sh

# Copy crontab
COPY crontab /etc/cron.d/lms-backup

# Đảm bảo crontab đúng permission (crond của BusyBox yêu cầu 0600)
RUN chmod 0600 /etc/cron.d/lms-backup \
  && crontab /etc/cron.d/lms-backup

VOLUME ["/backups"]

# Chạy cron ở foreground (-f) và log ra stdout (-l 2)
CMD ["crond", "-f", "-l", "2"]
