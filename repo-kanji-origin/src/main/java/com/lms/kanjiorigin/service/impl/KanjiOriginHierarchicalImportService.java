package com.lms.kanjiorigin.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.excel.HierarchicalImportRow;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.repository.*;
import com.lms.content.common.service.impl.AbstractHierarchicalImportService;
import com.lms.kanjiorigin.dto.excel.KanjiExtraRowData;
import com.lms.kanjiorigin.entity.*;
import com.lms.kanjiorigin.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * Hierarchical import service for kanji-origin module.
 * Support hierarchy: StudySet -> Lesson -> Kanji (One Lesson has many Kanjis).
 * 
 * Logic:
 * - Lesson data comes from Extra Columns (AA, AB).
 * - Kanji data comes from Standard Columns (M-Z) + Remaining Extra Columns.
 * - Rows with same Lesson Title in same StudySet are merged into one Lesson.
 * - Supports inheritance: If Lesson Title is empty, uses the previous valid
 * lesson title.
 */
@Service
@Slf4j
public class KanjiOriginHierarchicalImportService extends AbstractHierarchicalImportService<KanjiLesson> {

    // Extra column indices (from AA onwards)
    private static final int COL_LESSON_TITLE = 26; // AA
    private static final int COL_LESSON_DESC = 27; // AB
    private static final int COL_ORIGIN_TEXT_CN = 28; // AC
    private static final int COL_ORIGIN_TEXT_EN = 29; // AD
    private static final int COL_STROKE_ANIMATION_URL = 30; // AE
    private static final int COL_EXAMPLE_MEANING_VI = 31; // AF
    private static final int COL_EXAMPLE_MEANING_EN = 32; // AGz
    private static final int COL_QUESTION_CONTENT = 33; // AH
    private static final int COL_CORRECT_ANSWER = 34; // AI
    private static final int COL_WRONG_OPTIONS = 35; // AJ

    private final KanjiLessonRepository kanjiLessonRepository;
    private final KanjiOriginRepository kanjiOriginRepository;
    private final KanjiQuestionRepository kanjiQuestionRepository;
    private final KanjiQuestionWrongOptionRepository kanjiQuestionWrongOptionRepository;
    private final KanjiLessonQuestionRepository kanjiLessonQuestionRepository;

    // Request-scoped context holder
    private final ThreadLocal<ImportContext> contextHolder = new ThreadLocal<>();

    // Keep track of lesson state during import
    private static class ImportContext {
        Map<Integer, KanjiExtraRowData> extraDataMap;
        String lastLessonTitle;
        String lastLessonDesc;

        ImportContext(Map<Integer, KanjiExtraRowData> extraDataMap) {
            this.extraDataMap = extraDataMap;
        }
    }

    public KanjiOriginHierarchicalImportService(
            PackageRepository packageRepository,
            FolderRepository folderRepository,
            StudySetRepository studySetRepository,
            TypeRepository typeRepository,
            KanjiLessonRepository kanjiLessonRepository,
            KanjiOriginRepository kanjiOriginRepository,
            KanjiQuestionRepository kanjiQuestionRepository,
            KanjiQuestionWrongOptionRepository kanjiQuestionWrongOptionRepository,
            KanjiLessonQuestionRepository kanjiLessonQuestionRepository) {
        super(packageRepository, folderRepository, studySetRepository, typeRepository);
        this.kanjiLessonRepository = kanjiLessonRepository;
        this.kanjiOriginRepository = kanjiOriginRepository;
        this.kanjiQuestionRepository = kanjiQuestionRepository;
        this.kanjiQuestionWrongOptionRepository = kanjiQuestionWrongOptionRepository;
        this.kanjiLessonQuestionRepository = kanjiLessonQuestionRepository;
    }

    @Override
    @Transactional
    public HierarchicalImportResult importFromPackageExcel(
            MultipartFile file, TypeName typeName, String userId, boolean isPrivate) {

        try {
            log.info("Pre-parsing extra kanji columns (AA-AJ)...");
            Map<Integer, KanjiExtraRowData> map = preParseExtraColumns(file);
            contextHolder.set(new ImportContext(map));
            log.info("Found extra data for {} rows", map.size());

            return super.importFromPackageExcel(file, typeName, userId, isPrivate);
        } finally {
            contextHolder.remove(); // Clean up thread local
        }
    }

