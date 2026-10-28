package com.greentea.findmydog.springboot.demo;

import com.greentea.findmydog.springboot.domain.posts.Content;
import com.greentea.findmydog.springboot.domain.posts.Image;
import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.posts.PostsRepository;
import com.greentea.findmydog.springboot.domain.user.Role;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.storage.ImageNames;
import com.greentea.findmydog.springboot.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 로컬에서 화면을 둘러보기 위한 예시 데이터. SPRING_PROFILES_ACTIVE=demo 일 때만 동작한다.
 * 예시 사진은 static/img 의 강아지 그림을 색 배경에 얹어 만든다.
 */
@Slf4j
@Profile("demo")
@Component
@RequiredArgsConstructor
public class DemoDataLoader implements ApplicationRunner {

    static final String DEMO_EMAIL = "demo@findmydog.local";

    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final ImageStorage imageStorage;

    private record Sample(String kind, String title, String breed, String color, String gender, Integer age, String pname,
                          String address, String landmark, double lat, double lng, int daysAgo, String dog, String bg,
                          boolean reunited, boolean mine, String features) {
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (postsRepository.count() > 0) return;
        Users demo = userRepository.save(Users.builder().name("데모 사용자").email(DEMO_EMAIL).role(Role.USER).build());
        Users neighbor = userRepository.save(Users.builder().name("망원동 이웃").email("neighbor@findmydog.local").role(Role.USER).build());

        Sample[] samples = {
                new Sample("분실", "망원한강공원에서 흰색 말티즈 루시를 찾아요", "말티즈", "흰색", "암컷", 5, "루시", "서울특별시 마포구 망원동", "망원한강공원 2주차장", 37.5552, 126.8956, 1, "dog-poodle", "#f3d9c4", false, true, "분홍색 하네스를 하고 있어요. 이름을 부르면 다가오지만 낯선 사람을 무서워해요."),
                new Sample("발견", "연남동 경의선숲길에서 허스키를 보호하고 있어요", "시베리안 허스키", "검정, 흰색", "수컷", 2, null, "서울특별시 마포구 연남동", "경의선숲길 연남 구간", 37.5622, 126.9236, 0, "dog-husky", "#cfe0f2", false, false, "목줄은 있는데 인식표가 없어요. 사람을 잘 따르고 건강해 보여요."),
                new Sample("분실", "합정역 근처에서 포메라니안 보리를 잃어버렸어요", "포메라니안", "주황색", "수컷", 3, "보리", "서울특별시 마포구 합정동", "합정역 7번 출구", 37.5495, 126.9139, 2, "dog-pomeranian", "#f6e6b4", false, false, "겁이 많아 차 밑에 숨을 수 있어요. 간식 소리에 반응해요."),
                new Sample("분실", "성산동에서 빨간 스카프 한 강아지를 찾습니다", "믹스견", "흰색, 회색", "수컷", 7, "몽이", "서울특별시 마포구 성산동", "성미산 입구", 37.5664, 126.9105, 4, "dog-bandana", "#e7ddf3", false, false, "빨간 스카프를 하고 있어요. 다리가 조금 불편해요."),
                new Sample("발견", "상암 월드컵공원에서 푸들을 발견했어요", "푸들", "크림색", "모름", null, null, "서울특별시 마포구 상암동", "평화의공원 남문", 37.5668, 126.8970, 3, "dog-poodle", "#d9ecd9", true, false, "가족을 찾아 돌려보냈어요. 도와주셔서 고마워요."),
                new Sample("분실", "서교동에서 포메라니안 두부를 찾아요", "포메라니안", "주황색, 흰색", "암컷", 4, "두부", "서울특별시 마포구 서교동", "홍대입구역 9번 출구", 37.5571, 126.9245, 6, "dog-pomeranian", "#f2d4d4", false, false, "목에 방울이 달린 목걸이를 하고 있어요."),
                new Sample("발견", "신촌역 앞에서 떠돌던 허스키를 데리고 있어요", "시베리안 허스키", "회색, 흰색", "암컷", null, null, "서울특별시 서대문구 신촌동", "신촌역 2번 출구", 37.5552, 126.9368, 5, "dog-husky", "#e3e8ee", false, false, "동물병원에서 칩 조회를 했는데 등록되어 있지 않았어요."),
                new Sample("분실", "망원시장에서 말티즈 콩이가 집을 나갔어요", "말티즈", "흰색", "수컷", 9, "콩이", "서울특별시 마포구 망원동", "망원시장 입구", 37.5560, 126.9060, 9, "dog-bandana", "#f6efc4", true, true, "가족을 찾았어요!"),
        };

        for (Sample s : samples) {
            Content content = Content.builder()
                    .reporterName(s.mine() ? "데모 사용자" : "망원동 이웃").contact("010-1234-5678")
                    .lostDate(LocalDateTime.now().minusDays(s.daysAgo()).withHour(18).withMinute(30).withSecond(0).withNano(0))
                    .landmark(s.landmark()).address(s.address()).breed(s.breed()).pname(s.pname()).color(s.color())
                    .gender(s.gender()).age(s.age()).features(s.features()).hasMicrochip(s.age() != null && s.age() > 3)
                    .latitude(s.lat()).longitude(s.lng()).build();
            Users owner = s.mine() ? demo : neighbor;
            Posts post = Posts.builder().kind(s.kind()).title(s.title()).content(content).author(owner.getName()).user(owner).build();
            if (s.reunited()) post.changeStatus(Posts.STATUS_REUNITED);
            String name = ImageNames.newName("jpg");
            imageStorage.save(name, photo(s.dog(), s.bg()));
            post.addImage(Image.builder().originalFileName(s.dog() + ".png").storedFileName(name).post(post).build());
            postsRepository.save(post);
        }
        log.info("데모 데이터 {}건을 만들었어요. /demo/login 으로 데모 사용자로 로그인할 수 있어요.", samples.length);
    }

    private static byte[] photo(String dog, String background) throws IOException {
        BufferedImage canvas = new BufferedImage(960, 720, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(Color.decode(background));
        g.fillRect(0, 0, 960, 720);
        try (InputStream in = new ClassPathResource("static/img/" + dog + ".png").getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            double scale = 560.0 / img.getHeight();
            int w = (int) (img.getWidth() * scale), h = 560;
            g.drawImage(img, (960 - w) / 2, 720 - h - 40, w, h, null);
        }
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(canvas, "jpg", out);
        return out.toByteArray();
    }
}
