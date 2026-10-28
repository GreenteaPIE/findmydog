package com.greentea.findmydog.springboot.sevice.adoption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 유기동물 입양 공고.
 * DATA_GO_KR_SERVICE_KEY 가 있으면 공공데이터포털 '구조동물 조회 서비스'를 쓰고,
 * 없으면 국가동물보호정보시스템 화면을 읽어 오는 방식(WebScrapingService)으로 대신한다.
 */
@Slf4j
@Service
public class AdoptionService {

    static final String API_URL = "https://apis.data.go.kr/1543061/abandonmentPublicService_v2/abandonmentPublic_v2";
    static final String DOG = "417000";
    static final int PAGE_SIZE = 12;
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;

    private final String serviceKey;
    private final WebScrapingService webScrapingService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final JsonNode codes;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    private record Cached(Instant at, AdoptionResult result) {
    }

    public AdoptionService(@Value("${adoption.service-key:}") String serviceKey,
                           WebScrapingService webScrapingService, ObjectMapper objectMapper) {
        // 포털의 '인코딩' 키를 넣어도 두 번 인코딩되지 않도록 한 번 풀어 둔다
        this.serviceKey = serviceKey.contains("%") ? URLDecoder.decode(serviceKey, StandardCharsets.UTF_8) : serviceKey.trim();
        this.webScrapingService = webScrapingService;
        this.objectMapper = objectMapper;
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.codes = loadCodes(objectMapper);
    }

    private static JsonNode loadCodes(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("adoption-codes.json").getInputStream()) {
            return objectMapper.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** 시도·시군구·품종 코드표 (화면의 선택 목록용) */
    public JsonNode codes() {
        return codes;
    }

    public boolean usesOfficialApi() {
        return !serviceKey.isEmpty();
    }

    public AdoptionResult search(AdoptionQuery query) {
        Cached hit = cache.get(query.cacheKey());
        if (hit != null && hit.at().plus(CACHE_TTL).isAfter(Instant.now())) {
            return hit.result();
        }
        AdoptionResult result = usesOfficialApi() ? searchApi(query) : searchByScraping(query);
        if (cache.size() > 500) cache.clear();
        cache.put(query.cacheKey(), new Cached(Instant.now(), result));
        return result;
    }

    URI apiUri(AdoptionQuery q) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(API_URL)
                // data.go.kr 은 키의 + / = 가 꼭 %2B %2F %3D 로 와야 해서 직접 인코딩한다 (나머지 값은 숫자와 영문)
                .queryParam("serviceKey", URLEncoder.encode(serviceKey, StandardCharsets.UTF_8))
                .queryParam("bgnde", q.from().format(YYYYMMDD))
                .queryParam("endde", q.to().format(YYYYMMDD))
                .queryParam("upkind", DOG)
                .queryParam("pageNo", q.page())
                .queryParam("numOfRows", PAGE_SIZE)
                .queryParam("_type", "json");
        if (q.breed() != null) uri.queryParam("kind", q.breed());
        if (q.sido() != null) uri.queryParam("upr_cd", q.sido());
        if (q.sigungu() != null) uri.queryParam("org_cd", q.sigungu());
        return uri.build(true).toUri();
    }

    private AdoptionResult searchApi(AdoptionQuery q) {
        String json = restClient.get().uri(apiUri(q)).retrieve().body(String.class);
        return parse(json, q.page());
    }

    AdoptionResult parse(String json, int page) {
        try {
            JsonNode body = AnimalApiParser.body(objectMapper.readTree(json));
            List<ShelterAnimal> items = AnimalApiParser.items(body).stream().map(AnimalApiParser::animal).toList();
            int total = body.path("totalCount").asInt(items.size());
            return new AdoptionResult(items, total, page, PAGE_SIZE, (long) page * PAGE_SIZE < total, "api");
        } catch (IOException e) {
            // 키가 잘못되면 JSON 대신 XML 오류가 오기도 한다
            throw new IllegalStateException("공공 API 응답을 읽지 못했어요", e);
        }
    }

    private AdoptionResult searchByScraping(AdoptionQuery q) {
        List<ShelterAnimal> items = webScrapingService.scrape(q, PAGE_SIZE);
        return new AdoptionResult(items, -1, q.page(), PAGE_SIZE, items.size() == PAGE_SIZE, "scrape");
    }
}
