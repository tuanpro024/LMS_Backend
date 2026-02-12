package com.lms.quiz.mapper;

import com.lms.quiz.dto.request.CreateQuestionRequest;
import com.lms.quiz.entity.*;
import com.lms.quiz.entity.enums.DifficultyLevel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Manual mapper to convert CreateQuestionRequest → QuizQuestion with
 * sub-entities.
 */
@Component
public class QuestionMapper {

    public QuizQuestion toEntity(CreateQuestionRequest request, Quiz quiz, int questionIndex) {
        QuizQuestion question = QuizQuestion.builder()
                .quiz(quiz)
                .questionIndex(questionIndex)
                .questionType(request.getQuestionType())
                .questionText(request.getQuestionText())
                .questionMediaUrl(request.getQuestionMediaUrl())
                .explanation(request.getExplanation())
                .points(request.getPoints() != null ? request.getPoints() : 1)
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : DifficultyLevel.MEDIUM)
                .fillBlankMode(request.getFillBlankMode())
                .sentenceTemplate(request.getSentenceTemplate())
                .correctSentence(request.getCorrectSentence())
                .translationHint(request.getTranslationHint())
                .options(new ArrayList<>())
                .blanks(new ArrayList<>())
                .matchingPairs(new ArrayList<>())
                .sentenceChunks(new ArrayList<>())
                .build();

        // Map sub-entities based on question type
        switch (request.getQuestionType()) {
            case MULTIPLE_CHOICE -> mapOptions(request.getOptions(), question);
            case FILL_IN_BLANK -> {
                mapBlanks(request.getBlanks(), question);
                if (request.getOptions() != null) {
                    mapOptions(request.getOptions(), question);
                }
            }
            case MATCHING_PAIRS -> mapMatchingPairs(request.getMatchingPairs(), question);
            case SENTENCE_BUILDER -> mapSentenceChunks(request.getSentenceChunks(), question);
        }

        return question;
    }

    private void mapOptions(List<CreateQuestionRequest.OptionData> options, QuizQuestion question) {
        if (options == null)
            return;
        IntStream.range(0, options.size()).forEach(i -> {
            CreateQuestionRequest.OptionData data = options.get(i);
            QuizOption option = QuizOption.builder()
                    .question(question)
                    .optionIndex(i)
                    .content(data.getContent())
                    .mediaUrl(data.getMediaUrl())
                    .isCorrect(data.getIsCorrect() != null ? data.getIsCorrect() : false)
                    .build();
            question.getOptions().add(option);
        });
    }

    private void mapBlanks(List<CreateQuestionRequest.BlankData> blanks, QuizQuestion question) {
        if (blanks == null)
            return;
        blanks.forEach(data -> {
            QuizBlank blank = QuizBlank.builder()
                    .question(question)
                    .blankIndex(data.getBlankIndex() != null ? data.getBlankIndex() : 0)
                    .correctAnswer(data.getCorrectAnswer())
                    .acceptedAnswers(data.getAcceptedAnswers())
                    .hint(data.getHint())
                    .build();
            question.getBlanks().add(blank);
        });
    }

    private void mapMatchingPairs(List<CreateQuestionRequest.MatchingPairData> pairs, QuizQuestion question) {
        if (pairs == null)
            return;
        pairs.forEach(data -> {
            MatchingPair pair = MatchingPair.builder()
                    .question(question)
                    .prompt(data.getPrompt())
                    .answer(data.getAnswer())
                    .promptMediaUrl(data.getPromptMediaUrl())
                    .answerMediaUrl(data.getAnswerMediaUrl())
                    .build();
            question.getMatchingPairs().add(pair);
        });
    }

    private void mapSentenceChunks(List<CreateQuestionRequest.ChunkData> chunks, QuizQuestion question) {
        if (chunks == null)
            return;
        chunks.forEach(data -> {
            SentenceChunk chunk = SentenceChunk.builder()
                    .question(question)
                    .content(data.getContent())
                    .correctPosition(data.getCorrectPosition() != null ? data.getCorrectPosition() : 0)
                    .isDistractor(data.getIsDistractor() != null ? data.getIsDistractor() : false)
                    .build();
            question.getSentenceChunks().add(chunk);
        });
    }
}
