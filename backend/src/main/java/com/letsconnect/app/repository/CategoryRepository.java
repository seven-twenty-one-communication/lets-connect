package com.letsconnect.app.repository;

import com.letsconnect.app.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    Optional<Category> findBySlug(String slug);
}
