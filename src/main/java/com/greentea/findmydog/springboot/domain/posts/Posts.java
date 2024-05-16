package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.BaseTimeEntity;
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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String kind;

    @Column(length = 500, nullable = false)
    private String title;

    private String author;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "content_id")
    private Content content;

    @Builder
    public Posts(String kind, String title, Content content, String author) {
        this.kind = kind;
        this.title = title;
        this.content = content;
        this.author = author;

    }

    public void update(String title) {
        this.title = title;
    }

    public void addImage(Image image) {
        this.images.add(image);
        if (image.getPost() != this) {
            image.setPost(this);
        }
    }

}