package com.lms.flashcard.publisher;

import com.lms.flashcard.event.FlashcardStudySetProgressKafkaPayload;

public interface FlashcardProgressEventPublisher {
    void publishProgressUpdatedEvent(FlashcardStudySetProgressKafkaPayload payload);
}
