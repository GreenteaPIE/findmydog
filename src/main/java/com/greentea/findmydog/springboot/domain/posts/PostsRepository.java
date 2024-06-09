package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.user.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostsRepository extends JpaRepository<Posts, Long> {
    @Query("SELECT p FROM Posts p ORDER BY p.id DESC")
    List<Posts> findAllDesc();

    List<Posts> findByUser(Users user);

    Page<Posts> findByTitleContaining(String title, Pageable pageable);
}
