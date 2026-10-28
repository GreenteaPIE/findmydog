package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.BaseTimeEntity;
import com.greentea.findmydog.springboot.domain.user.Users;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Posts extends BaseTimeEntity {

    public static final String KIND_LOST = "분실";
    public static final String KIND_FOUND = "발견";
    public static final String STATUS_LOOKING = "L";  // 찾고 있어요
    public static final String STATUS_REUNITED = "F"; // 가족을 찾았어요

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Image> images = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String kind;

    @Column(length = 500, nullable = false)
    private String title;

    private String author;

    private String status = STATUS_LOOKING;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "content_id")
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Builder
    public Posts(String kind, String title, Content content, String author, Users user) {
        this.kind = kind;
        this.title = title;
        this.content = content;
        this.author = author;
        this.user = user;
        this.status = STATUS_LOOKING;
    }

    public void update(String kind, String title) {
        this.kind = kind;
        this.title = title;
    }

    public void changeStatus(String status) {
        if (!STATUS_LOOKING.equals(status) && !STATUS_REUNITED.equals(status)) {
            throw new IllegalArgumentException("알 수 없는 상태입니다: " + status);
        }
        this.status = status;
    }

    public boolean isOwnedBy(Long userId) {
        return user != null && userId != null && user.getId().equals(userId);
    }

    public boolean isReunited() {
        return STATUS_REUNITED.equals(status);
    }

    public void addImage(Image image) {
        this.images.add(image);
        if (image.getPost() != this) {
            image.setPost(this);
        }
    }

    public void setUser(Users user) {
        this.user = user;
        user.getPosts().add(this);
    }

}
