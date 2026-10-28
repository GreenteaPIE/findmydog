package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SightingRepository extends JpaRepository<Sighting, Long> {
    List<Sighting> findByPostOrderBySeenAtDesc(Posts post);

    long countByUserAndCreatedDateAfter(Users user, LocalDateTime after);
}
