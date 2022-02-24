package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, UUID> {

    @Query(value = "SELECT * " +
            "FROM answer " +
            "WHERE question_id = :questionId " +
            "AND (:isAdmin = true OR deleted_at IS NULL);", nativeQuery = true)
    List<AnswerEntity> findAllByQuestionId(@Param("questionId") final UUID questionId, @Param("isAdmin") final boolean isAdmin);

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
}
