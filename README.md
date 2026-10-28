# 🐶 Find My Dog

잃어버린 반려견을 **지역 커뮤니티에서 함께 찾는** 웹 서비스입니다.
실종·발견 글을 사진·위치와 함께 올리고, 지도에서 근처 제보를 모아 보고, 유기견 입양 공고도 함께 확인할 수 있어요.

> 반려견 루시(말티즈)를 키우며, 산책 중 잃어버린 강아지를 찾기 어렵다는 이야기를 듣고 시작한 개인 프로젝트입니다.
> 개발기: https://greenteapie.github.io/Project-Find-my-dog/

## 주요 기능

- **소셜 로그인**: 카카오·네이버·구글 (Spring Security OAuth2), 탈퇴 시 소셜 연결 해제와 작성 글 정리
- **실종·발견 게시판**: 작성·수정·삭제, 여러 장 이미지 업로드(자동 리사이즈), 검색과 페이징
- **지도**: 카카오맵에 실종 위치 표시, 클러스터로 근처 제보 모아 보기
- **유기견 입양 공고**: 공고를 수집해 조건별로 검색

## 기술 스택

| 분류 | 사용 기술 |
|---|---|
| Backend | Java 17, Spring Boot 3.5, Spring Security (OAuth2 Client), Spring Data JPA, Spring Session JDBC |
| View | Thymeleaf (+ Layout Dialect), Bootstrap, jQuery |
| Database | H2 (인메모리) |
| 기타 | Kakao Map API, jsoup, JUnit 5, GitHub Actions |

## 실행하기

1. OAuth 키 설정: `application-oauth.properties.example` 을 `src/main/resources/application-oauth.properties` 로 복사하고,
   카카오·네이버·구글 개발자 콘솔에서 발급받은 키를 넣습니다. (이 파일은 `.gitignore` 되어 커밋되지 않습니다)
2. 실행
   ```bash
   ./gradlew bootRun
   ```
3. http://localhost:8080 접속

업로드한 이미지는 기본으로 `./uploads` 에 저장됩니다. 다른 폴더를 쓰려면 환경 변수 `FILE_UPLOAD_DIR` 을 지정하세요 (예: `C:/download`).

## 테스트

```bash
./gradlew test
```

테스트는 `src/test/resources` 의 가짜 OAuth 설정을 사용하므로 실제 키 없이 실행됩니다. GitHub Actions 에서 push·PR 마다 자동으로 실행됩니다.

## 프로젝트 구조

```
src/main/java/com/greentea/findmydog/springboot
├── config/        # 보안(OAuth2), JPA, 웹 설정
├── domain/        # 엔티티와 리포지토리 (Posts, Content, Image, Users)
├── sevice/        # 게시글·이미지 처리, 소셜 연결 해제, 입양 공고 수집
└── web/           # 컨트롤러와 DTO
```
