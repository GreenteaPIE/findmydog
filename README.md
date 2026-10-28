# 🐶 Find My Dog

잃어버린 반려견을 **지역 커뮤니티에서 함께 찾는** 웹 서비스입니다.
실종·발견 글을 사진·위치와 함께 올리고, 지도에서 근처 제보를 모아 보고, 유기견 입양 공고도 함께 확인할 수 있어요.

> 반려견 루시(말티즈)를 키우며, 산책 중 잃어버린 강아지를 찾기 어렵다는 이야기를 듣고 시작한 개인 프로젝트입니다.
> 개발기: https://greenteapie.github.io/Project-Find-my-dog/

## 주요 기능

- **실종·발견 제보**: 사진(최대 5장, 서버에서 JPEG 로 다시 저장하며 위치 정보 제거), 지도에서 위치 고르기·장소 검색, 품종·색상·동네 검색과 필터
- **가족을 찾았어요**: 찾으면 상태를 바꿔 연락처를 숨기고 ‘찾았어요’ 도장이 찍혀요
- **제보 지도**: 카카오맵 클러스터, 화면 안 제보 목록, 실종·발견 필터
- **보호소 입양 공고**: 공공데이터포털 ‘구조동물 조회 서비스’ (키가 없으면 국가동물보호정보시스템 화면 수집으로 대체)
- **소셜 로그인**: 카카오·네이버·구글. 탈퇴하면 소셜 연결 해제와 작성 글·사진 삭제
- **보안**: 작성자는 세션에서 정하고 수정·삭제·상태 변경은 작성자만(403), CSRF 보호, 업로드 파일은 UUID 이름으로 저장, 연락처는 로그인한 사람에게만 표시

## 기술 스택

| 분류 | 사용 기술 |
|---|---|
| Backend | Java 17, Spring Boot 3.5, Spring Security (OAuth2 Client), Spring Data JPA, Spring Session JDBC, Bean Validation |
| View | Thymeleaf (+ Layout Dialect), 직접 만든 CSS, 바닐라 JavaScript |
| Database | H2 (개발), PostgreSQL (운영) |
| 기타 | Kakao Map API, 공공데이터포털 API, jsoup, JUnit 5, GitHub Actions, Docker |

## 로컬에서 실행하기

키 없이 예시 데이터로 둘러보기 (데모 계정 로그인 포함):

```bash
SPRING_PROFILES_ACTIVE=demo ./gradlew bootRun
```

http://localhost:8080 접속 후 로그인 화면의 **데모 계정으로 둘러보기**를 누르면 글쓰기·수정·삭제까지 해 볼 수 있어요. (`demo` 프로필에서만 켜집니다)

실제 소셜 로그인을 쓰려면 환경 변수로 키를 넣거나, `application-oauth.properties.example` 을 `src/main/resources/application-oauth.properties` 로 복사해 채웁니다 (`.gitignore` 되어 있음).

| 환경 변수 | 설명 |
|---|---|
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | 구글 로그인 |
| `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` | 네이버 로그인 |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` | 카카오 로그인 |
| `KAKAO_MAP_KEY` | 카카오 지도 JavaScript 키 (사이트 도메인 등록 필요) |
| `DATA_GO_KR_SERVICE_KEY` | 공공데이터포털 구조동물 조회 서비스 인증키 (없으면 화면 수집으로 대체) |
| `FILE_UPLOAD_DIR` | 사진 저장 폴더 (기본 `./uploads`) |

## 운영 (prod 프로필)

```bash
docker build -t findmydog .
docker run -p 8080:8080 -v findmydog-uploads:/data/uploads \
  -e DATABASE_URL=jdbc:postgresql://HOST:5432/findmydog -e DATABASE_USERNAME=... -e DATABASE_PASSWORD=... \
  -e GOOGLE_CLIENT_ID=... -e GOOGLE_CLIENT_SECRET=... -e KAKAO_MAP_KEY=... findmydog
```

헬스체크: `/actuator/health`. 프록시(로드밸런서) 뒤에서 https 로 서비스하는 것을 전제로 보안 쿠키와 forwarded 헤더를 켭니다.

## 테스트

```bash
./gradlew test
```

작성자 확인(403), 로그인 확인(401), CSRF, 입력 검증, 파일 이름 경로 조작, 공공 API 응답 해석, 화면 렌더링을 검사합니다. GitHub Actions 에서 push·PR 마다 실행됩니다.

## 프로젝트 구조

```
src/main/java/com/greentea/findmydog/springboot
├── config/        # 보안(OAuth2), JPA, 웹 설정
├── domain/        # 엔티티와 리포지토리 (Posts, Content, Image, Users)
├── sevice/        # 게시글, 입양 공고(공공 API·화면 수집), 회원
├── storage/       # 사진 저장소(ImageStorage)와 JPEG 변환
├── exception/     # API 오류 응답
├── demo/          # demo 프로필 전용 예시 데이터·데모 로그인
└── web/           # 컨트롤러와 DTO
```
