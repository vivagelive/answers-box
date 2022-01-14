package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    @Query(value = "SELECT EXISTS(SELECT * FROM tag WHERE name = :name)", nativeQuery = true)
    boolean findByName(@Param("name") final String name);
}
