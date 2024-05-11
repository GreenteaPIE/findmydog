package com.greentea.findmydog.springboot.domain.posts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByPost(Posts post);
}