    @Override
    protected KanjiLesson createContentItem(HierarchicalImportRow row, int index) {
        ImportContext ctx = contextHolder.get();
        KanjiExtraRowData extra = (ctx != null && ctx.extraDataMap != null)
                ? ctx.extraDataMap.get(row.getRowNumber())
                : null;

        String lessonTitle = null;
        String lessonDesc = null;

        // Check if current row has explicit lesson title
        if (extra != null && extra.getLessonTitle() != null && !extra.getLessonTitle().isEmpty()) {
            lessonTitle = extra.getLessonTitle();
            lessonDesc = extra.getLessonDescription();

            // Update context with new active lesson
            if (ctx != null) {
                ctx.lastLessonTitle = lessonTitle;
                ctx.lastLessonDesc = lessonDesc;
            }
        }
        // If not, try to use inherited lesson from context
        else if (ctx != null && ctx.lastLessonTitle != null) {
            lessonTitle = ctx.lastLessonTitle;
            lessonDesc = ctx.lastLessonDesc;
        }

        // Fallback: use Term if absolutely no lesson info found anywhere
        if (lessonTitle == null) {
            lessonTitle = row.getTerm() != null ? row.getTerm().trim() : "Untitled Lesson";
            lessonDesc = row.getDefinition();
        }

        // Create Lesson Object (Transient container)
        KanjiLesson lesson = new KanjiLesson();
        lesson.setContentIndex(index);
        lesson.setTitle(lessonTitle);
        lesson.setDescription(lessonDesc);

        // Create KanjiOrigin from Standard Cols + Extra Cols
        KanjiOrigin origin = new KanjiOrigin();
        origin.setTerm(row.getTerm() != null ? row.getTerm().trim() : "");
        origin.setMeaning(row.getDefinition() != null ? row.getDefinition().trim() : null);
        origin.setPinyin(row.getPinyin() != null ? row.getPinyin().trim() : null);
        origin.setSinoVn(row.getSinoVn() != null ? row.getSinoVn().trim() : null);
        origin.setOriginTextVi(row.getSinoOrigin() != null ? row.getSinoOrigin().trim() : null);
        origin.setOriginImage(row.getImageOrigin() != null ? row.getImageOrigin().trim() : null);
        origin.setAudioUrl(row.getAudio() != null ? row.getAudio().trim() : null);
        origin.setExampleSentence(row.getExampleSentence() != null ? row.getExampleSentence().trim() : null);
        origin.setExamplePinyin(row.getExamplePinyin() != null ? row.getExamplePinyin().trim() : null);
        origin.setExampleMeaning(row.getExampleMeaning() != null ? row.getExampleMeaning().trim() : null);

        if (extra != null) {
            origin.setOriginTextCn(extra.getOriginTextCn());
            origin.setOriginTextEn(extra.getOriginTextEn());
            origin.setStrokeAnimationUrl(extra.getStrokeAnimationUrl());
            origin.setExampleMeaningVi(extra.getExampleMeaningVi());
            origin.setExampleMeaningEn(extra.getExampleMeaningEn());
        }

        // Attach to transient fields
        lesson.setTempImportOrigin(origin);
        lesson.setTempExtraData(extra);

        return lesson;
    }

    @Override
    protected void saveContentItem(KanjiLesson item) {
        if (item.getStudySet() == null) {
            log.warn("Skipping item with no StudySet");
            return;
        }

        // 1. Check if Lesson exists in StudySet by Title
        Optional<KanjiLesson> existingOpt = kanjiLessonRepository
                .findByStudySetIdAndTitleAndDeletedFalse(item.getStudySet().getId(), item.getTitle());

        KanjiLesson savedLesson;
        if (existingOpt.isPresent()) {
            savedLesson = existingOpt.get(); // Reuse existing lesson
        } else {
            // New Lesson
            Integer maxIdx = kanjiLessonRepository.findMaxContentIndexByStudySetId(item.getStudySet().getId());
            int newIdx = (maxIdx == null) ? 1 : maxIdx + 1;
            item.setContentIndex(newIdx);
            savedLesson = kanjiLessonRepository.save(item);
        }

        // 2. Save KanjiOrigin
        KanjiOrigin origin = item.getTempImportOrigin();
        if (origin != null) {
            origin.setKanjiLesson(savedLesson);
            origin.setContentIndex(0); // TODO: fetch max index if ordering matters
            kanjiOriginRepository.save(origin);
        }

        // 3. Save Question
        KanjiExtraRowData extra = item.getTempExtraData();
        if (extra != null && extra.hasQuestionData()) {
            saveQuestion(savedLesson, extra);
        }
    }

