package com.greentea.findmydog.springboot.sevice.adoption;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 공공 API 키가 없을 때 쓰는 대체 수단. 국가동물보호정보시스템의 목록·상세 화면을 읽는다.
 * 화면 구조가 바뀌면 깨질 수 있으므로 운영에서는 DATA_GO_KR_SERVICE_KEY 를 설정하는 것을 권장한다.
 */
@Slf4j
@Service
public class WebScrapingService {

    private static final String LIST_URL = "https://www.animal.go.kr/front/awtis/protection/protectionList.do";
    private static final int TIMEOUT_MS = 8000;

    private static Connection connect(String url) {
        return Jsoup.connect(url).timeout(TIMEOUT_MS).userAgent("Mozilla/5.0 (FindMyDog)");
    }

    public List<ShelterAnimal> scrape(AdoptionQuery q, int pageSize) {
        List<ShelterAnimal> result = new ArrayList<>();
        try {
            Document doc = connect(LIST_URL)
                    .data("page", String.valueOf(q.page()))
                    .data("pageSize", String.valueOf(pageSize))
                    .data("menuNo", "1000000060")
                    .data("searchSDate", q.from().toString())
                    .data("searchEDate", q.to().toString())
                    .data("searchUprCd", q.sido() == null ? "" : q.sido())
                    .data("searchOrgCd", q.sigungu() == null ? "" : q.sigungu())
                    .data("searchUpKindCd", AdoptionService.DOG)
                    .data("searchKindCd", q.breed() == null ? "" : q.breed())
                    .get();
            for (Element li : doc.select("li")) {
                String[] parts = li.select("a").attr("onclick").split("'");
                if (parts.length > 1 && result.size() < pageSize) {
                    detail(parts[1]).ifPresent(result::add);
                }
            }
        } catch (IOException e) {
            // 빈 목록으로 돌려주면 '공고가 없다'로 보이므로, 못 읽었다는 사실을 알린다
            throw new IllegalStateException("국가동물보호정보시스템에 연결하지 못했어요: " + e.getMessage(), e);
        }
        return result;
    }

    private Optional<ShelterAnimal> detail(String desertionNo) {
        String url = ShelterAnimal.DETAIL_URL + desertionNo;
        try {
            Document doc = connect(url).get();
            String image = doc.select("ul.thum-list img.photoArea").attr("src");
            String received = cell(doc, "접수일시");
            String[] ageWeight = cell(doc, "나이/체중").split("/");
            return Optional.of(new ShelterAnimal(
                    desertionNo,
                    cell(doc, "공고번호"),
                    image.isEmpty() ? "" : "https://www.animal.go.kr" + image,
                    cell(doc, "품종").replaceFirst("^\\[[^]]*]\\s*", ""),
                    cell(doc, "색상"),
                    cell(doc, "성별"),
                    cell(doc, "중성화"),
                    ageWeight[0].trim(),
                    ageWeight.length > 1 ? ageWeight[1].trim() : "",
                    received.length() >= 10 ? received.substring(0, 10) : received,
                    cell(doc, "발생장소"),
                    cell(doc, "특징"),
                    cell(doc, "보호센터"),
                    cell(doc, "연락처"),
                    cell(doc, "보호장소"),
                    cell(doc, "관할기관"),
                    "", "", cell(doc, "상태"),
                    url));
        } catch (IOException e) {
            log.warn("입양 공고 상세({})를 읽지 못했어요: {}", desertionNo, e.getMessage());
            return Optional.empty();
        }
    }

    private static String cell(Document doc, String header) {
        return doc.select("th:contains(" + header + ") + td").text();
    }
}
