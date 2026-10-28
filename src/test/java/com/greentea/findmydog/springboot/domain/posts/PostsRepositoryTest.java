package com.greentea.findmydog.springboot.domain.posts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PostsRepositoryTest {

    @Autowired
    PostsRepository postsRepository;

    @AfterEach
    public void cleanup() {
        postsRepository.deleteAll();
    }

    private Posts newPost(String title) {
        return Posts.builder()
                .kind("실종")
                .title(title)
                .author("author@example.com")
                .content(Content.builder().pname("루시").breed("말티즈").landmark("시청역").build())
                .build();
    }

    @Test
    public void 게시글저장_불러오기() {
        //given
        String title = "말티즈 루시를 찾습니다";
        postsRepository.save(newPost(title));

        //when
        List<Posts> postsList = postsRepository.findAll();

        //then
        Posts posts = postsList.get(0);
        assertThat(posts.getTitle()).isEqualTo(title);
        assertThat(posts.getKind()).isEqualTo("실종");
        assertThat(posts.getStatus()).isEqualTo("L");
        assertThat(posts.getContent().getPname()).isEqualTo("루시");
    }

    @Test
    public void BaseTimeEntity_등록() {
        //given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        postsRepository.save(newPost("title"));

        //when
        Posts posts = postsRepository.findAll().get(0);

        //then
        assertThat(posts.getCreatedDate()).isAfter(before);
        assertThat(posts.getModifiedDate()).isAfter(before);
    }
}
