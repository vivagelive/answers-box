package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, UUID> {

    @Query(value = "SELECT * FROM answer WHERE question_id = :questionId AND (:isAdmin = true OR deleted_at IS NULL);", nativeQuery = true)
    List<AnswerEntity> findAnswersByQuestionId(@Param("questionId") final UUID questionId, @Param("isAdmin") final boolean isAdmin);
}
