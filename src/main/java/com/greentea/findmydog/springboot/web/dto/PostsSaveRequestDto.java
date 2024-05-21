package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.user.Users;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsSaveRequestDto {
    private String kind;
    private String title;
    private ContentDto content;
    private String author;
    private Long userId;


    @Builder
    public PostsSaveRequestDto(String kind, String title, ContentDto content, String author, Long userId) {
        this.kind = kind;
        this.title = title;
        this.content = content;
        this.author = author;
        this.userId = userId;
    }

    public Posts toEntity(Users user){
        return Posts.builder()
                .kind(kind)
                .title(title)
                .content(content.toEntity())
                .author(author)
                .user(user)
                .build();
    }
}