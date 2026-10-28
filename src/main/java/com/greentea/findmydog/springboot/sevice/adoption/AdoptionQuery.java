package com.greentea.findmydog.springboot.sevice.adoption;

import java.time.LocalDate;

/** 입양 공고 검색 조건. 날짜가 없으면 최근 30일. */
public record AdoptionQuery(LocalDate from, LocalDate to, String sido, String sigungu, String breed, int page) {

    public AdoptionQuery {
        to = to == null ? LocalDate.now() : to;
        from = from == null ? to.minusDays(30) : from;
        if (from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        sido = blankToNull(sido);
        sigungu = blankToNull(sigungu);
        breed = blankToNull(breed);
        page = Math.max(page, 1);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    String cacheKey() {
        return String.join("|", from.toString(), to.toString(), String.valueOf(sido), String.valueOf(sigungu), String.valueOf(breed), String.valueOf(page));
    }
}
