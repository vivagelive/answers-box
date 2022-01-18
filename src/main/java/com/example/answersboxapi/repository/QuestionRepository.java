package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {
}
