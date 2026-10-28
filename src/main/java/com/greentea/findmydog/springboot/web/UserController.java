package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.config.auth.LoginUser;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.sevice.users.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    // 회원 탈퇴: 소셜 연결 해제, 작성한 글과 사진 삭제, 로그아웃
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@LoginUser SessionUser user, HttpServletRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        HttpSession session = request.getSession();
        userService.deleteUser(user.getId(), (String) session.getAttribute("registrationId"), (String) session.getAttribute("accessToken"));
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
