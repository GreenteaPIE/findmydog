package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Content;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsUpdateRequestDto {
    private String kind;
    private String title;
    private ContentDto content;
    private boolean imageChanged; // Add this field

    @Builder
    public PostsUpdateRequestDto(String kind, String title, ContentDto content, boolean imageChanged) {
        this.kind = kind;
        this.title = title;
        this.content = content;
        this.imageChanged = imageChanged; // Set this field
    }
}