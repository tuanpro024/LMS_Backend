package com.lms.quiz.config;

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

        long count = typeRepository.count();
        if (count > 0) {
            log.info("Types already initialized. Count: {}", count);
            return;
        }

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
