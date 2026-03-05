package com.lms.videocourse.config;

import com.lms.content.common.entity.Type;
import com.lms.content.common.entity.TypeName;
import com.lms.content.common.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Seeds the database with required lookup data on application startup.
 * Specifically seeds the Type table with all TypeName enum values.
 * Uses existsByName check to avoid duplicate inserts on restart.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TypeRepository typeRepository;

    @Override
    public void run(String... args) {
        seedTypes();
    }

    /**
     * Seeds all TypeName enum values into the types table.
     * Idempotent — safe to run on every startup.
     */
    private void seedTypes() {
        log.info("Seeding types...");

        Arrays.stream(TypeName.values()).forEach(typeName -> {
            if (!typeRepository.existsByName(typeName)) {
                Type type = Type.builder()
                        .name(typeName)
                        .description(getTypeDescription(typeName))
                        .build();
                typeRepository.save(type);
                log.info("Created type: {}", typeName);
            }
        });

        log.info("Types seeding completed. Total types: {}", typeRepository.count());
    }

    private String getTypeDescription(TypeName typeName) {
        return switch (typeName) {
            case FREE -> "Tự do - Học tập tự do không theo lộ trình cụ thể";
            case LEARNING_PATH -> "Ôn luyện - Lộ trình học tập có cấu trúc";
            case VIDEO_COURSE -> "Video khóa học - Khóa học video trực tuyến";
            case LEARNING -> "Học 1-1, 1-n - Học tập cá nhân hoặc nhóm";
        };
    }
}
