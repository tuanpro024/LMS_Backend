package com.lms.aipractice.entity;

import com.lms.content.common.entity.BaseContentItem;
import com.lms.aipractice.entity.enums.AiItemSubtype;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents one AI-graded practice question inside a StudySet.
 * Extends BaseContentItem which already provides: id, study_set_id,
 * content_index, createdAt, updatedAt, deleted.
 * Note: No @Builder — MapStruct uses setter-based mapping which handles parent
 * class fields correctly.
 */
@Entity
@Table(name = "ai_practice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiPracticeItem extends BaseContentItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "question_subtype", nullable = false, length = 50)
    private AiItemSubtype questionSubtype;

    /** HSK level: 3, 4, 5, 6 */
    @Column(name = "hsk_level")
    private Integer hskLevel;

    /** Speaking level: elementary, intermediate, advanced (null for writing) */
    @Column(name = "part_level", length = 20)
    private String partLevel;

    /** Main question text or instruction shown to student */
    @Column(name = "prompt_text", columnDefinition = "TEXT")
    private String promptText;

    /** For picture_sentence, picture_paragraph, speaking_picture_description */
    @Column(name = "image_description", columnDefinition = "TEXT")
    private String imageDescription;

    /**
     * JSON array of required words for SHORT_PARAGRAPH.
     * Example: ["尽管","影响","坚持"]
     */
    @Column(name = "required_words_json", columnDefinition = "TEXT")
    private String requiredWordsJson;

    /**
     * Reference/model answer for SENTENCE_ARRANGEMENT, SPEAKING_LISTEN_AND_ANSWER
     */
    @Column(name = "reference_answer", columnDefinition = "TEXT")
    private String referenceAnswer;

    /**
     * Key points summary of the source article for SUMMARY_WRITING
     * (original_article_summary)
     */
    @Column(name = "original_article_summary", columnDefinition = "TEXT")
    private String originalArticleSummary;

    /**
     * Reference text for AUDIO_COMPARE (reference_text sent to HSK_API audio
     * compare API)
     */
    @Column(name = "reference_text", columnDefinition = "TEXT")
    private String referenceText;

    /**
     * Multimedia file id for teacher-provided listening audio
     * (SPEAKING_LISTEN_AND_ANSWER).
     */
    @Column(name = "question_audio_file_id", length = 64)
    private String questionAudioFileId;

    /**
     * Multimedia file id for teacher-provided image
     * (PICTURE_SENTENCE/PICTURE_PARAGRAPH).
     */
    @Column(name = "question_image_file_id", length = 64)
    private String questionImageFileId;

    /** Extra configuration JSON for future extensibility */
    @Column(name = "extra_config_json", columnDefinition = "TEXT")
    private String extraConfigJson;
}
