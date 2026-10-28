package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Content;
import com.greentea.findmydog.springboot.domain.posts.Posts;

import java.time.LocalDateTime;

/** 제보 지도에 찍을 핀 하나. 연락처 같은 개인정보는 넣지 않는다. */
public record MapMarkerDto(Long id, String kind, boolean reunited, String title, String breed, String pname,
                           String address, double lat, double lng, String thumbnail, LocalDateTime lostDate) {

    public static MapMarkerDto from(Posts post) {
        Content c = post.getContent();
        String thumbnail = post.getImages().isEmpty() ? null : "/images/" + post.getImages().get(0).getStoredFileName();
        return new MapMarkerDto(post.getId(), post.getKind(), post.isReunited(), post.getTitle(), c.getBreed(), c.getPname(),
                c.getAddress(), c.getLatitude(), c.getLongitude(), thumbnail, c.getLostDate());
    }
}
