package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.QuestionDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionDetailsRepository extends JpaRepository<QuestionDetailsEntity, UUID> {
}
