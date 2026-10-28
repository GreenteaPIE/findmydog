package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.storage.ImageNames;
import lombok.Getter;
import lombok.Setter;

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
    private boolean hidden;
    private int sightingCount;
    @Setter
    private Double distanceKm; // 기준 위치에서 떨어진 거리 (내 주변 검색, 비슷한 제보)

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
        this.hidden = entity.isHidden();
        this.sightingCount = entity.getSightings().size();
        this.images = entity.getImages().stream()
                .map(image -> new ImageDto(image.getId(), image.getOriginalFileName(), image.getStoredFileName()))
                .toList();
        this.content = new ContentDto(entity.getContent());
    }

    /** 목록용 작은 대표 사진. 사진이 없으면 null */
    public String getThumbnail() {
        return images.isEmpty() ? null : ImageNames.thumbnailOf(images.get(0).getStoredFileName());
    }

    /** 1.2km, 800m 처럼 읽기 쉬운 거리 */
    public String getDistanceLabel() {
        if (distanceKm == null) return null;
        return distanceKm < 1 ? Math.max(100, Math.round(distanceKm * 10) * 100) + "m" : String.format("%.1fkm", distanceKm);
    }
}
