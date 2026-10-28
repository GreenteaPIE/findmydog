package com.greentea.findmydog.springboot.sevice.adoption;

/** 보호소에 있는 동물 한 마리 (입양 공고). 공공 API 와 스크래핑 결과를 같은 모양으로 맞춘다. */
public record ShelterAnimal(
        String desertionNo,
        String noticeNo,
        String imageUrl,
        String breed,
        String color,
        String sex,          // 수컷 / 암컷 / 미상
        String neuter,       // 예 / 아니오 / 미상
        String age,
        String weight,
        String happenDate,   // yyyy-MM-dd
        String happenPlace,
        String specialMark,
        String careName,
        String careTel,
        String careAddr,
        String orgName,
        String noticeStart,
        String noticeEnd,
        String state,
        String detailUrl) {

    static final String DETAIL_URL = "https://www.animal.go.kr/front/awtis/protection/protectionDtl.do?desertionNo=";
}
