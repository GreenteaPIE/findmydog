package com.greentea.findmydog.springboot.sevice.adoption;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 공공데이터포털 '구조동물 조회 서비스' 응답(JSON)을 ShelterAnimal 로 바꾼다.
 * 버전마다 필드 이름이 조금씩 달라서 (popfile1/popfile, kindNm/kindCd 등) 있는 값을 차례로 쓴다.
 */
final class AnimalApiParser {

    private AnimalApiParser() {
    }

    static JsonNode body(JsonNode root) {
        JsonNode header = root.path("response").path("header");
        String code = header.path("resultCode").asText("");
        if (!code.isEmpty() && !"00".equals(code)) {
            throw new IllegalStateException("공공 API 오류: " + code + " " + header.path("resultMsg").asText());
        }
        return root.path("response").path("body");
    }

    /** item 이 하나면 객체, 없으면 빈 문자열로 오기도 해서 모두 리스트로 맞춘다 */
    static List<JsonNode> items(JsonNode body) {
        JsonNode item = body.path("items").path("item");
        List<JsonNode> list = new ArrayList<>();
        if (item.isArray()) item.forEach(list::add);
        else if (item.isObject()) list.add(item);
        return list;
    }

    static ShelterAnimal animal(JsonNode n) {
        String desertionNo = text(n, "desertionNo");
        return new ShelterAnimal(
                desertionNo,
                text(n, "noticeNo"),
                text(n, "popfile1", "popfile", "filename"),
                breed(n),
                text(n, "colorCd"),
                sex(text(n, "sexCd")),
                neuter(text(n, "neuterYn")),
                text(n, "age").replace("(년생)", "년생"),
                text(n, "weight").replace("(Kg)", "kg"),
                date(text(n, "happenDt")),
                text(n, "happenPlace"),
                text(n, "specialMark"),
                text(n, "careNm"),
                text(n, "careTel"),
                text(n, "careAddr"),
                text(n, "orgNm"),
                date(text(n, "noticeSdt")),
                date(text(n, "noticeEdt")),
                text(n, "processState"),
                desertionNo.isEmpty() ? null : ShelterAnimal.DETAIL_URL + desertionNo);
    }

    private static String breed(JsonNode n) {
        String name = text(n, "kindNm", "kindFullNm", "kindCd");
        return name.replaceFirst("^\\[[^]]*]\\s*", ""); // "[개] 믹스견" → "믹스견"
    }

    static String text(JsonNode n, String... names) {
        for (String name : names) {
            String value = n.path(name).asText("").trim();
            if (!value.isEmpty() && !"null".equals(value)) return value;
        }
        return "";
    }

    static String date(String yyyymmdd) {
        return yyyymmdd.matches("\\d{8}")
                ? yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6)
                : yyyymmdd;
    }

    static String sex(String code) {
        return switch (code) {
            case "M" -> "수컷";
            case "F" -> "암컷";
            default -> "미상";
        };
    }

    static String neuter(String code) {
        return switch (code) {
            case "Y" -> "예";
            case "N" -> "아니오";
            default -> "미상";
        };
    }
}
