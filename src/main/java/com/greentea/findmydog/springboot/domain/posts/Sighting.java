package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.BaseTimeEntity;
import com.greentea.findmydog.springboot.domain.user.Users;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 목격 제보: 다른 사람이 "여기서 봤어요" 하고 남기는 기록. 연락처는 글쓴이에게만 보인다. */
@Getter
@NoArgsConstructor
@Entity
public class Sighting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false)
    private LocalDateTime seenAt;

    @Column(nullable = false, length = 100)
    private String place;

    private Double latitude;
    private Double longitude;

    @Column(length = 500)
    private String note;

    private String contact;

    @Builder
    public Sighting(Posts post, Users user, LocalDateTime seenAt, String place, Double latitude, Double longitude, String note, String contact) {
        this.post = post;
        this.user = user;
        this.seenAt = seenAt;
        this.place = place;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.contact = contact;
    }

    public boolean isWrittenBy(Long userId) {
        return user.getId().equals(userId);
    }
}
