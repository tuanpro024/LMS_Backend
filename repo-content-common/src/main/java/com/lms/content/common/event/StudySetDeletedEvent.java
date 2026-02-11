package com.lms.content.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StudySetDeletedEvent extends ApplicationEvent {
    private final String studySetId;

    public StudySetDeletedEvent(Object source, String studySetId) {
        super(source);
        this.studySetId = studySetId;
    }
}
