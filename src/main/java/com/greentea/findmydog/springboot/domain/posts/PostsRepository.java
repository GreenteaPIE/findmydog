package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PostsRepository extends JpaRepository<Posts, Long>, JpaSpecificationExecutor<Posts> {
    @Query("SELECT p FROM Posts p WHERE p.hidden = false ORDER BY p.id DESC")
    List<Posts> findAllVisibleDesc();

    List<Posts> findByUser(Users user);

    List<Posts> findByUserOrderByIdDesc(Users user);

    List<Posts> findTop8ByHiddenFalseOrderByIdDesc();

    long countByStatusAndHiddenFalse(String status);

    long countByUserAndCreatedDateAfter(Users user, LocalDateTime after);
}
