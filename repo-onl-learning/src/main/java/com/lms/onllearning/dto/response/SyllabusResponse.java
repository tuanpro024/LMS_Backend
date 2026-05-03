package com.lms.onllearning.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Maps list item từ CMS API: GET /api/erp/syllabus
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SyllabusResponse(
    String id,
    String code,
    String name,
    @JsonProperty("hsk_level") String hskLevel,
    String type,
    String status,
    String author,
    @JsonProperty("created_at") @JsonDeserialize(using = CmsInstantDeserializer.class) Instant createdAt,
    @JsonProperty("updated_at") @JsonDeserialize(using = CmsInstantDeserializer.class) Instant updatedAt
) {
    public static class CmsInstantDeserializer extends JsonDeserializer<Instant> {
    private static final ZoneId CMS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
        private static final DateTimeFormatter CMS_LOCAL_DATE_TIME_WITH_MICROS =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        private static final DateTimeFormatter CMS_LOCAL_DATE_TIME =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws java.io.IOException {
            String text = p.getValueAsString();
            if (text == null || text.isBlank()) {
                return null;
            }

            String value = text.trim();
            try {
                return Instant.parse(value);
            } catch (Exception ignored) {
            }
            try {
                return OffsetDateTime.parse(value).toInstant();
            } catch (Exception ignored) {
            }
            try {
                return ZonedDateTime.parse(value).toInstant();
            } catch (Exception ignored) {
            }
            try {
                return LocalDateTime.parse(value, CMS_LOCAL_DATE_TIME_WITH_MICROS)
                        .atZone(CMS_ZONE)
                        .toInstant();
            } catch (Exception ignored) {
            }
            try {
                return LocalDateTime.parse(value, CMS_LOCAL_DATE_TIME)
                        .atZone(CMS_ZONE)
                        .toInstant();
            } catch (Exception ignored) {
            }

            throw ctxt.weirdStringException(value, Instant.class,
                    "Unsupported CMS timestamp format for syllabus response");
        }
    }
}
