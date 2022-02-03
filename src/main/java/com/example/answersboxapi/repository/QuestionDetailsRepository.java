package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.QuestionDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionDetailsRepository extends JpaRepository<QuestionDetailsEntity, UUID> {

    @Query(value = "INSERT INTO question_details(question_id, tag_id) VALUES (:questionId, :tagId)", nativeQuery = true)
    QuestionDetailsEntity create(@Param("questionId") final UUID questionId, @Param("tagId") final UUID tagId);

    @Query(value = "SELECT * FROM question_details WHERE question_id = :questionId", nativeQuery = true)
    List<QuestionDetailsEntity> findAllByQuestionId(@Param("questionId") final UUID questionId);

    @Modifying
    @Query(value = "DELETE FROM question_details WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") final UUID id);
}
