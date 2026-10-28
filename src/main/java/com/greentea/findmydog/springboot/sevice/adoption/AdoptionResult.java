package com.greentea.findmydog.springboot.sevice.adoption;

import java.util.List;

/**
 * 입양 공고 한 페이지.
 * source: api (공공데이터포털) / scrape (키가 없을 때 국가동물보호정보시스템 화면에서 수집)
 * totalCount 는 공공 API 일 때만 정확하고, 스크래핑일 때는 -1 이다.
 */
public record AdoptionResult(List<ShelterAnimal> items, int totalCount, int page, int pageSize, boolean hasNext, String source) {
}
