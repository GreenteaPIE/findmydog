package com.greentea.findmydog.springboot.sevice.users;

import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final SocialUnlinkService socialUnlinkService;
    private final PostsService postsService;

    @Transactional
    public void deleteUser(Long userId, String registrationId, String accessToken) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없어요: " + userId));

        // 소셜 연결 해제는 토큰이 만료됐을 수도 있으니, 실패해도 탈퇴는 계속 진행한다
        try {
            if (accessToken != null) {
                switch (String.valueOf(registrationId)) {
                    case "google" -> socialUnlinkService.unlinkGoogle(accessToken);
                    case "kakao" -> socialUnlinkService.unlinkKakao(accessToken);
                    case "naver" -> socialUnlinkService.unlinkNaver(accessToken);
                    default -> log.info("연결 해제를 지원하지 않는 로그인: {}", registrationId);
                }
            }
        } catch (RuntimeException e) {
            log.warn("소셜 연결 해제 실패 ({}): {}", registrationId, e.getMessage());
        }

        postsService.deleteAllByUser(user);
        userRepository.delete(user);
    }
}
