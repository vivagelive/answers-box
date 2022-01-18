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
@Table(name = "tag_details")
public class TagDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private QuestionEntity questionId;

    @ManyToOne
    @JoinColumn(name = "tagId", nullable = false)
    private TagEntity tagId;
}
