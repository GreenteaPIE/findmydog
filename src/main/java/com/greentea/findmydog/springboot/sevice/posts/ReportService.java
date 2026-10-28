package com.greentea.findmydog.springboot.sevice.posts;

import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.posts.PostsRepository;
import com.greentea.findmydog.springboot.domain.posts.Report;
import com.greentea.findmydog.springboot.domain.posts.ReportRepository;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.exception.ConflictException;
import com.greentea.findmydog.springboot.exception.PostNotFoundException;
import com.greentea.findmydog.springboot.web.dto.PostsResponseDto;
import com.greentea.findmydog.springboot.web.dto.ReportRequestDto;
import com.greentea.findmydog.springboot.web.dto.ReportedPostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    /** 서로 다른 사람의 신고가 이만큼 쌓이면 관리자가 보기 전까지 글을 숨긴다 */
    public static final int AUTO_HIDE_AT = 3;

    private final ReportRepository reportRepository;
    private final PostsRepository postsRepository;
    private final UserRepository userRepository;

    /** @return 신고 뒤 글이 숨겨졌는지 */
    @Transactional
    public boolean report(Long postId, ReportRequestDto dto, Long userId) {
        Posts post = postsRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        Users user = userRepository.findById(userId).orElseThrow(() -> new AccessDeniedException("로그인 정보를 찾을 수 없어요"));
        if (post.isOwnedBy(userId)) {
            throw new IllegalArgumentException("내 글은 신고할 수 없어요");
        }
        if (reportRepository.existsByPostAndUser(post, user)) {
            throw new ConflictException("이미 신고한 글이에요");
        }
        reportRepository.save(Report.builder().post(post).user(user).reason(dto.reason())
                .detail(StringUtils.hasText(dto.detail()) ? dto.detail().trim() : null).build());
        if (reportRepository.countByPost(post) >= AUTO_HIDE_AT && !post.isHidden()) {
            post.hide();
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<ReportedPostDto> reportedPosts() {
        return reportRepository.findReportedPosts().stream().map(post -> {
            List<Report> reports = reportRepository.findByPostOrderByIdDesc(post);
            List<String> reasons = reports.stream()
                    .map(r -> r.getDetail() == null ? r.getReason() : r.getReason() + ": " + r.getDetail())
                    .toList();
            return new ReportedPostDto(new PostsResponseDto(post), reports.size(), reasons);
        }).toList();
    }

    // 관리자가 문제없다고 본 글: 신고를 지우고 다시 보이게 한다
    @Transactional
    public void dismiss(Long postId) {
        Posts post = postsRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        post.getReports().clear();
        post.restore();
    }
}
