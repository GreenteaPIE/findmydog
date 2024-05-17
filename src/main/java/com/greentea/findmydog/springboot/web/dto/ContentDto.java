package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Content;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ContentDto {
    private Long id;
    private String reporterName;
    private String contact;
    private LocalDateTime lostDate;
    private String landmark;
    private String breed;
    private String color;
    private String gender;
    private int age;
    private String features;
    private boolean hasMicrochip;
    private double latitude;
    private double longitude;

    public ContentDto(Content entity) {
        this.id = entity.getId();
        this.reporterName = entity.getReporterName();
        this.contact = entity.getContact();
        this.lostDate = entity.getLostDate();
        this.landmark = entity.getLandmark();
        this.breed = entity.getBreed();
        this.color = entity.getColor();
        this.gender = entity.getGender();
        this.age = entity.getAge();
        this.features = entity.getFeatures();
        this.hasMicrochip = entity.isHasMicrochip();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
    }

    @Builder
    public ContentDto(String reporterName, String contact, LocalDateTime lostDate, String landmark, String breed, String color, String gender, int age, String features, boolean hasMicrochip, double latitude, double longitude) {
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

    public Content toEntity() {
        return Content.builder()
                .reporterName(reporterName)
                .contact(contact)
                .lostDate(lostDate)
                .landmark(landmark)
                .breed(breed)
                .color(color)
                .gender(gender)
                .age(age)
                .features(features)
                .hasMicrochip(hasMicrochip)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
