package com.lavela.pool.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pool_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PoolImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}