package com.example.answersboxapi.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "answer")
public class AnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false, updatable = false)
    private UUID id;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @ManyToOne
    @JoinColumn (name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "answerId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QuestionDetailsEntity> questionDetails;
}
