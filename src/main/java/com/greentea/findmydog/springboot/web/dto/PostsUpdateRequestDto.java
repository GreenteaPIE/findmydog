package com.greentea.findmydog.springboot.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsUpdateRequestDto {
    @NotBlank(message = "분실·발견 중 하나를 골라 주세요")
    @Pattern(regexp = "^(분실|발견)$", message = "분실·발견 중 하나를 골라 주세요")
    private String kind;

    @NotBlank(message = "제목을 입력해 주세요")
    @Size(max = 100, message = "제목은 100자까지 쓸 수 있어요")
    private String title;

    @Valid
    @NotNull
    private ContentDto content;

    private boolean imageChanged; // true 면 기존 사진을 지우고 새 사진으로 바꾼다

    @Builder
    public PostsUpdateRequestDto(String kind, String title, ContentDto content, boolean imageChanged) {
        this.kind = kind;
        this.title = title;
        this.content = content;
        this.imageChanged = imageChanged;
    }
}
