package com.lms.pronunciation.entity;

import com.lms.content.common.entity.BaseContentItem;
import com.lms.pronunciation.entity.enums.PronunciationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pronunciation_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationItem extends BaseContentItem {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PronunciationType type;

    @Column(nullable = false)
    private String symbol;  // b, p, m, f, a, o, e...

    private String pinyin;  // /bù/

    private String hanzi;   // 不

    @Column(columnDefinition = "TEXT")
    private String pronunciationGuide;  // Hướng dẫn cách phát âm

    @Column(name = "mouth_image_url", length = 500)
    private String mouthImageUrl;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    private String exampleWord;

    private String examplePinyin;

    private String exampleMeaning;
}
