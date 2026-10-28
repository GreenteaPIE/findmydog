package com.greentea.findmydog.springboot.config.auth;

import com.greentea.findmydog.springboot.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    private static final PathPatternRequestMatcher.Builder PATH = PathPatternRequestMatcher.withDefaults();

    // 로그인 없이 볼 수 있는 화면과 API (읽기 전용)
    private static final String[] PUBLIC_GET = {
            "/", "/posts", "/posts/{id:\\d+}", "/map", "/adoption", "/guide", "/about", "/login",
            "/images/**", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/favicon.svg", "/robots.txt",
            "/api/map/**", "/api/adoption/**", "/actuator/health", "/error",
            "/demo/login", // demo 프로필에서만 있는 주소
            // 예전 주소 (새 주소로 돌려보낸다)
            "/posts/paging", "/posts/detail/**", "/find/map", "/posts/notice", "/loginPage"
    };

    private static RequestMatcher get(String... patterns) {
        return new OrRequestMatcher(Arrays.stream(patterns).map(p -> (RequestMatcher) PATH.matcher(HttpMethod.GET, p)).toList());
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        RequestMatcher h2Console = PATH.matcher("/h2-console/**");
        RequestMatcher api = PATH.matcher("/api/**");

        http
                // 화면은 <meta name="_csrf"> 의 토큰을 X-CSRF-TOKEN 헤더로 보낸다
                .csrf(csrf -> csrf.ignoringRequestMatchers(h2Console))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(get(PUBLIC_GET)).permitAll()
                        .requestMatchers(h2Console).permitAll()
                        .requestMatchers(PATH.matcher("/api/v1/**")).hasRole(Role.USER.name())
                        .anyRequest().authenticated())

                // API 는 로그인 화면으로 보내지 않고 401 을 돌려준다
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), api))

                .logout(logout -> logout.logoutSuccessUrl("/"))

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(customOAuth2UserService))
                        // 로그인 전에 보려던 화면(예: 글쓰기)으로 돌아간다
                        .defaultSuccessUrl("/"));

        return http.build();
    }
}
