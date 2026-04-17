package com.lms.kanjiorigin.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportResult;
import com.lms.content.common.dto.excel.HierarchicalImportRow;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.StudySetRepository;
import com.lms.content.common.repository.TypeRepository;
import com.lms.content.common.service.impl.AbstractHierarchicalImportService;
import com.lms.kanjiorigin.dto.excel.KanjiExtraRowData;
import com.lms.kanjiorigin.entity.KanjiOrigin;
import com.lms.kanjiorigin.repository.KanjiOriginRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class KanjiOriginHierarchicalImportService extends AbstractHierarchicalImportService<KanjiOrigin> {

    private static final int[] COL_ORIGIN_TEXT_CN = { 22, 28 };
    private static final int[] COL_ORIGIN_TEXT_EN = { 23, 29 };
    private static final int[] COL_STROKE_ANIMATION_URL = { 24, 30 };
    private static final int[] COL_EXAMPLE_MEANING_VI = { 25, 31 };
    private static final int[] COL_EXAMPLE_MEANING_EN = { 26, 32 };

    private static final String H_ORIGIN_TEXT_CN = "origintextcn";
    private static final String H_ORIGIN_TEXT_EN = "origintexten";
    private static final String H_STROKE_ANIMATION_URL = "strokeanimationurl";
    private static final String H_EXAMPLE_MEANING_VI = "examplemeaningvi";
    private static final String H_EXAMPLE_MEANING_EN = "examplemeaningen";

    private static final Map<String, List<String>> EXTRA_HEADER_ALIASES = Map.ofEntries(
            Map.entry(H_ORIGIN_TEXT_CN,
                    List.of("origintextcn", "origin_text_cn", "origin cn", "origincntext", "origintextzh")),
            Map.entry(H_ORIGIN_TEXT_EN,
                    List.of("origintexten", "origin_text_en", "origin en", "originentext")),
            Map.entry(H_STROKE_ANIMATION_URL,
                    List.of("strokeanimationurl", "stroke_animation_url", "strokeurl", "animationurl",
                            "strokegifurl")),
            Map.entry(H_EXAMPLE_MEANING_VI,
                    List.of("examplemeaningvi", "example_meaning_vi", "examplevimeaning", "examplemeaningvn")),
            Map.entry(H_EXAMPLE_MEANING_EN,
                    List.of("examplemeaningen", "example_meaning_en", "exampleenmeaning")));

    private final KanjiOriginRepository kanjiOriginRepository;
    private final ThreadLocal<Map<Integer, KanjiExtraRowData>> extrasContext = new ThreadLocal<>();

    public KanjiOriginHierarchicalImportService(
            PackageRepository packageRepository,
            FolderRepository folderRepository,
            StudySetRepository studySetRepository,
            TypeRepository typeRepository,
            KanjiOriginRepository kanjiOriginRepository) {
        super(packageRepository, folderRepository, studySetRepository, typeRepository);
        this.kanjiOriginRepository = kanjiOriginRepository;
    }

    @Override
    @Transactional
    public HierarchicalImportResult importFromPackageExcel(
            MultipartFile file, TypeName typeName, String userId, boolean isPrivate) {
        try {
            extrasContext.set(preParseExtraColumns(file));
            return super.importFromPackageExcel(file, typeName, userId, isPrivate);
        } finally {
            extrasContext.remove();
        }
    }

    @Override
    protected KanjiOrigin createContentItem(HierarchicalImportRow row, int index) {
        KanjiExtraRowData extra = Optional.ofNullable(extrasContext.get())
                .map(map -> map.get(row.getRowNumber()))
                .orElse(null);

        KanjiOrigin origin = new KanjiOrigin();
        origin.setContentIndex(index);
        origin.setTerm(row.getTerm() == null ? "" : row.getTerm().trim());
        origin.setMeaning(trimToNull(row.getDefinition()));
        origin.setPinyin(trimToNull(row.getPinyin()));
        origin.setSinoVn(trimToNull(row.getSinoVn()));
        origin.setOriginTextVi(trimToNull(row.getSinoOrigin()));
        origin.setOriginImage(trimToNull(row.getImageOrigin()));
        origin.setAudioUrl(trimToNull(row.getAudio()));
        origin.setExampleSentence(trimToNull(row.getExampleSentence()));
        origin.setExamplePinyin(trimToNull(row.getExamplePinyin()));
        origin.setExampleMeaning(trimToNull(row.getExampleMeaning()));

        if (extra != null) {
            origin.setOriginTextCn(trimToNull(extra.getOriginTextCn()));
            origin.setOriginTextEn(trimToNull(extra.getOriginTextEn()));
            origin.setStrokeAnimationUrl(trimToNull(extra.getStrokeAnimationUrl()));
            origin.setExampleMeaningVi(trimToNull(extra.getExampleMeaningVi()));
            origin.setExampleMeaningEn(trimToNull(extra.getExampleMeaningEn()));
        }

        return origin;
    }

    @Override
    protected void saveContentItem(KanjiOrigin item) {
        if (item.getStudySet() == null) {
            log.warn("Skipping kanji origin import row with no study set");
            return;
        }

        String studySetId = item.getStudySet().getId();
        Optional<KanjiOrigin> existingOpt = kanjiOriginRepository
                .findByStudySetIdAndTermAndDeletedFalse(studySetId, item.getTerm());

        if (existingOpt.isPresent()) {
            KanjiOrigin existing = existingOpt.get();
            existing.setMeaning(item.getMeaning());
            existing.setPinyin(item.getPinyin());
            existing.setSinoVn(item.getSinoVn());
            existing.setAudioUrl(item.getAudioUrl());
            existing.setStrokeAnimationUrl(item.getStrokeAnimationUrl());
            existing.setOriginImage(item.getOriginImage());
            existing.setOriginTextVi(item.getOriginTextVi());
            existing.setOriginTextCn(item.getOriginTextCn());
            existing.setOriginTextEn(item.getOriginTextEn());
            existing.setExampleSentence(item.getExampleSentence());
            existing.setExampleMeaning(item.getExampleMeaning());
            existing.setExampleMeaningVi(item.getExampleMeaningVi());
            existing.setExampleMeaningEn(item.getExampleMeaningEn());
            existing.setExamplePinyin(item.getExamplePinyin());
            kanjiOriginRepository.save(existing);
            return;
        }

        Integer contentIndex = item.getContentIndex();
        if (contentIndex == null
                || kanjiOriginRepository.existsByStudySetIdAndContentIndexAndDeletedFalse(studySetId, contentIndex)) {
            Integer maxIndex = kanjiOriginRepository.findMaxContentIndexByStudySetId(studySetId);
            contentIndex = maxIndex == null ? 0 : maxIndex + 1;
            item.setContentIndex(contentIndex);
        }

        kanjiOriginRepository.save(item);
    }

    @Override
    protected String getContentItemTypeName() {
        return "KanjiOrigin";
    }

    private Map<Integer, KanjiExtraRowData> preParseExtraColumns(MultipartFile file) {
        Map<Integer, KanjiExtraRowData> map = new HashMap<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            Row headerRow = sheet.getRow(firstRowNum);
            Map<String, Integer> headerIndexMap = buildNormalizedHeaderIndexMap(headerRow);

            int originTextCnIdx = resolveColumnIndex(headerIndexMap, H_ORIGIN_TEXT_CN, COL_ORIGIN_TEXT_CN);
            int originTextEnIdx = resolveColumnIndex(headerIndexMap, H_ORIGIN_TEXT_EN, COL_ORIGIN_TEXT_EN);
            int strokeAnimationUrlIdx = resolveColumnIndex(
                    headerIndexMap,
                    H_STROKE_ANIMATION_URL,
                    COL_STROKE_ANIMATION_URL);
            int exampleMeaningViIdx = resolveColumnIndex(
                    headerIndexMap,
                    H_EXAMPLE_MEANING_VI,
                    COL_EXAMPLE_MEANING_VI);
            int exampleMeaningEnIdx = resolveColumnIndex(
                    headerIndexMap,
                    H_EXAMPLE_MEANING_EN,
                    COL_EXAMPLE_MEANING_EN);

            for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String originTextCn = getCellStr(row, originTextCnIdx);
                String originTextEn = getCellStr(row, originTextEnIdx);
                String strokeUrl = getCellStr(row, strokeAnimationUrlIdx);
                String exMeanVi = getCellStr(row, exampleMeaningViIdx);
                String exMeanEn = getCellStr(row, exampleMeaningEnIdx);

                if (originTextCn != null || originTextEn != null || strokeUrl != null || exMeanVi != null
                        || exMeanEn != null) {
                    map.put(i + 1, KanjiExtraRowData.builder()
                            .originTextCn(originTextCn)
                            .originTextEn(originTextEn)
                            .strokeAnimationUrl(strokeUrl)
                            .exampleMeaningVi(exMeanVi)
                            .exampleMeaningEn(exMeanEn)
                            .build());
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to pre-parse kanji extra columns: {}", ex.getMessage());
        }

        return map;
    }

    private static Map<String, Integer> buildNormalizedHeaderIndexMap(Row headerRow) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        if (headerRow == null) {
            return headerIndexMap;
        }

        short firstCell = headerRow.getFirstCellNum();
        short lastCell = headerRow.getLastCellNum();
        if (firstCell < 0 || lastCell < 0) {
            return headerIndexMap;
        }

        for (int i = firstCell; i < lastCell; i++) {
            String header = getCellStr(headerRow, i);
            if (header == null || header.isBlank()) {
                continue;
            }

            String normalized = normalizeHeader(header);
            if (!normalized.isEmpty()) {
                headerIndexMap.putIfAbsent(normalized, i);
            }
        }

        return headerIndexMap;
    }

    private static int resolveColumnIndex(Map<String, Integer> headerIndexMap, String key, int... fallbacks) {
        List<String> aliases = EXTRA_HEADER_ALIASES.getOrDefault(key, List.of(key));
        for (String alias : aliases) {
            Integer index = headerIndexMap.get(normalizeHeader(alias));
            if (index != null) {
                return index;
            }
        }
        if (fallbacks != null && fallbacks.length > 0) {
            return fallbacks[0];
        }
        return -1;
    }

    private static String normalizeHeader(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String getCellStr(Row row, int colIndex) {
        if (colIndex < 0) {
            return null;
        }

        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> trimToNull(cell.getStringCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    String text = cell.getStringCellValue();
                    yield trimToNull(text);
                } catch (Exception ignored) {
                    try {
                        double v = cell.getNumericCellValue();
                        yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
                    } catch (Exception ignoredAgain) {
                        yield null;
                    }
                }
            }
            default -> null;
        };
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}