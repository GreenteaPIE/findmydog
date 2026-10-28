package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Sighting;

import java.time.LocalDateTime;

/** contact 는 제보 글쓴이와 목격자 본인에게만 채워진다. deletable 은 지금 보는 사람이 지울 수 있는지. */
public record SightingResponseDto(Long id, String authorName, LocalDateTime seenAt, String place, Double lat, Double lng,
                                  String note, String contact, boolean deletable) {

    public static SightingResponseDto of(Sighting s, Long viewerId, boolean admin) {
        boolean postOwner = s.getPost().isOwnedBy(viewerId);
        boolean writer = viewerId != null && s.isWrittenBy(viewerId);
        return new SightingResponseDto(s.getId(), s.getUser().getName(), s.getSeenAt(), s.getPlace(), s.getLatitude(), s.getLongitude(),
                s.getNote(), postOwner || writer ? s.getContact() : null, postOwner || writer || admin);
    }
}
