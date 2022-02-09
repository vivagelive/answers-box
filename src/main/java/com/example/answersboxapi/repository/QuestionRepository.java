package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {

    @Query(value = "SELECT * FROM question WHERE :isAdmin = true OR deleted_at IS NULL", nativeQuery = true)
    Page<QuestionEntity> findAll(final Pageable pageable, @Param("isAdmin") final boolean isAdmin);

    @Query(value = "SELECT * FROM question INNER JOIN question_details " +
                    "ON question.id = question_details.question_id " +
                    "AND question_details.tag_id IN :tagIds", nativeQuery = true)
    Page<QuestionEntity> filteredQuestions(final Pageable pageable, @Param("tagIds") final List<UUID> tagIds);
}
