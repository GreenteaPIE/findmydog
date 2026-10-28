package com.greentea.findmydog.springboot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 업로드 이미지는 build 폴더 아래에 저장해서 테스트가 다른 곳을 건드리지 않게 한다
@SpringBootTest(properties = "file.upload-dir=build/test-uploads")
public class PostsApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mvc;
    private Users user;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        user = userRepository.save(Users.builder().name("tester").email("tester@example.com").role(Role.USER).build());
    }

    @AfterEach
    public void tearDown() {
        postsRepository.deleteAll();
        userRepository.deleteAll();
    }

    private ContentDto content(String pname) {
        return ContentDto.builder().pname(pname).breed("말티즈").landmark("시청역").latitude(37.5665).longitude(126.978).build();
    }

    private MockMultipartFile jsonPart(Object body) throws Exception {
        return new MockMultipartFile("post", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(body));
    }

    private MockMultipartFile pngPart() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB), "png", out);
        return new MockMultipartFile("images", "dog.png", MediaType.IMAGE_PNG_VALUE, out.toByteArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_등록된다() throws Exception {
        //given
        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                .kind("실종").title("말티즈 루시를 찾습니다").author("tester").userId(user.getId()).content(content("루시"))
                .build();

        //when
        mvc.perform(multipart("/api/v1/posts").file(jsonPart(requestDto)).file(pngPart()))
                .andExpect(status().isOk());

        //then
        List<Posts> all = postsRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getTitle()).isEqualTo("말티즈 루시를 찾습니다");
        assertThat(all.get(0).getKind()).isEqualTo("실종");
    }

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_수정된다() throws Exception {
        //given
        PostsSaveRequestDto saveDto = PostsSaveRequestDto.builder()
                .kind("실종").title("title").author("tester").userId(user.getId()).content(content("루시"))
                .build();
        Posts saved = postsRepository.save(saveDto.toEntity(user));

        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
                .kind("실종").title("title2").content(content("루시")).imageChanged(false)
                .build();

        //when
        mvc.perform(multipart(HttpMethod.PUT, "/api/v1/posts/" + saved.getId()).file(jsonPart(requestDto)))
                .andExpect(status().isOk());

        //then
        assertThat(postsRepository.findById(saved.getId()).orElseThrow().getTitle()).isEqualTo("title2");
    }

    @Test
    public void 로그인하지_않으면_등록할_수_없다() throws Exception {
        mvc.perform(multipart("/api/v1/posts").file(jsonPart(PostsSaveRequestDto.builder().title("x").build())).file(pngPart()))
                .andExpect(status().is3xxRedirection());
        assertThat(postsRepository.findAll()).isEmpty();
    }
}
