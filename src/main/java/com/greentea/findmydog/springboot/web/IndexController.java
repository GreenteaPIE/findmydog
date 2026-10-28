package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.config.auth.LoginUser;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.sevice.adoption.AdoptionService;
import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import com.greentea.findmydog.springboot.sevice.posts.SightingService;
import com.greentea.findmydog.springboot.web.dto.PostsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final SightingService sightingService;
    private final AdoptionService adoptionService;

    // 메인: 최근 제보와 현황
    @GetMapping("/")
    public String index(Model model) {
        List<PostsResponseDto> recent = postsService.findRecent();
        model.addAttribute("posts", recent);
        // 첫 화면 전봇대에는 아직 찾고 있는 아이만 붙인다
        model.addAttribute("heroPosts", recent.stream().filter(p -> !p.isReunited()).limit(3).toList());
        model.addAttribute("lookingCount", postsService.countLooking());
        model.addAttribute("reunitedCount", postsService.countReunited());
        return "index";
    }

    // 제보 목록 (page 는 1부터)
    @GetMapping("/posts")
    public String list(@RequestParam(required = false) String kind,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) Double lat,
                       @RequestParam(required = false) Double lng,
                       @RequestParam(defaultValue = "1") int page, Model model) {
        Page<PostsResponseDto> posts = postsService.search(kind, status, q, lat, lng, page);
        model.addAttribute("posts", posts);
        model.addAttribute("kind", kind);
        model.addAttribute("status", status);
        model.addAttribute("q", q);
        model.addAttribute("lat", lat);
        model.addAttribute("lng", lng);
        model.addAttribute("nearKm", (int) PostsService.NEAR_KM);
        model.addAttribute("page", Math.max(page, 1));
        // 페이지 번호는 지금 페이지 앞뒤로 2개씩
        model.addAttribute("startPage", Math.max(1, page - 2));
        model.addAttribute("endPage", Math.max(1, Math.min(posts.getTotalPages(), page + 2)));
        model.addAttribute("pageTitle", Posts.KIND_LOST.equals(kind) ? "실종 제보" : Posts.KIND_FOUND.equals(kind) ? "발견 제보" : "모든 제보");
        return "posts/list";
    }

    // 제보 상세: 목격 제보와 비슷한 제보를 함께 보여 준다
    @GetMapping("/posts/{id:\\d+}")
    public String detail(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        Long viewerId = user == null ? null : user.getId();
        boolean admin = user != null && user.isAdmin();
        PostsResponseDto post = postsService.findVisible(id, viewerId, admin);
        model.addAttribute("post", post);
        model.addAttribute("isOwner", viewerId != null && viewerId.equals(post.getUser_id()));
        model.addAttribute("isAdmin", admin);
        model.addAttribute("sightings", sightingService.list(id, viewerId, admin));
        model.addAttribute("similar", post.isReunited() ? List.of() : postsService.findSimilar(id, 4));
        return "posts/detail";
    }

    // 인쇄용 A4 전단지. 연락처는 글쓴이가 볼 때만 들어간다
    @GetMapping("/posts/{id:\\d+}/flyer")
    public String flyer(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        Long viewerId = user == null ? null : user.getId();
        PostsResponseDto post = postsService.findVisible(id, viewerId, user != null && user.isAdmin());
        model.addAttribute("post", post);
        model.addAttribute("isOwner", viewerId != null && viewerId.equals(post.getUser_id()));
        return "posts/flyer";
    }

    // 제보 쓰기
    @GetMapping("/posts/new")
    public String create(@RequestParam(required = false) String kind, Model model) {
        model.addAttribute("post", null);
        model.addAttribute("kind", Posts.KIND_FOUND.equals(kind) ? Posts.KIND_FOUND : Posts.KIND_LOST);
        model.addAttribute("maxImages", PostsService.MAX_IMAGES);
        return "posts/form";
    }

    // 제보 고치기 (작성자만)
    @GetMapping("/posts/{id:\\d+}/edit")
    public String edit(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        PostsResponseDto post = postsService.findForEdit(id, user.getId());
        model.addAttribute("post", post);
        model.addAttribute("kind", post.getKind());
        model.addAttribute("maxImages", PostsService.MAX_IMAGES);
        return "posts/form";
    }

    // 내가 쓴 제보
    @GetMapping("/my/posts")
    public String myPosts(@LoginUser SessionUser user, Model model) {
        model.addAttribute("posts", postsService.findByUser(user.getId()));
        return "posts/mine";
    }

    @GetMapping("/map")
    public String map() {
        return "map";
    }

    @GetMapping("/adoption")
    public String adoption(Model model) {
        model.addAttribute("officialApi", adoptionService.usesOfficialApi());
        return "adoption";
    }

    @GetMapping("/guide")
    public String guide() {
        return "guide";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/login")
    public String login(@LoginUser SessionUser user) {
        return user == null ? "login" : "redirect:/";
    }

    // ---------- 예전 주소 ----------

    @GetMapping("/posts/paging")
    public String oldList() {
        // kind·searchTitle 같은 예전 검색 조건을 새 이름으로 옮긴다
        String query = ServletUriComponentsBuilder.fromCurrentRequest().build().getQuery();
        return "redirect:/posts" + (query == null ? "" : "?" + query.replace("searchTitle=", "q="));
    }

    @GetMapping("/posts/detail/{id}")
    public String oldDetail(@PathVariable Long id) {
        return "redirect:/posts/" + id;
    }

    @GetMapping("/posts/save")
    public String oldSave() {
        return "redirect:/posts/new";
    }

    @GetMapping("/posts/update/{id}")
    public String oldUpdate(@PathVariable Long id) {
        return "redirect:/posts/" + id + "/edit";
    }

    @GetMapping("/find/map")
    public String oldMap() {
        return "redirect:/map";
    }

    @GetMapping("/posts/notice")
    public String oldNotice() {
        return "redirect:/guide";
    }

    @GetMapping({"/loginPage", "/user/posts"})
    public String oldLogin(@LoginUser SessionUser user) {
        return user == null ? "redirect:/login" : "redirect:/my/posts";
    }
}
