package com.example.answersboxapi.repository;

import com.example.answersboxapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmail(final String email);

    @Query(value = "SELECT EXISTS(SELECT * FROM users WHERE id = :id AND deleted_at IS NULL)", nativeQuery = true)
    boolean existsById(@Param("id") final UUID id);

    @Query(value = "SELECT * FROM users WHERE email = :email AND deleted_at IS NULL;", nativeQuery = true)
    Optional<UserEntity> findByEmail(@Param("email") final String email);

    @Modifying
    @Query(value = "UPDATE users SET deleted_at = NOW() WHERE id = :id ;", nativeQuery = true)
    void deleteById(@Param("id") final UUID id);
}
