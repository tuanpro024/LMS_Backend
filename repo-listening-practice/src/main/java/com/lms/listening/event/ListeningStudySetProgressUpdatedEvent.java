package com.lms.listening.event;

import com.lms.listening.entity.ListeningStudySetProgress;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ListeningStudySetProgressUpdatedEvent extends ApplicationEvent {
    private final ListeningStudySetProgress progress;

    public ListeningStudySetProgressUpdatedEvent(Object source, ListeningStudySetProgress progress) {
        super(source);
        this.progress = progress;
    }
}
