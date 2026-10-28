package com.greentea.findmydog.springboot.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReportRequestDto(
        @Pattern(regexp = "^(광고·도배|사실이 아닌 글|부적절한 사진|개인정보 노출|기타)$", message = "신고 이유를 골라 주세요")
        String reason,
        @Size(max = 300, message = "자세한 내용은 300자까지 쓸 수 있어요")
        String detail) {
}
