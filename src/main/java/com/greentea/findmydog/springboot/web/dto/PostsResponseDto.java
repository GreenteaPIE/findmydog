package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostsResponseDto {

    private Long id;
    private Long user_id;
    private String kind;
    private String title;
    private String author;
    private String status;
    private List<ImageDto> images; // 이미지 정보를 담을 리스트 추가
    private ContentDto content;
    private LocalDateTime modifiedDate;

    public PostsResponseDto(Posts entity){
        this.id = entity.getId();
        this.user_id = entity.getUser().getId();
        this.kind = entity.getKind();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.status = entity.getStatus();
        this.modifiedDate = entity.getModifiedDate();
        // Posts 엔티티 내의 이미지 리스트를 ImageDto 리스트로 변환
        this.images = entity.getImages().stream()
                .map(image -> new ImageDto(image.getId(), image.getOriginalFileName(), image.getStoredFileName()))
                .collect(Collectors.toList());
        this.content = new ContentDto(entity.getContent());
    }
}
