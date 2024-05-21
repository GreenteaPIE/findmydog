package com.greentea.findmydog.springboot.sevice.users;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SocialUnlinkService {

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public void unlinkGoogle(String accessToken) {
        String url = "https://accounts.google.com/o/oauth2/revoke?token=" + accessToken;
        restTemplate.postForObject(url, null, String.class);
    }

    public void unlinkKakao(String accessToken) {
        String url = "https://kapi.kakao.com/v1/user/unlink";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public void unlinkNaver(String accessToken) {
        String url = "https://nid.naver.com/oauth2.0/token?grant_type=delete&client_id=" + naverClientId + "&client_secret=" + naverClientSecret + "&access_token=" + accessToken + "&service_provider=NAVER";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        restTemplate.postForObject(url, entity, String.class);
    }
}
