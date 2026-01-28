package com.lms.flashcard.service.impl;

import com.lms.content.common.dto.excel.HierarchicalImportRow;
import com.lms.content.common.dto.excel.ImportableContentItem;
import com.lms.content.common.repository.*;
import com.lms.content.common.service.impl.AbstractHierarchicalImportService;
import com.lms.flashcard.entity.Card;
import com.lms.flashcard.repository.CardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Hierarchical import service for flashcard module
 */
@Service
@Slf4j
public class FlashcardHierarchicalImportService extends AbstractHierarchicalImportService<Card> {

    private final CardRepository cardRepository;

    public FlashcardHierarchicalImportService(
            PackageRepository packageRepository,
            SubjectRepository subjectRepository,
            SlotRepository slotRepository,
            FolderRepository folderRepository,
            StudySetRepository studySetRepository,
            TypeRepository typeRepository,
            CardRepository cardRepository) {
        super(packageRepository, subjectRepository, slotRepository,
                folderRepository, studySetRepository, typeRepository);
        this.cardRepository = cardRepository;
    }

    @Override
    protected Card createContentItem(HierarchicalImportRow row, int index) {
        Card card = new Card();
        card.setCardIndex(index);
        card.setTerm(row.getTerm() != null ? row.getTerm().trim() : "");
        card.setDefinition(row.getDefinition() != null ? row.getDefinition().trim() : "");
        card.setPinyin(row.getPinyin() != null ? row.getPinyin().trim() : null);
        card.setSinoVn(row.getSinoVn() != null ? row.getSinoVn().trim() : null);
        card.setWordType(row.getWordType() != null ? row.getWordType().trim() : null);
        card.setHskLevel(row.getHskLevel() != null ? row.getHskLevel().trim() : null);
        card.setImageWord(row.getImageWord() != null ? row.getImageWord().trim() : null);
        card.setSinoOrigin(row.getSinoOrigin() != null ? row.getSinoOrigin().trim() : null);
        card.setImageOrigin(row.getImageOrigin() != null ? row.getImageOrigin().trim() : null);
        card.setAudio(row.getAudio() != null ? row.getAudio().trim() : null);
        card.setExampleSentence(row.getExampleSentence() != null ? row.getExampleSentence().trim() : null);
        card.setExamplePinyin(row.getExamplePinyin() != null ? row.getExamplePinyin().trim() : null);
        card.setExampleMeaning(row.getExampleMeaning() != null ? row.getExampleMeaning().trim() : null);
        card.setCharacters(row.getCharactersJson() != null ? row.getCharactersJson().trim() : null);

        return card;
    }

    @Override
    protected void saveContentItem(Card item) {
        cardRepository.save(item);
    }

    @Override
    protected String getContentItemTypeName() {
        return "Card";
    }
}
