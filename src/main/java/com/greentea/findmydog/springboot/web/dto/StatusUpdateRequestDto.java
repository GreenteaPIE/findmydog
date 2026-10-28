package com.greentea.findmydog.springboot.web.dto;

import jakarta.validation.constraints.Pattern;

/** L: 찾고 있어요, F: 가족을 찾았어요 */
public record StatusUpdateRequestDto(@Pattern(regexp = "^[LF]$", message = "상태는 L 또는 F 예요") String status) {
}
