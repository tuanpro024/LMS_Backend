package com.lms.pronunciation.publisher;

import com.lms.pronunciation.event.PronunciationStudySetProgressKafkaPayload;

public interface PronunciationProgressEventPublisher {
    void publishProgressUpdatedEvent(PronunciationStudySetProgressKafkaPayload payload);
}
