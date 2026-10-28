package com.greentea.findmydog.springboot.web.dto;

import com.greentea.findmydog.springboot.domain.posts.Content;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ContentDto {
    private Long id;

    @NotBlank(message = "신고자 이름을 입력해 주세요")
    @Size(max = 30, message = "신고자 이름은 30자까지 쓸 수 있어요")
    private String reporterName;

    @NotBlank(message = "연락처를 입력해 주세요")
    @Pattern(regexp = "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$", message = "연락처는 010-1234-5678 형식으로 입력해 주세요")
    private String contact;

    @NotNull(message = "분실·발견 일시를 입력해 주세요")
    @PastOrPresent(message = "분실·발견 일시는 지금보다 늦을 수 없어요")
    private LocalDateTime lostDate;

    @Size(max = 100, message = "주위 건물은 100자까지 쓸 수 있어요")
    private String landmark;

    @Size(max = 200)
    private String address;

    @NotBlank(message = "품종을 입력해 주세요")
    @Size(max = 50, message = "품종은 50자까지 쓸 수 있어요")
    private String breed;

    @Size(max = 30, message = "이름은 30자까지 쓸 수 있어요")
    private String pname;

    @NotBlank(message = "색상을 입력해 주세요")
    @Size(max = 30, message = "색상은 30자까지 쓸 수 있어요")
    private String color;

    @Pattern(regexp = "^(암컷|수컷|모름)$", message = "성별을 골라 주세요")
    private String gender;

    @Min(value = 0, message = "나이는 0살 이상이에요")
    @Max(value = 30, message = "나이는 30살까지 입력할 수 있어요")
    private Integer age;

    @Size(max = 1000, message = "특징은 1000자까지 쓸 수 있어요")
    private String features;

    private boolean hasMicrochip;

    // 대한민국 범위 안의 좌표만 받는다
    @DecimalMin(value = "33.0", message = "지도에서 위치를 골라 주세요")
    @DecimalMax(value = "39.0", message = "지도에서 위치를 골라 주세요")
    private double latitude;

    @DecimalMin(value = "124.0", message = "지도에서 위치를 골라 주세요")
    @DecimalMax(value = "132.0", message = "지도에서 위치를 골라 주세요")
    private double longitude;

    public ContentDto(Content entity) {
        this.id = entity.getId();
        this.reporterName = entity.getReporterName();
        this.contact = entity.getContact();
        this.lostDate = entity.getLostDate();
        this.landmark = entity.getLandmark();
        this.address = entity.getAddress();
        this.breed = entity.getBreed();
        this.pname = entity.getPname();
        this.color = entity.getColor();
        this.gender = entity.getGender();
        this.age = entity.getAge();
        this.features = entity.getFeatures();
        this.hasMicrochip = entity.isHasMicrochip();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
    }

    @Builder
    public ContentDto(String reporterName, String contact, LocalDateTime lostDate, String landmark, String address, String breed, String pname, String color, String gender, Integer age, String features, boolean hasMicrochip, double latitude, double longitude) {
        this.reporterName = reporterName;
        this.contact = contact;
        this.lostDate = lostDate;
        this.landmark = landmark;
        this.address = address;
        this.breed = breed;
        this.pname = pname;
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
                .address(address)
                .breed(breed)
                .pname(pname)
                .color(color)
                .gender(gender)
                .age(age)
                .features(features)
                .hasMicrochip(hasMicrochip)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    /** 수정할 때 엔티티에 값을 옮긴다 */
    public void applyTo(Content content) {
        content.setReporterName(reporterName);
        content.setContact(contact);
        content.setLostDate(lostDate);
        content.setLandmark(landmark);
        content.setAddress(address);
        content.setBreed(breed);
        content.setPname(pname);
        content.setColor(color);
        content.setGender(gender);
        content.setAge(age);
        content.setFeatures(features);
        content.setHasMicrochip(hasMicrochip);
        content.setLatitude(latitude);
        content.setLongitude(longitude);
    }
}
