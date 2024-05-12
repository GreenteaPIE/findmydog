package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostsListResponseDto {
    private Long id;
    private String kind;
    private String title;
    private String author;
    private List<ImageDto> images;
    private LocalDateTime modifiedDate;

    public PostsListResponseDto(Posts entity){
        this.id = entity.getId();
        this.kind = entity.getKind();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.modifiedDate = entity.getModifiedDate();
        this.images = entity.getImages().stream()
                .map(image -> new ImageDto(image.getId(), image.getOriginalFileName(), image.getStoredFileName()))
                .collect(Collectors.toList());
    }
}