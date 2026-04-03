package com.lms.listening.publisher;

import com.lms.listening.event.ListeningStudySetProgressKafkaPayload;

public interface ListeningProgressEventPublisher {
    void publish(ListeningStudySetProgressKafkaPayload event);
}
