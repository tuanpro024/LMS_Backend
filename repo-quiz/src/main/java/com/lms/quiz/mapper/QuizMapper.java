package com.lms.quiz.mapper;

import com.lms.quiz.dto.response.QuestionResponse;
import com.lms.quiz.dto.response.QuizDetailResponse;
import com.lms.quiz.dto.response.QuizResponse;
import com.lms.quiz.entity.*;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "studySetId", source = "studySet.id")
    @Mapping(target = "totalQuestions", expression = "java(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)")
    QuizResponse toQuizResponse(Quiz quiz);

    @Mapping(target = "studySetId", source = "studySet.id")
    @Mapping(target = "totalQuestions", expression = "java(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)")
    @Mapping(target = "totalPoints", expression = "java(calculateTotalPoints(quiz))")
    @Mapping(target = "questions", source = "questions", qualifiedByName = "mapQuestionsForAdmin")
    QuizDetailResponse toQuizDetailResponse(Quiz quiz);

    @Mapping(target = "studySetId", source = "studySet.id")
    @Mapping(target = "totalQuestions", expression = "java(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)")
    @Mapping(target = "totalPoints", expression = "java(calculateTotalPoints(quiz))")
    @Mapping(target = "questions", source = "questions", qualifiedByName = "mapQuestionsForAttempt")
    QuizDetailResponse toQuizDetailResponseForAttempt(Quiz quiz);

    @Named("mapQuestionsForAdmin")
    default List<QuestionResponse> mapQuestionsForAdmin(List<QuizQuestion> questions) {
        if (questions == null)
            return Collections.emptyList();
        return questions.stream()
                .map(this::mapQuestionForAdmin)
                .collect(Collectors.toList());
    }

    @Named("mapQuestionsForAttempt")
    default List<QuestionResponse> mapQuestionsForAttempt(List<QuizQuestion> questions) {
        if (questions == null)
            return Collections.emptyList();
        return questions.stream()
                .map(this::mapQuestionForAttempt)
                .collect(Collectors.toList());
    }

    default QuestionResponse mapQuestionForAdmin(QuizQuestion q) {
        QuestionResponse.QuestionResponseBuilder builder = QuestionResponse.builder()
                .id(q.getId())
                .questionIndex(q.getQuestionIndex())
                .questionType(q.getQuestionType())
                .questionText(q.getQuestionText())
                .questionMediaUrl(q.getQuestionMediaUrl())
                .points(q.getPoints())
                .difficulty(q.getDifficulty());

        switch (q.getQuestionType()) {
            case MULTIPLE_CHOICE -> builder.options(
                    q.getOptions().stream()
                            .map(o -> QuestionResponse.OptionInfo.builder()
                                    .id(o.getId())
                                    .optionIndex(o.getOptionIndex())
                                    .content(o.getContent())
                                    .mediaUrl(o.getMediaUrl())
                                    .build())
                            .collect(Collectors.toList()));
            case FILL_IN_BLANK -> {
                builder.fillBlankMode(q.getFillBlankMode());
                builder.sentenceTemplate(q.getSentenceTemplate());
                builder.blanks(q.getBlanks().stream()
                        .map(b -> QuestionResponse.BlankInfo.builder()
                                .blankIndex(b.getBlankIndex())
                                .hint(b.getHint())
                                .build())
                        .collect(Collectors.toList()));
                if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                    builder.options(q.getOptions().stream()
                            .map(o -> QuestionResponse.OptionInfo.builder()
                                    .id(o.getId())
                                    .optionIndex(o.getOptionIndex())
                                    .content(o.getContent())
                                    .build())
                            .collect(Collectors.toList()));
                }
            }
            case MATCHING_PAIRS -> builder.pairInfos(
                    q.getMatchingPairs().stream()
                            .map(p -> QuestionResponse.PairInfo.builder()
                                    .id(p.getId())
                                    .prompt(p.getPrompt())
                                    .promptMediaUrl(p.getPromptMediaUrl())
                                    .answer(p.getAnswer())
                                    .answerMediaUrl(p.getAnswerMediaUrl())
                                    .build())
                            .collect(Collectors.toList()));
            case SENTENCE_BUILDER -> {
                builder.translationHint(q.getTranslationHint());
                builder.chunks(q.getSentenceChunks().stream()
                        .map(c -> QuestionResponse.ChunkInfo.builder()
                                .id(c.getId())
                                .content(c.getContent())
                                .isDistractor(c.getIsDistractor())
                                .build())
                        .collect(Collectors.toList()));
            }
        }

        return builder.build();
    }

    /**
     * Map question for attempt - same as admin but hides correct answers.
     * FE will shuffle options/pairs/chunks on their side.
     */
    default QuestionResponse mapQuestionForAttempt(QuizQuestion q) {
        // Same as admin mapping — answer correctness is already hidden in
        // OptionInfo/BlankInfo/ChunkInfo
        return mapQuestionForAdmin(q);
    }

    default int calculateTotalPoints(Quiz quiz) {
        if (quiz.getQuestions() == null)
            return 0;
        return quiz.getQuestions().stream()
                .mapToInt(q -> q.getPoints() != null ? q.getPoints() : 1)
                .sum();
    }
}
