package com.lms.flashcard.config;

import com.lms.flashcard.entity.Type;
import com.lms.flashcard.entity.TypeName;
import com.lms.flashcard.repository.TypeRepository;
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

        // Check if types already exist
        long count = typeRepository.count();
        if (count > 0) {
            log.info("Types already initialized. Count: {}", count);
            return;
        }

        // Create and save all types
        Arrays.stream(TypeName.values())
                .forEach(typeName -> {
                    Type type = Type.builder()
                            .name(typeName)
                            .build();
                    typeRepository.save(type);
                    log.info("Created type: {}", typeName);
                });

        log.info("Type initialization completed. Total types: {}", TypeName.values().length);
    }
}
