package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.entity.TagEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {

    @Query(value = "SELECT question " +
            "FROM QuestionEntity question " +
            "         INNER JOIN QuestionDetailsEntity question_details ON question.id = question_details.questionId " +
            "AND (COALESCE (:tagIds, null) IS NULL OR question_details.tagId IN :tagIds) " +
            "WHERE (:deletedFlag IS NULL OR :deletedFlag = TRUE AND question.deletedAt IS NOT NULL " +
            "           OR :deletedFlag = FALSE AND question.deletedAt IS NULL) " +
            "AND (question.title LIKE %:searchParam% OR question.description LIKE %:searchParam%) ")
    Page<QuestionEntity> findAll(@Param("tagIds") final List<TagEntity> tagIds,
                                 @Param("searchParam") final String searchParam,
                                 @Param("deletedFlag") final boolean deletedFlag,
                                 final Pageable pageable);

    @Query(value = "SELECT * " +
            "FROM question " +
            "WHERE id = :id " +
            "AND deleted_at IS NULL", nativeQuery = true)
    Optional<QuestionEntity> findById(@Param("id") final UUID id);

    @Modifying
    @Query(value = "UPDATE question " +
            "SET deleted_at = NOW() " +
            "WHERE  id = :id", nativeQuery = true)
    void deleteById(@Param("id") final UUID id);
}
