package com.greentea.findmydog.springboot.sevice.users;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.posts.PostsRepository;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final SocialUnlinkService socialUnlinkService;
    private final PostsService postsService;
    private final PostsRepository postsRepository;

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

        // 게시글 삭제
        List<Posts> userPosts = postsRepository.findByUser(user);
        for (Posts post : userPosts) {
            postsService.delete(post.getId());
        }

        // 사용자 삭제
        userRepository.delete(user);
    }
}
