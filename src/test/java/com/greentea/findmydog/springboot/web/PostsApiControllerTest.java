package com.greentea.findmydog.springboot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.posts.PostsRepository;
import com.greentea.findmydog.springboot.domain.user.Role;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.web.dto.ContentDto;
import com.greentea.findmydog.springboot.web.dto.PostsSaveRequestDto;
import com.greentea.findmydog.springboot.web.dto.PostsUpdateRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 업로드 이미지는 build 폴더 아래에 저장해서 테스트가 다른 곳을 건드리지 않게 한다
@SpringBootTest(properties = "file.upload-dir=build/test-uploads")
public class PostsApiControllerTest {

    private static final Path UPLOADS = Path.of("build/test-uploads");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate tx;

    private MockMvc mvc;
    private Users owner;
    private Users stranger;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        owner = userRepository.save(Users.builder().name("루시 보호자").email("owner@example.com").role(Role.USER).build());
        stranger = userRepository.save(Users.builder().name("지나가는 사람").email("stranger@example.com").role(Role.USER).build());
    }

    @AfterEach
    public void tearDown() {
        postsRepository.deleteAll();
        userRepository.deleteAll();
    }

    // 로그인한 사용자로 요청 (세션의 user + ROLE_USER + CSRF 토큰)
    private <T extends MockHttpServletRequestBuilder> T as(Users who, T request) {
        request.sessionAttr("user", new SessionUser(who)).with(user(who.getEmail()).roles("USER")).with(csrf());
        return request;
    }

    private ContentDto content(String pname) {
        return ContentDto.builder().reporterName("김루시").contact("010-1234-5678").lostDate(LocalDateTime.now().minusHours(3))
                .pname(pname).breed("말티즈").color("흰색").gender("암컷").landmark("시청역").address("서울특별시 중구 태평로1가")
                .latitude(37.5665).longitude(126.978).build();
    }

    private PostsSaveRequestDto saveDto(String title) {
        return PostsSaveRequestDto.builder().kind("분실").title(title).content(content("루시")).build();
    }

    private MockMultipartFile jsonPart(Object body) throws Exception {
        return new MockMultipartFile("post", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(body));
    }

    private MockMultipartFile pngPart(String filename) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB), "png", out);
        return new MockMultipartFile("images", filename, MediaType.IMAGE_PNG_VALUE, out.toByteArray());
    }

    private Posts savedPostOf(Users user) {
        return postsRepository.save(saveDto("말티즈 루시를 찾습니다").toEntity(user));
    }

    private String titleOf(Long id) {
        return postsRepository.findById(id).orElseThrow().getTitle();
    }

    @Test
    public void 글을_올리면_작성자는_세션의_사용자다() throws Exception {
        mvc.perform(as(owner, multipart("/api/v1/posts").file(jsonPart(saveDto("말티즈 루시를 찾습니다"))).file(pngPart("dog.png"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        List<Posts> all = postsRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getAuthor()).isEqualTo("루시 보호자");
        assertThat(all.get(0).getContent().getAddress()).isEqualTo("서울특별시 중구 태평로1가");
    }

    @Test
    public void 업로드_파일_이름은_경로로_쓰지_않고_UUID_로_저장한다() throws Exception {
        mvc.perform(as(owner, multipart("/api/v1/posts").file(jsonPart(saveDto("title"))).file(pngPart("../../evil.png"))))
                .andExpect(status().isOk());

        String stored = tx.execute(s -> postsRepository.findAll().get(0).getImages().get(0).getStoredFileName());
        assertThat(stored).matches("[0-9a-f-]{36}\\.jpg");
        assertThat(Files.exists(UPLOADS.resolve(stored))).isTrue();
    }

    @Test
    public void 사진이_아닌_파일은_거절한다() throws Exception {
        MockMultipartFile fake = new MockMultipartFile("images", "dog.png", MediaType.IMAGE_PNG_VALUE, "not an image".getBytes());
        mvc.perform(as(owner, multipart("/api/v1/posts").file(jsonPart(saveDto("title"))).file(fake)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사진 파일이 아니에요: dog.png"));
        assertThat(postsRepository.findAll()).isEmpty();
    }

    @Test
    public void 입력값이_틀리면_칸별_오류를_알려준다() throws Exception {
        ContentDto bad = content("루시");
        bad.setContact("12345");
        bad.setLatitude(0);
        PostsSaveRequestDto dto = PostsSaveRequestDto.builder().kind("분실").title("").content(bad).build();

        mvc.perform(as(owner, multipart("/api/v1/posts").file(jsonPart(dto)).file(pngPart("dog.png"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists())
                .andExpect(jsonPath("$.fields.contact").exists())
                .andExpect(jsonPath("$.fields.latitude").exists());
    }

    @Test
    public void 로그인하지_않으면_401() throws Exception {
        mvc.perform(multipart("/api/v1/posts").file(jsonPart(saveDto("x"))).file(pngPart("dog.png")).with(csrf()))
                .andExpect(status().isUnauthorized());
        assertThat(postsRepository.findAll()).isEmpty();
    }

    @Test
    public void CSRF_토큰이_없으면_거절한다() throws Exception {
        mvc.perform(multipart("/api/v1/posts").file(jsonPart(saveDto("x"))).file(pngPart("dog.png"))
                        .sessionAttr("user", new SessionUser(owner)).with(user(owner.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void 작성자는_글을_고칠_수_있다() throws Exception {
        Posts saved = savedPostOf(owner);
        PostsUpdateRequestDto dto = PostsUpdateRequestDto.builder().kind("분실").title("고친 제목").content(content("루시")).build();

        mvc.perform(as(owner, multipart(HttpMethod.PUT, "/api/v1/posts/" + saved.getId()).file(jsonPart(dto))))
                .andExpect(status().isOk());

        assertThat(titleOf(saved.getId())).isEqualTo("고친 제목");
    }

    @Test
    public void 다른_사람의_글은_고칠_수_없다() throws Exception {
        Posts saved = savedPostOf(owner);
        PostsUpdateRequestDto dto = PostsUpdateRequestDto.builder().kind("분실").title("가로채기").content(content("루시")).build();

        mvc.perform(as(stranger, multipart(HttpMethod.PUT, "/api/v1/posts/" + saved.getId()).file(jsonPart(dto))))
                .andExpect(status().isForbidden());

        assertThat(titleOf(saved.getId())).isEqualTo("말티즈 루시를 찾습니다");
    }

    @Test
    public void 다른_사람의_글은_지울_수_없고_작성자는_지울_수_있다() throws Exception {
        Posts saved = savedPostOf(owner);

        mvc.perform(as(stranger, delete("/api/v1/posts/" + saved.getId()))).andExpect(status().isForbidden());
        assertThat(postsRepository.existsById(saved.getId())).isTrue();

        mvc.perform(as(owner, delete("/api/v1/posts/" + saved.getId()))).andExpect(status().isNoContent());
        assertThat(postsRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    public void 가족을_찾으면_상태를_바꾼다() throws Exception {
        Posts saved = savedPostOf(owner);

        mvc.perform(as(stranger, patch("/api/v1/posts/" + saved.getId() + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"F\"}")))
                .andExpect(status().isForbidden());
        mvc.perform(as(owner, patch("/api/v1/posts/" + saved.getId() + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"F\"}")))
                .andExpect(status().isNoContent());

        assertThat(postsRepository.findById(saved.getId()).orElseThrow().isReunited()).isTrue();
    }

    @Test
    public void 지도_API_는_연락처를_내보내지_않는다() throws Exception {
        savedPostOf(owner);
        String body = mvc.perform(get("/api/map/posts")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(body).contains("말티즈 루시를 찾습니다").doesNotContain("010-1234-5678");
    }

    @Test
    public void 로그인하면_글쓰기와_내_글_화면이_열린다() throws Exception {
        Posts saved = savedPostOf(owner);
        for (int i = 0; i < 13; i++) savedPostOf(owner); // 2쪽 이상이 되도록
        for (String path : new String[]{"/posts/new", "/posts/new?kind=발견", "/my/posts", "/posts", "/posts?page=2", "/posts/" + saved.getId(), "/posts/" + saved.getId() + "/edit"}) {
            mvc.perform(as(owner, get(path))).andExpect(status().isOk());
        }
        // 남의 글 수정 화면은 403
        mvc.perform(as(stranger, get("/posts/" + saved.getId() + "/edit"))).andExpect(status().isForbidden());
    }

    @Test
    public void 없는_글은_404() throws Exception {
        mvc.perform(get("/api/v1/posts/999999").with(user("a").roles("USER"))).andExpect(status().isNotFound());
    }

    @Test
    public void 이미지_주소에_경로를_넣으면_404() throws Exception {
        mvc.perform(get("/images/..%2F..%2Fbuild.gradle")).andExpect(status().is4xxClientError());
        mvc.perform(get("/images/not-a-uuid.txt")).andExpect(status().isNotFound());
    }
}
