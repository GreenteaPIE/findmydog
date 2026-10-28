package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.config.auth.LoginUser;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import com.greentea.findmydog.springboot.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 글쓴이는 요청 본문이 아니라 로그인 세션에서 정하고, 수정·삭제는 글쓴이만 할 수 있다 */
@RequiredArgsConstructor
@RestController
public class PostsApiController {

    private final PostsService postsService;

    private static Long userId(SessionUser user) {
        if (user == null) throw new AccessDeniedException("로그인이 필요해요");
        return user.getId();
    }

    @PostMapping("/api/v1/posts")
    public Map<String, Long> save(@LoginUser SessionUser user,
                                  @Valid @RequestPart("post") PostsSaveRequestDto requestDto,
                                  @RequestPart(value = "images", required = false) List<MultipartFile> files) {
        return Map.of("id", postsService.save(requestDto, files, userId(user)));
    }

    @PutMapping("/api/v1/posts/{id}")
    public Map<String, Long> update(@LoginUser SessionUser user, @PathVariable Long id,
                                    @Valid @RequestPart("post") PostsUpdateRequestDto requestDto,
                                    @RequestPart(value = "images", required = false) List<MultipartFile> files) {
        return Map.of("id", postsService.update(id, requestDto, files, userId(user)));
    }

    @PatchMapping("/api/v1/posts/{id}/status")
    public ResponseEntity<Void> changeStatus(@LoginUser SessionUser user, @PathVariable Long id,
                                             @Valid @RequestBody StatusUpdateRequestDto request) {
        postsService.changeStatus(id, request.status(), userId(user));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/posts/{id}")
    public ResponseEntity<Void> delete(@LoginUser SessionUser user, @PathVariable Long id) {
        postsService.delete(id, userId(user));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/posts/{id}")
    public PostsResponseDto findById(@PathVariable Long id) {
        return postsService.findById(id);
    }

    // 제보 지도의 핀 (누구나 볼 수 있고, 연락처는 빠져 있다)
    @GetMapping("/api/map/posts")
    public List<MapMarkerDto> mapMarkers() {
        return postsService.findMapMarkers();
    }
}
