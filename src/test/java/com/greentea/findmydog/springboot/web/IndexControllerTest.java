package com.greentea.findmydog.springboot.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings.Redirects;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class IndexControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // 브라우저처럼 HTML 을 요청하고, 리다이렉트는 따라가지 않는다
    private ResponseEntity<String> get(String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.TEXT_HTML));
        return restTemplate.withRedirects(Redirects.DONT_FOLLOW)
                .exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Test
    public void 로그인_없이_볼_수_있는_화면() {
        assertThat(get("/").getBody()).contains("<title>Find My Dog</title>").contains("동네가 함께 찾아요");
        for (String path : new String[]{"/posts", "/posts?kind=분실&status=L&q=말티즈", "/map", "/adoption", "/guide", "/about", "/login"}) {
            ResponseEntity<String> res = get(path);
            assertThat(res.getStatusCode()).as(path).isEqualTo(HttpStatus.OK);
            assertThat(res.getBody()).as(path).contains("Find My Dog");
        }
    }

    @Test
    public void 없는_제보는_404_화면() {
        ResponseEntity<String> res = get("/posts/999999");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).contains("찾는 페이지가 없어요");
    }

    @Test
    public void 글쓰기는_로그인_화면으로_보낸다() {
        ResponseEntity<String> res = get("/posts/new");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(res.getHeaders().getLocation().getPath()).isEqualTo("/login");
    }

    @Test
    public void 예전_주소는_새_주소로_보낸다() {
        ResponseEntity<String> res = get("/posts/paging?kind=분실&searchTitle=말티즈");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(res.getHeaders().getLocation().toString()).contains("/posts?").contains("q=");
        assertThat(get("/find/map").getHeaders().getLocation().getPath()).isEqualTo("/map");
    }

    @Test
    public void 입양_코드표() {
        String body = restTemplate.getForObject("/api/adoption/codes", String.class);
        assertThat(body).contains("서울특별시").contains("강남구").contains("말티즈");
    }
}
