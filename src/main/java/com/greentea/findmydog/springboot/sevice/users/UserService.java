package com.greentea.findmydog.springboot.sevice.users;

import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final SocialUnlinkService socialUnlinkService;

    @Transactional
    public void deleteUser(Long userId, String registrationId, String accessToken) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user found with id: " + userId));

        // 소셜 연동 해제
        if ("google".equals(registrationId)) {
            socialUnlinkService.unlinkGoogle(accessToken);
        } else if ("kakao".equals(registrationId)) {
            socialUnlinkService.unlinkKakao(accessToken);
        } else if ("naver".equals(registrationId)) {
            socialUnlinkService.unlinkNaver(accessToken);
        }

        // 사용자 삭제
        userRepository.delete(user);
    }
}

