package com.greentea.findmydog.springboot.domain.posts;

import com.greentea.findmydog.springboot.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Content extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reporterName;    // 분실신고자 이름
    private String contact;         // 연락처
    private LocalDateTime lostDate; // 분실일자
    private String landmark;        // 주위 대표건물
    private String breed;           // 품종
    private String color;           // 색상
    private String gender;          // 성별
    private int age;                // 나이
    private String features;        // 특징
    private boolean hasMicrochip;   // 내장칩 유무
    private double latitude;        // 위도
    private double longitude;       // 경도

    @OneToOne(mappedBy = "content")
    private Posts post;

    @Builder
    public Content(String reporterName, String contact, LocalDateTime lostDate, String landmark, String breed, String color, String gender, int age, String features, boolean hasMicrochip, double latitude, double longitude) {
        this.reporterName = reporterName;
        this.contact = contact;
        this.lostDate = lostDate;
        this.landmark = landmark;
        this.breed = breed;
        this.color = color;
        this.gender = gender;
        this.age = age;
        this.features = features;
        this.hasMicrochip = hasMicrochip;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

