package com.lms.quiz.service.impl;

import com.lms.content.common.entity.StudySet;
import com.lms.content.common.mapper.StudySetMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.content.common.service.impl.StudySetServiceImpl;
import com.lms.quiz.repository.QuizAttemptRepository;
import com.lms.quiz.repository.QuizRepository;
import com.lms.quiz.repository.UserQuizProgressRepository;
import com.lms.quiz.repository.UserQuizStudySetProgressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quiz-specific override của StudySetServiceImpl.
 * <p>
 * Mục đích chính: Xóa các bản ghi quiz và progress thuộc về {@code StudySet}
 * trước khi xóa {@code StudySet} để tránh vi phạm FK constraint.
 * <p>
 * Thứ tự xóa (đảm bảo không vi phạm FK):
 * <ol>
 *   <li>{@code quiz_attempts}            — tham chiếu study_set_id (plain column)</li>
 *   <li>{@code user_quiz_progress}       — tham chiếu study_set_id (plain column)</li>
 *   <li>{@code user_quiz_study_set_progress}</li>
 *   <li>{@code quizzes}                  — FK study_set_id → study_sets.id</li>
 *   <li>{@code quiz_questions}, {@code quiz_options}, ... — tự cascade từ Quiz</li>
 * </ol>
 */
@Service
@Primary
@Slf4j
@Transactional
public class QuizStudySetServiceImpl extends StudySetServiceImpl {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserQuizProgressRepository userQuizProgressRepository;
    private final UserQuizStudySetProgressRepository userQuizStudySetProgressRepository;

    public QuizStudySetServiceImpl(
            StudySetRepository studySetRepository,
            FolderRepository folderRepository,
            PackageRepository packageRepository,
            StudySetMapper studySetMapper,
            ApplicationEventPublisher eventPublisher,
            QuizRepository quizRepository,
            QuizAttemptRepository quizAttemptRepository,
            UserQuizProgressRepository userQuizProgressRepository,
            UserQuizStudySetProgressRepository userQuizStudySetProgressRepository) {
        super(studySetRepository, folderRepository, packageRepository, studySetMapper, eventPublisher);
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.userQuizProgressRepository = userQuizProgressRepository;
        this.userQuizStudySetProgressRepository = userQuizStudySetProgressRepository;
    }

    @Override
    protected void beforeDeleteStudySet(StudySet entity, String userId) {
        String studySetId = entity.getId();
        log.info("[QuizStudySetServiceImpl] Deleting quiz data for studySetId={}", studySetId);

        // 1. Xóa quiz attempts (tham chiếu study_set_id và quiz_id)
        quizAttemptRepository.deleteByStudySetId(studySetId);

        // 2. Xóa user quiz progress per-quiz
        userQuizProgressRepository.deleteByStudySetId(studySetId);

        // 3. Xóa user quiz study set progress
        userQuizStudySetProgressRepository.deleteByStudySetId(studySetId);

        // 4. Xóa quizzes (FK study_set_id → study_sets.id)
        //    QuizQuestion, QuizOption, QuizBlank, MatchingPair, SentenceChunk
        //    được tự xóa cascade (CascadeType.ALL trên Quiz)
        quizRepository.deleteByStudySetId(studySetId);

        log.info("[QuizStudySetServiceImpl] Deleted quiz data for studySetId={}", studySetId);
    }
}
