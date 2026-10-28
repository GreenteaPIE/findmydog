package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostsResponseDto {

    private Long id;
    private Long user_id;
    private String kind;
    private String title;
    private String author;
    private String status;
    private boolean reunited;
    private List<ImageDto> images;
    private ContentDto content;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public PostsResponseDto(Posts entity) {
        this.id = entity.getId();
        this.user_id = entity.getUser() == null ? null : entity.getUser().getId();
        this.kind = entity.getKind();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.status = entity.getStatus();
        this.reunited = entity.isReunited();
        this.createdDate = entity.getCreatedDate();
        this.modifiedDate = entity.getModifiedDate();
        this.images = entity.getImages().stream()
                .map(image -> new ImageDto(image.getId(), image.getOriginalFileName(), image.getStoredFileName()))
                .toList();
        this.content = new ContentDto(entity.getContent());
    }

    /** 첫 번째 사진. 사진이 없으면 null */
    public String getThumbnail() {
        return images.isEmpty() ? null : images.get(0).getStoredFileName();
    }
}
