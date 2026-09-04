package com.syfe.pfm.repository;

import com.syfe.pfm.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.userId = :userId OR c.userId IS NULL")
    List<Category> findAllByUserIdOrSystem(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.userId = :userId OR c.userId IS NULL)")
    Optional<Category> findByNameAndUserIdOrSystem(@Param("name") String name, @Param("userId") Long userId);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND (c.userId = :userId OR c.userId IS NULL)")
    boolean existsByNameAndUserIdOrSystem(@Param("name") String name, @Param("userId") Long userId);

    Optional<Category> findByNameAndUserIdAndIsCustomTrue(String name, Long userId);
}
