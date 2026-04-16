package com.lms.aipractice.adapter.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.aipractice.entity.AiPracticeAnswer;
import com.lms.aipractice.entity.AiPracticeItem;
import com.lms.aipractice.entity.enums.AiItemSubtype;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WritingRequestMapperTest {

    private WritingRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WritingRequestMapper(new ObjectMapper());
    }

    @Test
    void buildPayload_mapsKnownWritingSubtypesAndModel() {
        AiPracticeAnswer answer = AiPracticeAnswer.builder().answerText("老师请我们星期天吃饭").build();

        AiPracticeItem sentenceItem = new AiPracticeItem();
        sentenceItem.setQuestionSubtype(AiItemSubtype.SENTENCE_ARRANGEMENT);
        sentenceItem.setPromptText("把下列词语连成完整的句子");
        sentenceItem.setHskLevel(4);

        AiPracticeItem shortParagraphItem = new AiPracticeItem();
        shortParagraphItem.setQuestionSubtype(AiItemSubtype.SHORT_PARAGRAPH);
        shortParagraphItem.setPromptText("写一段短文");
        shortParagraphItem.setHskLevel(5);

        AiPracticeItem summaryItem = new AiPracticeItem();
        summaryItem.setQuestionSubtype(AiItemSubtype.SUMMARY_WRITING);
        summaryItem.setPromptText("写摘要");
        summaryItem.setHskLevel(6);

        Map<String, Object> sentencePayload = mapper.buildPayload(sentenceItem, answer);
        Map<String, Object> shortPayload = mapper.buildPayload(shortParagraphItem, answer);
        Map<String, Object> summaryPayload = mapper.buildPayload(summaryItem, answer);

        assertEquals("sentence_arrangement", sentencePayload.get("question_type"));
        assertEquals("short_paragraph", shortPayload.get("question_type"));
        assertEquals("summary_writing", summaryPayload.get("question_type"));

        assertEquals("deepseek-reasoner", sentencePayload.get("model"));
        assertEquals("老师请我们星期天吃饭", sentencePayload.get("user_input"));
        assertEquals("老师请我们星期天吃饭", sentencePayload.get("student_answer"));
    }

    @Test
    void buildPayload_keepsFallbackSubtypeForLegacyTypes() {
        AiPracticeItem item = new AiPracticeItem();
        item.setQuestionSubtype(AiItemSubtype.PICTURE_SENTENCE);
        item.setPromptText("描述图片");

        AiPracticeAnswer answer = AiPracticeAnswer.builder().answerText("有一个人").build();

        Map<String, Object> payload = mapper.buildPayload(item, answer);

        assertEquals("picture_sentence", payload.get("question_type"));
    }

    @Test
    void buildPayload_parsesRequiredWordsArrayWhenJsonValid() {
        AiPracticeItem item = new AiPracticeItem();
        item.setQuestionSubtype(AiItemSubtype.SHORT_PARAGRAPH);
        item.setPromptText("根据提示词写短文");
        item.setRequiredWordsJson("[\"生日\",\"朋友\",\"蛋糕\",\"快乐\"]");

        AiPracticeAnswer answer = AiPracticeAnswer.builder().answerText("test").build();

        Map<String, Object> payload = mapper.buildPayload(item, answer);

        Object requiredWords = payload.get("required_words");
        assertTrue(requiredWords instanceof List<?>);
        assertEquals(List.of("生日", "朋友", "蛋糕", "快乐"), requiredWords);
    }

    @Test
    void buildPayload_setsRequiredWordsNullWhenJsonInvalid() {
        AiPracticeItem item = new AiPracticeItem();
        item.setQuestionSubtype(AiItemSubtype.SHORT_PARAGRAPH);
        item.setPromptText("根据提示词写短文");
        item.setRequiredWordsJson("not-a-json-array");

        AiPracticeAnswer answer = AiPracticeAnswer.builder().answerText("test").build();

        Map<String, Object> payload = mapper.buildPayload(item, answer);

        assertNull(payload.get("required_words"));
    }
}
