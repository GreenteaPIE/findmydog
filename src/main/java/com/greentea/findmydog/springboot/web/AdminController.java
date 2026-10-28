package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import com.greentea.findmydog.springboot.sevice.posts.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 관리자 전용 (/admin, /api/admin/**). ROLE_ADMIN 은 ADMIN_EMAILS 로 정한다. */
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final ReportService reportService;
    private final PostsService postsService;

    @GetMapping("/admin")
    public String reports(Model model) {
        model.addAttribute("reported", reportService.reportedPosts());
        return "admin";
    }

    @ResponseBody
    @PostMapping("/api/admin/posts/{id}/hide")
    public ResponseEntity<Void> hide(@PathVariable Long id) {
        postsService.setHidden(id, true);
        return ResponseEntity.noContent().build();
    }

    // 문제없는 글: 신고를 지우고 다시 보이게
    @ResponseBody
    @PostMapping("/api/admin/posts/{id}/dismiss")
    public ResponseEntity<Void> dismiss(@PathVariable Long id) {
        reportService.dismiss(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseBody
    @DeleteMapping("/api/admin/posts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postsService.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
