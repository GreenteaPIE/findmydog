package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.BaseTimeEntity;
import com.greentea.findmydog.springboot.domain.user.Users;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 게시글 신고. 한 사람은 한 글에 한 번만 신고할 수 있다. */
@Getter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false, length = 30)
    private String reason;

    @Column(length = 300)
    private String detail;

    @Builder
    public Report(Posts post, Users user, String reason, String detail) {
        this.post = post;
        this.user = user;
        this.reason = reason;
        this.detail = detail;
    }
}
