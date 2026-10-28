package com.greentea.findmydog.springboot.sevice.adoption;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnimalApiParserTest {

    private final AdoptionService service = new AdoptionService("", new WebScrapingService(), new ObjectMapper());

    @Test
    void 공공_API_응답을_읽는다() {
        String json = """
                {"response":{"header":{"resultCode":"00","resultMsg":"NORMAL SERVICE."},
                 "body":{"items":{"item":[{"desertionNo":"411301202500123","noticeNo":"서울-마포-2025-00123",
                   "popfile1":"https://www.animal.go.kr/files/shelter/2025/10/a.jpg","kindNm":"말티즈","colorCd":"흰색",
                   "age":"2021(년생)","weight":"3.2(Kg)","sexCd":"F","neuterYn":"Y","happenDt":"20251020",
                   "happenPlace":"망원동 123","specialMark":"분홍 하네스","careNm":"마포구 동물보호센터","careTel":"02-123-4567",
                   "careAddr":"서울특별시 마포구","orgNm":"서울특별시 마포구","noticeSdt":"20251021","noticeEdt":"20251031","processState":"보호중"}]},
                 "numOfRows":12,"pageNo":1,"totalCount":30}}}
                """;
        AdoptionResult result = service.parse(json, 1);

        assertThat(result.totalCount()).isEqualTo(30);
        assertThat(result.hasNext()).isTrue();
        ShelterAnimal a = result.items().get(0);
        assertThat(a.breed()).isEqualTo("말티즈");
        assertThat(a.sex()).isEqualTo("암컷");
        assertThat(a.neuter()).isEqualTo("예");
        assertThat(a.age()).isEqualTo("2021년생");
        assertThat(a.weight()).isEqualTo("3.2kg");
        assertThat(a.happenDate()).isEqualTo("2025-10-20");
        assertThat(a.detailUrl()).endsWith("desertionNo=411301202500123");
    }

    @Test
    void 예전_필드_이름과_한_건짜리_응답도_읽는다() {
        String json = """
                {"response":{"header":{"resultCode":"00"},"body":{"items":{"item":
                 {"desertionNo":"1","popfile":"http://x/y.jpg","kindCd":"[개] 믹스견","sexCd":"Q","neuterYn":"U"}},"totalCount":1}}}
                """;
        ShelterAnimal a = service.parse(json, 1).items().get(0);
        assertThat(a.breed()).isEqualTo("믹스견");
        assertThat(a.imageUrl()).isEqualTo("http://x/y.jpg");
        assertThat(a.sex()).isEqualTo("미상");
    }

    @Test
    void 결과가_없으면_빈_목록() {
        String json = "{\"response\":{\"header\":{\"resultCode\":\"00\"},\"body\":{\"items\":\"\",\"totalCount\":0}}}";
        assertThat(service.parse(json, 1).items()).isEmpty();
    }

    @Test
    void 오류_코드면_예외() {
        String json = "{\"response\":{\"header\":{\"resultCode\":\"30\",\"resultMsg\":\"SERVICE_KEY_IS_NOT_REGISTERED_ERROR\"}}}";
        assertThatThrownBy(() -> service.parse(json, 1)).hasMessageContaining("30");
    }

    @Test
    void 인코딩된_키도_한_번만_인코딩해서_보낸다() {
        AdoptionService withKey = new AdoptionService("abc%2Bdef%3D%3D", new WebScrapingService(), new ObjectMapper());
        String uri = withKey.apiUri(new AdoptionQuery(LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 28), "6110000", null, null, 2)).toString();
        assertThat(uri).contains("serviceKey=abc%2Bdef%3D%3D").contains("bgnde=20251001").contains("upr_cd=6110000").contains("pageNo=2")
                .doesNotContain("org_cd");
    }
}
