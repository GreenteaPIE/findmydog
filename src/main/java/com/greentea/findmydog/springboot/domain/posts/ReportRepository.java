package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByPostAndUser(Posts post, Users user);

    long countByPost(Posts post);

    /** 신고가 하나라도 있는 글 (최근 신고 순) */
    @Query("SELECT r.post FROM Report r GROUP BY r.post ORDER BY MAX(r.id) DESC")
    List<Posts> findReportedPosts();

    List<Report> findByPostOrderByIdDesc(Posts post);
}
