package com.lms.writing.entity;

import com.lms.common.jpa.BaseEntityLongId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Type extends BaseEntityLongId {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TypeName name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
