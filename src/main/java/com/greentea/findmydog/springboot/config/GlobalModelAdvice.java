package com.greentea.findmydog.springboot.config;

import com.greentea.findmydog.springboot.config.auth.LoginUser;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** 모든 화면에서 쓰는 값: 로그인 사용자, 카카오 지도 키, 현재 경로(메뉴 표시용), 데모 모드 여부 */
@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAdvice {

    private final String kakaoMapKey;
    private final String siteUrl;
    private final boolean demoMode;

    public GlobalModelAdvice(@Value("${kakao.map.app-key:}") String kakaoMapKey, @Value("${site.url:}") String siteUrl, Environment environment) {
        this.kakaoMapKey = kakaoMapKey;
        this.siteUrl = siteUrl;
        this.demoMode = environment.acceptsProfiles(Profiles.of("demo"));
    }

    @ModelAttribute
    public void addCommon(@LoginUser SessionUser user, HttpServletRequest request, Model model) {
        model.addAttribute("loginUser", user);
        model.addAttribute("kakaoMapKey", kakaoMapKey);
        model.addAttribute("siteUrl", siteUrl);
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("demoMode", demoMode);
    }
}
