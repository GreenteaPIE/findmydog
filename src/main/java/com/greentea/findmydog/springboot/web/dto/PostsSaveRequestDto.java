package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsSaveRequestDto {
    private String kind;
    private String title;
    private String content;
    private String author;

    @Builder
    public PostsSaveRequestDto(String kind, String title, String content, String author) {
        this.kind = kind;
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public Posts toEntity(){
        return Posts.builder()
                .kind(kind)
                .title(title)
                .content(content)
                .author(author)
                .build();
    }
}