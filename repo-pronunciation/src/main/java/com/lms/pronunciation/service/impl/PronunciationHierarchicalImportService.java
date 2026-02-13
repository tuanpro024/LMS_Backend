package com.lms.pronunciation.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportRow;

import com.lms.content.common.repository.*;
import com.lms.content.common.service.impl.AbstractHierarchicalImportService;
import com.lms.pronunciation.entity.PronunciationItem;
import com.lms.pronunciation.entity.enums.PronunciationType;
import com.lms.pronunciation.repository.PronunciationItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Hierarchical import service for pronunciation module
 */
@Service
@Slf4j
public class PronunciationHierarchicalImportService extends AbstractHierarchicalImportService<PronunciationItem> {

    private final PronunciationItemRepository pronunciationItemRepository;

    public PronunciationHierarchicalImportService(
            PackageRepository packageRepository,
            SubjectRepository subjectRepository,
            SlotRepository slotRepository,
            FolderRepository folderRepository,
            StudySetRepository studySetRepository,
            TypeRepository typeRepository,
            PronunciationItemRepository pronunciationItemRepository) {
        super(packageRepository, subjectRepository, slotRepository,
                folderRepository, studySetRepository, typeRepository);
        this.pronunciationItemRepository = pronunciationItemRepository;
    }

    @Override
    protected PronunciationItem createContentItem(HierarchicalImportRow row, int index) {
        PronunciationItem item = new PronunciationItem();
        item.setContentIndex(index);

        // Map term -> symbol (the main identifier for pronunciation)
        item.setSymbol(row.getTerm() != null ? row.getTerm().trim() : "");

        // Map definition -> pronunciationGuide
        item.setPronunciationGuide(row.getDefinition() != null ? row.getDefinition().trim() : null);

        // Map pinyin
        item.setPinyin(row.getPinyin() != null ? row.getPinyin().trim() : null);

        // Map sinoVn -> hanzi
        item.setHanzi(row.getSinoVn() != null ? row.getSinoVn().trim() : null);

        // Map wordType -> PronunciationType
        if (row.getWordType() != null && !row.getWordType().trim().isEmpty()) {
            try {
                item.setType(PronunciationType.valueOf(row.getWordType().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown PronunciationType: {}, defaulting to INITIAL", row.getWordType());
                item.setType(PronunciationType.SHENGMU);
            }
        } else {
            item.setType(PronunciationType.SHENGMU);
        }

        // Map imageWord -> mouthImageUrl
        item.setMouthImageUrl(row.getImageWord() != null ? row.getImageWord().trim() : null);

        // Map audio -> audioUrl
        item.setAudioUrl(row.getAudio() != null ? row.getAudio().trim() : null);

        // Map example fields
        item.setExampleWord(row.getExampleSentence() != null ? row.getExampleSentence().trim() : null);
        item.setExamplePinyin(row.getExamplePinyin() != null ? row.getExamplePinyin().trim() : null);
        item.setExampleMeaning(row.getExampleMeaning() != null ? row.getExampleMeaning().trim() : null);

        return item;
    }

    @Override
    protected void saveContentItem(PronunciationItem item) {
        pronunciationItemRepository.save(item);
    }

    @Override
    protected String getContentItemTypeName() {
        return "PronunciationItem";
    }
}