    @Override
    protected String getContentItemTypeName() {
        return "KanjiLesson";
    }

    // =====================================================
    // Question persistence
    // =====================================================

    private void saveQuestion(KanjiLesson lesson, KanjiExtraRowData extra) {
        String content = extra.getQuestionContent().trim();
        String correctAnswer = extra.getCorrectAnswer() != null ? extra.getCorrectAnswer().trim() : "";

        Optional<KanjiQuestion> existingOpt = kanjiQuestionRepository
                .findByContentAndCorrectAnswerAndDeletedFalse(content, correctAnswer);

        KanjiQuestion question;
        if (existingOpt.isPresent()) {
            question = existingOpt.get();
        } else {
            question = new KanjiQuestion();
            question.setContent(content);
            question.setCorrectAnswer(correctAnswer);
            question.setDeleted(false);
            question = kanjiQuestionRepository.save(question);

            for (String wrongOption : extra.getWrongOptionsList()) {
                KanjiQuestionWrongOption option = new KanjiQuestionWrongOption();
                option.setKanjiQuestion(question);
                option.setWrongOption(wrongOption);
                option.setDeleted(false);
                kanjiQuestionWrongOptionRepository.save(option);
            }
        }

        boolean alreadyLinked = kanjiLessonQuestionRepository
                .existsByKanjiLessonIdAndKanjiQuestionIdAndDeletedFalse(lesson.getId(), question.getId());

        if (!alreadyLinked) {
            Integer maxIdx = kanjiLessonQuestionRepository.findMaxContentIndexByKanjiLessonId(lesson.getId());
            int newIdx = (maxIdx == null) ? 1 : maxIdx + 1;

            KanjiLessonQuestion lq = new KanjiLessonQuestion();
            lq.setKanjiLesson(lesson);
            lq.setKanjiQuestion(question);
            lq.setContentIndex(newIdx);
            lq.setDeleted(false);
            kanjiLessonQuestionRepository.save(lq);
        }
    }

    // =====================================================
    // Pre-parsing extra columns
    // =====================================================

    private Map<Integer, KanjiExtraRowData> preParseExtraColumns(MultipartFile file) {
        Map<Integer, KanjiExtraRowData> map = new HashMap<>();

        try (InputStream is = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                int rowNumber = i + 1;

                String lessonTitle = getCellStr(row, COL_LESSON_TITLE);
                String lessonDesc = getCellStr(row, COL_LESSON_DESC);
                String originTextCn = getCellStr(row, COL_ORIGIN_TEXT_CN);
                String originTextEn = getCellStr(row, COL_ORIGIN_TEXT_EN);
                String strokeUrl = getCellStr(row, COL_STROKE_ANIMATION_URL);
                String exMeanVi = getCellStr(row, COL_EXAMPLE_MEANING_VI);
                String exMeanEn = getCellStr(row, COL_EXAMPLE_MEANING_EN);
                String qContent = getCellStr(row, COL_QUESTION_CONTENT);
                String qCorrect = getCellStr(row, COL_CORRECT_ANSWER);
                String qWrong = getCellStr(row, COL_WRONG_OPTIONS);

                // Even if lessonTitle is null, we might have other extra data
                // We create the DTO anyway.
                if (lessonTitle != null || originTextCn != null || qContent != null) {
                    map.put(rowNumber, KanjiExtraRowData.builder()
                            .lessonTitle(lessonTitle)
                            .lessonDescription(lessonDesc)
                            .originTextCn(originTextCn)
                            .originTextEn(originTextEn)
                            .strokeAnimationUrl(strokeUrl)
                            .exampleMeaningVi(exMeanVi)
                            .exampleMeaningEn(exMeanEn)
                            .questionContent(qContent)
                            .correctAnswer(qCorrect)
                            .wrongOptionsRaw(qWrong)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to pre-parse extra columns: {}", e.getMessage());
        }

        return map;
    }

    private static String getCellStr(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null)
            return null;

        return switch (cell.getCellType()) {
            case STRING -> {
                String val = cell.getStringCellValue();
                yield (val != null && !val.trim().isEmpty()) ? val.trim() : null;
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double v = cell.getNumericCellValue();
                yield (v == Math.floor(v)) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
}
