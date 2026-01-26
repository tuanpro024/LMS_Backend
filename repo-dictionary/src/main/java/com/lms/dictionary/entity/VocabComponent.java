package com.lms.dictionary.entity;

import com.lms.common.jpa.BaseEntityLongId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vocabulary_components", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"parent_vocab_id", "component_vocab_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabComponent extends BaseEntityLongId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_vocab_id", nullable = false)
    private Vocabulary parentVocab;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_vocab_id", nullable = false)
    private Vocabulary componentVocab;

    @Column(name = "order_index")
    private Integer orderIndex;
}
