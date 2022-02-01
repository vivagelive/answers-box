package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    boolean existsByName(final String name);

    @Query(value = "SELECT * FROM tag WHERE id = :tagId", nativeQuery = true)
    Optional<TagEntity> findById(@Param("tagId") final UUID tagId);
}
