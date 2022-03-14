package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.AnswerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, UUID> {

    @Query(value = "SELECT answer " +
            "FROM AnswerEntity answer " +
            "WHERE (answer.question.id = :questionId " +
            "AND answer.text LIKE %:searchParam%) " +
            "AND (:deletedFlag IS NULL OR :deletedFlag = TRUE " +
            "      OR :deletedFlag = FALSE AND answer.deletedAt IS NULL) ")
    List<AnswerEntity> findAllByQuestionId(@Param("questionId") final UUID questionId,
                                           @Param("searchParam") final String searchParam,
                                           @Param("deletedFlag") final boolean deletedFlag);

    @Modifying
    @Query(value = "UPDATE answer " +
            "SET deleted_at = NOW() " +
            "WHERE question_id = :questionId", nativeQuery = true)
    void deleteAllByQuestionId(@Param("questionId") final UUID questionId);

    @Query(value = "SELECT EXISTS(" +
            "SELECT * " +
            "FROM answer " +
            "WHERE question_id = :questionId " +
            "AND deleted_at IS NULL)", nativeQuery = true)
    boolean existsByQuestionId(final UUID questionId);

    @Modifying
    @Query(value = "UPDATE answer " +
            "SET deleted_at = NOW() " +
            "WHERE id = :id " +
            "AND deleted_at IS NULL", nativeQuery = true)
    void deleteById(@Param("id") final UUID id);

    @Query(value = "SELECT answer " +
            "FROM AnswerEntity answer " +
            "WHERE answer.id IN :ids")
    Page<AnswerEntity> searchByListIds(@Param("ids") final List<UUID> ids, final Pageable pageable);
}
