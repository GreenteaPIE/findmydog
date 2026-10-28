package com.greentea.findmydog.springboot.config.auth;

import com.greentea.findmydog.springboot.config.auth.dto.OAuthAttributes;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.domain.user.Role;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private final Set<String> adminEmails;

    // ADMIN_EMAILS 에 적힌 이메일로 로그인하면 관리자가 된다 (쉼표로 여러 명)
    public CustomOAuth2UserService(UserRepository userRepository, HttpSession httpSession, @Value("${admin.emails:}") String adminEmails) {
        this.userRepository = userRepository;
        this.httpSession = httpSession;
        this.adminEmails = Arrays.stream(adminEmails.split(",")).map(String::trim).filter(e -> !e.isEmpty())
                .map(e -> e.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User =  delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        Users user = saveOrUpdate(attributes);

        httpSession.setAttribute("user", new SessionUser(user));
        httpSession.setAttribute("accessToken", userRequest.getAccessToken().getTokenValue());
        httpSession.setAttribute("registrationId", registrationId);

        return new DefaultOAuth2User(
                user.authorities(),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());

    }

    private Users saveOrUpdate(OAuthAttributes attributes) {
        Users user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());
        if (user.getEmail() != null && adminEmails.contains(user.getEmail().toLowerCase(Locale.ROOT))) {
            user.changeRole(Role.ADMIN);
        }
        return userRepository.save(user);
    }

}