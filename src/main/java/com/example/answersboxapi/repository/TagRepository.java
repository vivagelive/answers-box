package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    boolean existsByName(final String name);
}
