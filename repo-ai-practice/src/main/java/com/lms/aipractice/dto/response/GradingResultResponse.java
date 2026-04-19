package com.lms.aipractice.dto.response;

import com.lms.aipractice.entity.enums.GradingJobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class GradingResultResponse {
    private String jobId;
    private String providerJobId;
    private String errorMessage;

    private String answerId;
    private String itemId;
    private GradingJobStatus jobStatus;

    // ── Populated when job is COMPLETED ───────────────────────────────

    /** Raw normalized score (actual points earned, e.g. 22 out of 30) */
    private Double normalizedScore;

    /** Max possible score for this question (e.g. 30 for picture_paragraph) */
    private Double normalizedMaxScore;

    /** Level label: Cao / Trung bình / Thấp / Đúng / Sai */
    private String normalizedLevel;

    /** JSON array of deduction objects */
    private String deductionsJson;

    /** Vietnamese feedback text shown to student */
    private String feedbackText;

    /** ASR transcript (for speaking / audio_compare subtypes) */
    private String transcriptText;

    /** Full analytics JSON (rubric_breakdown, required_words_check, etc.) */
    private String analyticsJson;

    private Instant completedAt;

    // ── Writing result display fields (extracted from analyticsJson) ───

    /**
     * Bài làm mẫu của AI.
     * Populated for: picture_paragraph, short_paragraph.
     * Maps to HSK_API response field: model_answer
     */
    private String modelAnswer;

    /**
     * Câu đã được sửa lỗi (sentence_arrangement, hanzi_writing).
     * Maps to HSK_API response field: corrected_answer (alias corrected_sentence)
     */
    private String correctedAnswer;

    /**
     * Đoạn văn đã sửa (short_paragraph, picture_paragraph).
     * Maps to HSK_API response field: corrected_paragraph
     */
    private String correctedParagraph;

    /**
     * Bài tóm tắt đã sửa (summary_writing).
     * Maps to HSK_API response field: corrected_summary
     */
    private String correctedSummary;

    /**
     * Đáp án đúng (dùng cho sentence_arrangement — binary scoring).
     * Maps to HSK_API response field: correct_answer
     */
    private String correctAnswer;

    /**
     * Bài làm đúng hay sai (sentence_arrangement binary scoring).
     * Maps to HSK_API response field: is_correct
     */
    private Boolean isCorrect;

    /** Số ký tự trong bài làm của học sinh */
    private Integer characterCount;

    /** true nếu độ dài nằm trong khoảng yêu cầu */
    private Boolean characterCountValid;

    /** Ghi chú về độ dài bài làm */
    private String characterCountNote;

    /**
     * Giải thích quy tắc ngữ pháp cho câu bị sai.
     * Maps to HSK_API response field: grammar_explanation
     */
    private String grammarExplanation;

    /**
     * Kết quả kiểm tra từ bắt buộc (short_paragraph).
     * Parsed from analyticsJson.required_words_check as a Map for easy FE access.
     */
    private Map<String, Object> requiredWordsCheck;

    /**
     * Kết quả kiểm tra tiêu đề (summary_writing).
     * Parsed from analyticsJson.title_check
     */
    private Map<String, Object> titleCheck;

    /**
     * Các ý chính bị thiếu so với bài gốc (summary_writing).
     * Parsed from analyticsJson.missing_source_points
     */
    private List<String> missingSourcePoints;
}
