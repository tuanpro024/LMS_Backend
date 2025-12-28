package com.lms.common.mongodb;

import com.lms.common.jpa.BaseEntity;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

@Component
public class MongoEventListener extends AbstractMongoEventListener<BaseEntity> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<BaseEntity> event) {
        BaseEntity entity = event.getSource();
        if (entity.getId() == null) {
            // New entity
            entity.onCreate();
        } else {
            // Existing entity being updated
            entity.onUpdate();
        }
    }
}

