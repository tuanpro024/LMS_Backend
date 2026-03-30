package com.lms.writing.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportRow;
import com.lms.content.common.repository.*;
import com.lms.content.common.service.impl.AbstractHierarchicalImportService;
import com.lms.writing.entity.Word;
import com.lms.writing.repository.WordRepository;
import com.lms.writing.util.CharacterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Hierarchical import service for writing module.
 * Hierarchy: Package -&gt; Folder -&gt; StudySet -&gt; Word
 */
@Service
@Slf4j
public class WritingHierarchicalImportService extends AbstractHierarchicalImportService<Word> {

    private final WordRepository wordRepository;

    public WritingHierarchicalImportService(
            PackageRepository packageRepository,
            FolderRepository folderRepository,
            StudySetRepository studySetRepository,
            TypeRepository typeRepository,
            WordRepository wordRepository) {
        super(packageRepository, folderRepository, studySetRepository, typeRepository);
        this.wordRepository = wordRepository;
    }

    @Override
    protected Word createContentItem(HierarchicalImportRow row, int index) {
        String termValue = row.getTerm() != null ? row.getTerm().trim() : "";
        String charactersJson = row.getCharactersJson();

        // Generate characters JSON if not provided
        if (charactersJson == null || charactersJson.trim().isEmpty()) {
            charactersJson = CharacterUtils.wordToJsonArray(termValue);
        }

        Word word = new Word();
        word.setWord(termValue);
        word.setMeaning(row.getDefinition() != null ? row.getDefinition().trim() : "");
        word.setPinyin(row.getPinyin() != null ? row.getPinyin().trim() : "");
        word.setSinoVn(row.getSinoVn() != null ? row.getSinoVn().trim() : null);
        word.setWordType(row.getWordType() != null ? row.getWordType().trim() : null);
        word.setHskLevel(row.getHskLevel() != null ? row.getHskLevel().trim() : null);
        word.setImageWord(row.getImageWord() != null ? row.getImageWord().trim() : null);
        word.setSinoOrigin(row.getSinoOrigin() != null ? row.getSinoOrigin().trim() : null);
        word.setImageOrigin(row.getImageOrigin() != null ? row.getImageOrigin().trim() : null);
        word.setAudio(row.getAudio() != null ? row.getAudio().trim() : null);
        word.setExample(row.getExampleSentence() != null ? row.getExampleSentence().trim() : null);
        word.setExamplePinyin(row.getExamplePinyin() != null ? row.getExamplePinyin().trim() : null);
        word.setExampleMeaning(row.getExampleMeaning() != null ? row.getExampleMeaning().trim() : null);
        word.setCharacters(charactersJson);
        return word;
    }

    @Override
    protected void saveContentItem(Word item) {
        wordRepository.save(item);
    }

    @Override
    protected String getContentItemTypeName() {
        return "Word";
    }
}
