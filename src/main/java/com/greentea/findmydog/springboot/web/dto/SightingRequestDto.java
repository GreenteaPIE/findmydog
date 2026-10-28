package com.greentea.findmydog.springboot.web.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/** 목격 제보 입력. 지도 위치는 고르지 않아도 되고, 연락처는 글쓴이에게만 보인다. */
public record SightingRequestDto(
        @NotNull(message = "본 시간을 입력해 주세요")
        @PastOrPresent(message = "본 시간은 지금보다 늦을 수 없어요")
        LocalDateTime seenAt,

        @NotBlank(message = "본 곳을 입력해 주세요")
        @Size(max = 100, message = "본 곳은 100자까지 쓸 수 있어요")
        String place,

        @DecimalMin("33.0") @DecimalMax("39.0") Double latitude,
        @DecimalMin("124.0") @DecimalMax("132.0") Double longitude,

        @Size(max = 500, message = "메모는 500자까지 쓸 수 있어요")
        String note,

        @Pattern(regexp = "^$|^0\\d{1,2}-?\\d{3,4}-?\\d{4}$", message = "연락처는 010-1234-5678 형식으로 입력해 주세요")
        String contact) {
}
