package com.lms.aipractice.config;

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

    @Override
    public void run(String... args) {
        initializeTypes();
    }

    private void initializeTypes() {
        log.info("Checking and initializing Type data...");

        int createdCount = 0;
        for (TypeName typeName : Arrays.asList(TypeName.values())) {
            if (typeRepository.existsByName(typeName)) {
                continue;
            }

            Type type = Type.builder()
                    .name(typeName)
                    .build();
            typeRepository.save(type);
            createdCount++;
            log.info("Created missing type: {}", typeName);
        }

        if (createdCount == 0) {
            log.info("All types already initialized. Total enum types: {}", TypeName.values().length);
            return;
        }

        log.info("Type initialization completed. Created {}, total enum types: {}",
                createdCount, TypeName.values().length);
    }
}