package com.greentea.findmydog.springboot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.domain.posts.Content;
import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.posts.PostsRepository;
import com.greentea.findmydog.springboot.domain.user.Role;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import com.greentea.findmydog.springboot.web.dto.ContentDto;
import com.greentea.findmydog.springboot.web.dto.PostsResponseDto;
import com.greentea.findmydog.springboot.web.dto.PostsSaveRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 목격 제보, 신고·관리자, 비슷한 제보, 내 주변, 썸네일, 도배 방지, 전단지·QR·사이트맵 */
@SpringBootTest(properties = "file.upload-dir=build/test-uploads")
public class CommunityFeaturesTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PostsRepository postsRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostsService postsService;
    @Autowired private TransactionTemplate tx;

    private MockMvc mvc;
    private Users owner, neighbor, other, admin;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        owner = userRepository.save(Users.builder().name("보호자").email("owner@test.com").role(Role.USER).build());
        neighbor = userRepository.save(Users.builder().name("이웃").email("neighbor@test.com").role(Role.USER).build());
        other = userRepository.save(Users.builder().name("다른 사람").email("other@test.com").role(Role.USER).build());
        admin = userRepository.save(Users.builder().name("관리자").email("admin@test.com").role(Role.ADMIN).build());
    }

    @AfterEach
    void tearDown() {
        postsRepository.deleteAll();
        userRepository.deleteAll();
    }

    private <T extends MockHttpServletRequestBuilder> T as(Users who, T request) {
        String[] roles = who.isAdmin() ? new String[]{"USER", "ADMIN"} : new String[]{"USER"};
        request.sessionAttr("user", new SessionUser(who)).with(user(who.getEmail()).roles(roles)).with(csrf());
        return request;
    }

    private Posts newPost(Users user, String kind, String breed, double lat, double lng) {
        Content content = Content.builder().reporterName("김루시").contact("010-1234-5678").lostDate(LocalDateTime.now().minusDays(1))
                .breed(breed).color("흰색").gender("암컷").address("서울 마포구").latitude(lat).longitude(lng).build();
        return postsRepository.save(Posts.builder().kind(kind).title(breed + " " + kind).content(content).author(user.getName()).user(user).build());
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private Map<String, Object> sighting(String contact) {
        return Map.of("seenAt", LocalDateTime.now().minusHours(2).withNano(0).toString(), "place", "망원시장 입구",
                "note", "흰 강아지가 혼자 있었어요", "contact", contact, "latitude", 37.556, "longitude", 126.906);
    }

    @Test
    void 목격_제보는_누구나_남기고_연락처는_글쓴이에게만_보인다() throws Exception {
        Posts lost = newPost(owner, "분실", "말티즈", 37.5552, 126.8956);

        mvc.perform(as(neighbor, post("/api/v1/posts/" + lost.getId() + "/sightings")).contentType(MediaType.APPLICATION_JSON).content(json(sighting("010-2222-3333"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNumber());

        String ownerView = mvc.perform(as(owner, get("/posts/" + lost.getId()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(ownerView).contains("망원시장 입구").contains("010-2222-3333").contains("목격 제보 1건");
        String otherView = mvc.perform(as(other, get("/posts/" + lost.getId()))).andReturn().getResponse().getContentAsString();
        assertThat(otherView).contains("망원시장 입구").doesNotContain("010-2222-3333");
    }

    @Test
    void 목격_제보는_쓴_사람과_글쓴이만_지울_수_있다() throws Exception {
        Posts lost = newPost(owner, "분실", "말티즈", 37.5552, 126.8956);
        String body = mvc.perform(as(neighbor, post("/api/v1/posts/" + lost.getId() + "/sightings")).contentType(MediaType.APPLICATION_JSON).content(json(sighting(""))))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("id").asLong();

        mvc.perform(as(other, delete("/api/v1/sightings/" + id))).andExpect(status().isForbidden());
        mvc.perform(as(owner, delete("/api/v1/sightings/" + id))).andExpect(status().isNoContent());
    }

    @Test
    void 가족을_찾은_글에는_목격_제보를_남길_수_없다() throws Exception {
        Posts lost = newPost(owner, "분실", "말티즈", 37.5552, 126.8956);
        tx.executeWithoutResult(s -> postsRepository.findById(lost.getId()).orElseThrow().changeStatus(Posts.STATUS_REUNITED));
        mvc.perform(as(neighbor, post("/api/v1/posts/" + lost.getId() + "/sightings")).contentType(MediaType.APPLICATION_JSON).content(json(sighting(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 신고가_3건_쌓이면_숨기고_관리자가_되살린다() throws Exception {
        Posts spam = newPost(owner, "분실", "말티즈", 37.5552, 126.8956);
        String report = json(Map.of("reason", "광고·도배"));
        String url = "/api/v1/posts/" + spam.getId() + "/reports";

        mvc.perform(as(owner, post(url)).contentType(MediaType.APPLICATION_JSON).content(report)).andExpect(status().isBadRequest()); // 내 글
        mvc.perform(as(neighbor, post(url)).contentType(MediaType.APPLICATION_JSON).content(report)).andExpect(jsonPath("$.hidden").value(false));
        mvc.perform(as(neighbor, post(url)).contentType(MediaType.APPLICATION_JSON).content(report)).andExpect(status().isConflict()); // 두 번
        mvc.perform(as(other, post(url)).contentType(MediaType.APPLICATION_JSON).content(report)).andExpect(jsonPath("$.hidden").value(false));
        mvc.perform(as(admin, post(url)).contentType(MediaType.APPLICATION_JSON).content(report)).andExpect(jsonPath("$.hidden").value(true));

        // 숨긴 글: 남에게는 404, 글쓴이와 관리자에게는 보인다. 목록·지도에서도 빠진다
        mvc.perform(as(other, get("/posts/" + spam.getId()))).andExpect(status().isNotFound());
        mvc.perform(as(owner, get("/posts/" + spam.getId()))).andExpect(status().isOk());
        assertThat(postsService.findMapMarkers()).isEmpty();

        mvc.perform(as(neighbor, get("/admin"))).andExpect(status().isForbidden());
        mvc.perform(as(admin, get("/admin"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("신고 3건")));
        mvc.perform(as(neighbor, post("/api/admin/posts/" + spam.getId() + "/dismiss"))).andExpect(status().isForbidden());
        mvc.perform(as(admin, post("/api/admin/posts/" + spam.getId() + "/dismiss"))).andExpect(status().isNoContent());
        assertThat(postsService.findMapMarkers()).hasSize(1);
    }

    @Test
    void 비슷한_제보는_반대_종류의_가까운_글이고_품종이_같으면_먼저() {
        Posts lost = newPost(owner, "분실", "말티즈", 37.5552, 126.8956);       // 망원한강공원
        newPost(neighbor, "발견", "푸들", 37.5560, 126.9060);                   // 약 0.9km, 다른 품종
        Posts sameBreed = newPost(neighbor, "발견", "말티즈", 37.5622, 126.9236); // 약 2.6km, 같은 품종
        newPost(neighbor, "발견", "말티즈", 35.1796, 129.0756);                  // 부산: 제외
        newPost(neighbor, "분실", "말티즈", 37.5560, 126.9000);                  // 같은 종류: 제외

        List<PostsResponseDto> similar = postsService.findSimilar(lost.getId(), 4);
        assertThat(similar).hasSize(2);
        assertThat(similar.get(0).getId()).isEqualTo(sameBreed.getId());
        assertThat(similar.get(0).getDistanceLabel()).endsWith("km");
    }

    @Test
    void 내_주변_검색은_3km_안쪽만() {
        newPost(owner, "분실", "말티즈", 37.5552, 126.8956);
        newPost(owner, "분실", "푸들", 35.1796, 129.0756);
        var page = postsService.search(null, null, null, 37.556, 126.900, 1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getDistanceLabel()).isNotNull();
    }

    @Test
    void 사진을_올리면_목록용_작은_사진도_만든다() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2000, 1500, BufferedImage.TYPE_INT_RGB), "png", out);
        ContentDto content = ContentDto.builder().reporterName("김루시").contact("010-1234-5678").lostDate(LocalDateTime.now().minusHours(1))
                .breed("말티즈").color("흰색").gender("암컷").latitude(37.55).longitude(126.9).build();
        PostsSaveRequestDto dto = PostsSaveRequestDto.builder().kind("분실").title("사진 테스트").content(content).build();

        mvc.perform(as(owner, multipart("/api/v1/posts")
                        .file(new MockMultipartFile("post", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto)))
                        .file(new MockMultipartFile("images", "big.png", MediaType.IMAGE_PNG_VALUE, out.toByteArray()))))
                .andExpect(status().isOk());

        String name = tx.execute(s -> postsRepository.findAll().get(0).getImages().get(0).getStoredFileName());
        String thumb = name.replace(".jpg", "_t.jpg");
        BufferedImage full = ImageIO.read(Path.of("build/test-uploads", name).toFile());
        BufferedImage small = ImageIO.read(Path.of("build/test-uploads", thumb).toFile());
        assertThat(full.getWidth()).isEqualTo(1280);
        assertThat(small.getWidth()).isEqualTo(480);
        mvc.perform(get("/images/" + thumb)).andExpect(status().isOk()).andExpect(content().contentType("image/jpeg"));
    }

    @Test
    void 제보는_하루에_10개까지() throws Exception {
        for (int i = 0; i < PostsService.MAX_POSTS_PER_DAY; i++) newPost(owner, "분실", "말티즈", 37.55, 126.9);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "png", out);
        ContentDto content = ContentDto.builder().reporterName("김루시").contact("010-1234-5678").lostDate(LocalDateTime.now().minusHours(1))
                .breed("말티즈").color("흰색").latitude(37.55).longitude(126.9).build();
        mvc.perform(as(owner, multipart("/api/v1/posts")
                        .file(new MockMultipartFile("post", "", MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsBytes(PostsSaveRequestDto.builder().kind("분실").title("11번째").content(content).build())))
                        .file(new MockMultipartFile("images", "a.png", MediaType.IMAGE_PNG_VALUE, out.toByteArray()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제보는 하루에 10개까지 올릴 수 있어요"));
    }

    @Test
    void 전단지는_글쓴이에게만_연락처를_넣고_QR_과_사이트맵이_있다() throws Exception {
        Posts lost = newPost(owner, "분실", "말티즈", 37.5552, 126.8956);
        mvc.perform(as(owner, get("/posts/" + lost.getId() + "/flyer"))).andExpect(content().string(org.hamcrest.Matchers.containsString("010-1234-5678")));
        String anonymous = mvc.perform(get("/posts/" + lost.getId() + "/flyer")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(anonymous).contains("찾습니다").doesNotContain("010-1234-5678");

        mvc.perform(get("/posts/" + lost.getId() + "/qr.svg")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("<svg")));
        mvc.perform(get("/sitemap.xml")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/posts/" + lost.getId() + "</loc>")));
        mvc.perform(get("/robots.txt")).andExpect(content().string(org.hamcrest.Matchers.containsString("Sitemap: http://localhost/sitemap.xml")));
    }
}
