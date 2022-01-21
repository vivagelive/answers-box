package com.example.answersboxapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_details")
public class QuestionDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false, insertable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity questionId;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private TagEntity tagId;
}
