package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.user.Users;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 작성자는 요청 본문이 아니라 로그인 세션에서 정한다 */
@Getter
@NoArgsConstructor
public class PostsSaveRequestDto {
    @NotBlank(message = "분실·발견 중 하나를 골라 주세요")
    @Pattern(regexp = "^(분실|발견)$", message = "분실·발견 중 하나를 골라 주세요")
    private String kind;

    @NotBlank(message = "제목을 입력해 주세요")
    @Size(max = 100, message = "제목은 100자까지 쓸 수 있어요")
    private String title;

    @Valid
    @NotNull
    private ContentDto content;

    @Builder
    public PostsSaveRequestDto(String kind, String title, ContentDto content) {
        this.kind = kind;
        this.title = title;
        this.content = content;
    }

    public Posts toEntity(Users user) {
        return Posts.builder()
                .kind(kind)
                .title(title)
                .content(content.toEntity())
                .author(user.getName())
                .user(user)
                .build();
    }
}
