# 🐶 Find My Dog

잃어버린 반려견을 **지역 커뮤니티에서 함께 찾는** 웹 서비스입니다.
실종·발견 글을 사진·위치와 함께 올리고, 지도에서 근처 제보를 모아 보고, 유기견 입양 공고도 함께 확인할 수 있어요.

> 반려견 루시(말티즈)를 키우며, 산책 중 잃어버린 강아지를 찾기 어렵다는 이야기를 듣고 시작한 개인 프로젝트입니다.
> 개발기: https://greenteapie.github.io/Project-Find-my-dog/

## 주요 기능

- **실종·발견 제보**: 사진(최대 5장, 서버에서 JPEG 로 다시 저장하며 위치 정보 제거), 지도에서 위치 고르기·장소 검색, 품종·색상·동네 검색과 필터
- **가족을 찾았어요**: 찾으면 상태를 바꿔 연락처를 숨기고 ‘찾았어요’ 도장이 찍혀요
- **목격 제보**: 이웃이 “여기서 봤어요”로 본 시간·장소·메모를 남기고, 지도에 번호로 표시돼요. 목격자 연락처는 글쓴이에게만 보여요
- **비슷한 제보 추천**: 실종 글에는 5km 안의 발견 글을, 발견 글에는 실종 글을 같은 품종부터 보여 줘요
- **내 주변 제보**: 현재 위치 3km 안의 제보만 모아 보고 거리를 표시해요
- **인쇄용 전단지**: A4 한 장에 사진·특징·뜯어 가는 연락처 쪽지와 제보 페이지 QR 코드를 넣어 인쇄해요
- **신고와 관리자**: 신고 3건이 쌓이면 자동으로 숨기고, `ADMIN_EMAILS` 의 관리자가 /admin 에서 되살리거나 지워요
- **도배 방지**: 제보 하루 10개, 목격 제보 하루 20개까지
- **제보 지도**: 카카오맵 클러스터, 화면 안 제보 목록, 실종·발견 필터
- **보호소 입양 공고**: 공공데이터포털 ‘구조동물 조회 서비스’ (키가 없으면 국가동물보호정보시스템 화면 수집으로 대체)
- **소셜 로그인**: 카카오·네이버·구글. 탈퇴하면 소셜 연결 해제와 작성 글·사진 삭제
- **성능·검색 노출**: 목록·지도는 480px 작은 사진을 쓰고, `sitemap.xml`·`robots.txt` 를 제공해요
- **보안**: 작성자는 세션에서 정하고 수정·삭제·상태 변경은 작성자만(403), CSRF 보호, 업로드 파일은 UUID 이름으로 저장, 연락처는 로그인한 사람에게만 표시

## 기술 스택

| 분류 | 사용 기술 |
|---|---|
| Backend | Java 17, Spring Boot 3.5, Spring Security (OAuth2 Client), Spring Data JPA, Spring Session JDBC, Bean Validation |
| View | Thymeleaf (+ Layout Dialect), 직접 만든 CSS, 바닐라 JavaScript |
| Database | H2 (개발), PostgreSQL (운영) |
| 기타 | Kakao Map API, 공공데이터포털 API, jsoup, JUnit 5, GitHub Actions, Docker |

## 로컬에서 실행하기

**Java 17 이상**이 필요합니다. 키 없이 예시 데이터로 둘러보기 (데모 계정 로그인 포함):

```bash
./gradlew bootRun --args='--spring.profiles.active=demo'
```

Windows 명령 프롬프트:

```bat
gradlew.bat bootRun --args="--spring.profiles.active=demo"
```

> `Find My Dog needs Java 17 or newer` 가 나오면 Gradle 이 예전 Java 로 실행된 것입니다.
> JDK 17 을 설치하고 `JAVA_HOME` 을 그 폴더로 바꾼 뒤 `gradlew --stop` 으로 예전 Gradle 데몬을 끄고 다시 실행하세요.

http://localhost:8080 접속 후 로그인 화면의 **데모 계정으로 둘러보기**를 누르면 글쓰기·수정·삭제, 목격 제보, 신고 관리(/admin)까지 해 볼 수 있어요. (`demo` 프로필에서만 켜집니다)

실제 소셜 로그인을 쓰려면 환경 변수로 키를 넣거나, `application-oauth.properties.example` 을 `src/main/resources/application-oauth.properties` 로 복사해 채웁니다 (`.gitignore` 되어 있음).

| 환경 변수 | 설명 |
|---|---|
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | 구글 로그인 |
| `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` | 네이버 로그인 |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` | 카카오 로그인 |
| `KAKAO_MAP_KEY` | 카카오 지도 JavaScript 키 (사이트 도메인 등록 필요) |
| `DATA_GO_KR_SERVICE_KEY` | 공공데이터포털 구조동물 조회 서비스 인증키 (없으면 화면 수집으로 대체) |
| `FILE_UPLOAD_DIR` | 사진 저장 폴더 (기본 `./uploads`) |
| `ADMIN_EMAILS` | 관리자 이메일 (쉼표로 여러 명). 이 이메일로 로그인하면 /admin 사용 |
| `SITE_URL` | 사이트 주소. 전단지 QR 과 sitemap 에 쓰여요 (없으면 요청 주소) |

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

작성자 확인(403), 로그인 확인(401), CSRF, 입력 검증, 파일 이름 경로 조작, 목격 제보 권한과 연락처 노출, 신고 자동 숨김, 비슷한 제보·내 주변 거리 계산, 썸네일, 도배 방지, 전단지·QR·사이트맵, 공공 API 응답 해석을 검사합니다. GitHub Actions 에서 push·PR 마다 실행됩니다.

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
