package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.config.auth.LoginUser;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.sevice.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    private final HttpSession httpSession;

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@LoginUser SessionUser user) {
        String registrationId = (String) httpSession.getAttribute("registrationId");
        String accessToken = (String) httpSession.getAttribute("accessToken");

        if (user != null && registrationId != null && accessToken != null) {
            userService.deleteUser(user.getId(), registrationId, accessToken);
            httpSession.invalidate();  // 세션 무효화
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }
}
