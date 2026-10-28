package com.greentea.findmydog.springboot.web.dto;

import java.util.List;

/** 관리자 화면의 신고된 글 한 줄 */
public record ReportedPostDto(PostsResponseDto post, long reportCount, List<String> reasons) {
}
