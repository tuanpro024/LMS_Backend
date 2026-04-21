package com.lms.kanjiorigin.config;

import com.lms.content.common.entity.Type;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TypeRepository typeRepository;
    private final com.lms.kanjiorigin.repository.KanjiOriginRepository kanjiOriginRepository;
    private final com.lms.kanjiorigin.repository.UserKanjiProgressRepository userKanjiProgressRepository;
    private final com.lms.kanjiorigin.repository.KanjiStudySetProgressRepository kanjiStudySetProgressRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) {
        purgeLegacyData();
        initializeTypes();
    }

    private void purgeLegacyData() {
        log.info("Purging legacy soft-deleted data for Kanji Origin module...");
        try {
            userKanjiProgressRepository.purgeSoftDeleted();
            kanjiStudySetProgressRepository.purgeSoftDeleted();
            kanjiOriginRepository.purgeSoftDeleted();
            log.info("Legacy data purge completed successfully.");
        } catch (Exception e) {
            log.warn("Failed to purge legacy data: {}", e.getMessage());
        }
    }

    private void initializeTypes() {
        log.info("Checking and initializing Type data...");

        Arrays.stream(TypeName.values()).forEach(typeName -> {
            if (!typeRepository.existsByName(typeName)) {
                Type type = Type.builder()
                        .name(typeName)
                        .build();
                typeRepository.save(type);
                log.info("Created type: {}", typeName);
            }
        });

        log.info("Type initialization completed. Total types: {}", typeRepository.count());
    }
}
