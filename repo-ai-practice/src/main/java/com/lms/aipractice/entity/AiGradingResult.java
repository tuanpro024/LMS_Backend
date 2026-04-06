package com.lms.aipractice.entity;

import com.lms.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Normalized grading result derived from raw AI response.
 * Stores structured data for UI display and statistics.
 * Raw response is preserved in AiGradingJob.responsePayloadJson.
 */
@Entity
@Table(name = "ai_grading_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiGradingResult extends BaseEntity {

    @Column(name = "grading_job_id", nullable = false, length = 26, unique = true)
    private String gradingJobId;

    /** Normalized score on a 0–100 scale */
    @Column(name = "normalized_score")
    private Double normalizedScore;

    /** Max possible score for this question (from AI response max_score / max_score_per_question) */
    @Column(name = "normalized_max_score")
    private Double normalizedMaxScore;

    /** Level label: Cao / Trung bình / Thấp / Đúng / Sai */
    @Column(name = "normalized_level", length = 30)
    private String normalizedLevel;

    /** JSON array of deduction objects [{category, description, deduction}] */
    @Column(name = "deductions_json", columnDefinition = "TEXT")
    private String deductionsJson;

    /** Vietnamese feedback text shown to student */
    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    /** ASR transcript (for speaking/audio subtypes) */
    @Column(name = "transcript_text", columnDefinition = "TEXT")
    private String transcriptText;

    /**
     * JSON with subtype-specific analytics:
     * - Writing: rubric_breakdown, required_words_check, title_check, character_count
     * - Speaking: content_analysis, hesitation_count, speaking_duration_sec
     * - Audio: character_comparison, tone_score, initials_score, vowels_score
     */
    @Column(name = "analytics_json", columnDefinition = "TEXT")
    private String analyticsJson;
}
