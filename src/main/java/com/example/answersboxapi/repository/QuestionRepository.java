package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.entity.QuestionEntity;
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

    @Query(value = "SELECT * " +
            "FROM question " +
            "         INNER JOIN question_details ON question.id = question_details.question_id " +
            "AND (COALESCE (:tagIds, null) IS NULL OR question_details.tag_id IN :tagIds) " +
            "WHERE (:deletedFlag IS NULL OR (:deletedFlag = TRUE OR :deletedFlag = FALSE AND question.deleted_at IS NULL)) " +
            "AND (title LIKE %:searchParam% OR question.description LIKE %:searchParam%) ", nativeQuery = true)
    Page<QuestionEntity> findAll(final Pageable pageable,
                                 @Param("tagIds") final List<UUID> tagIds,
                                 @Param("searchParam") final String searchParam,
                                 @Param("deletedFlag") final boolean deletedFlag);

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

    @Query(value = " SELECT question " +
            "FROM QuestionEntity question " +
            "WHERE question.id IN :ids")
    Page<QuestionEntity> searchByListIds(@Param("ids") final List<UUID> questionIds, final Pageable pageable);
}
