package com.greentea.findmydog.springboot.config.auth;

import com.greentea.findmydog.springboot.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig{

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/img/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/h2-console/**"),
                                new AntPathRequestMatcher("/profile"),
                                new AntPathRequestMatcher("/posts/paging**"),
                                new AntPathRequestMatcher("/posts/detail/**"),
                                new AntPathRequestMatcher("/loginPage"),
                                new AntPathRequestMatcher("/find/map"),
                                new AntPathRequestMatcher("/about"),
                                new AntPathRequestMatcher("/adoption"),
                                new AntPathRequestMatcher("/adoption/*"),
                                new AntPathRequestMatcher("/posts/notice")
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/**")).hasRole(Role.USER.name())
                        .anyRequest().authenticated())

                .logout((logout) -> logout
                        .logoutSuccessUrl("/"))

                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userService(customOAuth2UserService))
                        .defaultSuccessUrl("/", true));

        return http.build();
    }
}
