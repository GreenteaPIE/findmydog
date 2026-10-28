package com.greentea.findmydog.springboot.demo;

import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/** demo 프로필 전용: 소셜 로그인 키 없이 데모 사용자(관리자 권한 포함)로 로그인한다. 운영(prod)에서는 이 컨트롤러가 없다. */
@Profile("demo")
@Controller
@RequiredArgsConstructor
public class DemoLoginController {

    private final UserRepository userRepository;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    @GetMapping("/demo/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        Users user = userRepository.findByEmail(DemoDataLoader.DEMO_EMAIL).orElseThrow();
        request.getSession().setAttribute("user", new SessionUser(user));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, user.authorities()));
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return "redirect:/";
    }
}
