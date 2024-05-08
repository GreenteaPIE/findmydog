package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostsResponseDto {

    private Long id;
    private String title;
    private String content;
    private String author;
    private List<ImageDto> images; // 이미지 정보를 담을 리스트 추가

    public PostsResponseDto(Posts entity){
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.author = entity.getAuthor();
        // Posts 엔티티 내의 이미지 리스트를 ImageDto 리스트로 변환
        this.images = entity.getImages().stream()
                .map(image -> new ImageDto(image.getId(), image.getOriginalFileName(), image.getStoredFileName()))
                .collect(Collectors.toList());
    }
}
