package com.lavela.pool.repository;

import com.lavela.pool.domain.entity.Pool;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Long> {

    @EntityGraph(attributePaths = "images")
    List<Pool> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "images")
    List<Pool> findByStatusOrderByNameAsc(String status);

    @EntityGraph(attributePaths = "images")
    Optional<Pool> findByIdAndStatus(Long id, String status);
}