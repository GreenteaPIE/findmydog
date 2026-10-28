package com.greentea.findmydog.springboot.sevice.posts;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.posts.PostsRepository;
import com.greentea.findmydog.springboot.domain.posts.Sighting;
import com.greentea.findmydog.springboot.domain.posts.SightingRepository;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.exception.PostNotFoundException;
import com.greentea.findmydog.springboot.web.dto.SightingRequestDto;
import com.greentea.findmydog.springboot.web.dto.SightingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SightingService {

    public static final int MAX_PER_DAY = 20;

    private final SightingRepository sightingRepository;
    private final PostsRepository postsRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long add(Long postId, SightingRequestDto dto, Long userId) {
        Posts post = postsRepository.findById(postId).filter(p -> !p.isHidden()).orElseThrow(() -> new PostNotFoundException(postId));
        if (post.isReunited()) {
            throw new IllegalArgumentException("가족을 찾은 제보에는 목격 제보를 남길 수 없어요");
        }
        Users user = userRepository.findById(userId).orElseThrow(() -> new AccessDeniedException("로그인 정보를 찾을 수 없어요"));
        if (sightingRepository.countByUserAndCreatedDateAfter(user, LocalDateTime.now().minusDays(1)) >= MAX_PER_DAY) {
            throw new IllegalArgumentException("목격 제보는 하루에 " + MAX_PER_DAY + "개까지 남길 수 있어요");
        }
        Sighting sighting = Sighting.builder()
                .post(post).user(user).seenAt(dto.seenAt()).place(dto.place().trim())
                .latitude(dto.latitude()).longitude(dto.longitude())
                .note(StringUtils.hasText(dto.note()) ? dto.note().trim() : null)
                .contact(StringUtils.hasText(dto.contact()) ? dto.contact().trim() : null)
                .build();
        return sightingRepository.save(sighting).getId();
    }

    // 목격자 본인, 제보 글쓴이, 관리자만 지울 수 있다
    @Transactional
    public void delete(Long sightingId, Long userId, boolean admin) {
        Sighting sighting = sightingRepository.findById(sightingId)
                .orElseThrow(() -> new IllegalArgumentException("목격 제보를 찾을 수 없어요"));
        if (!admin && !sighting.isWrittenBy(userId) && !sighting.getPost().isOwnedBy(userId)) {
            throw new AccessDeniedException("내가 남긴 목격 제보만 지울 수 있어요");
        }
        sighting.getPost().getSightings().remove(sighting);
        sightingRepository.delete(sighting);
    }

    @Transactional(readOnly = true)
    public List<SightingResponseDto> list(Long postId, Long viewerId, boolean admin) {
        Posts post = postsRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        return sightingRepository.findByPostOrderBySeenAtDesc(post).stream()
                .map(s -> SightingResponseDto.of(s, viewerId, admin))
                .toList();
    }
}
