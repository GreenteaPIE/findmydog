package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.config.auth.LoginUser;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import com.greentea.findmydog.springboot.sevice.posts.WebScrapingService;
import com.greentea.findmydog.springboot.web.dto.AnimalData;
import com.greentea.findmydog.springboot.web.dto.PostsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final WebScrapingService webScrapingService;

    // 인덱스 페이지
    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        model.addAttribute("posts", postsService.findAllDesc());
        if(user != null) {
            model.addAttribute("userName", user.getName());
            System.out.println("로그인 확인 "+ user.getName());
        }
        System.out.println("메인페이지 진입");
        return "index.html";
    }

    // 입양정보 페이지
    @GetMapping("/adoption")
    public String Find(@LoginUser SessionUser user, Model model) {
        List<AnimalData> animalDataList = webScrapingService.scrapeAnimalData();
        if(user != null) {
            model.addAttribute("userName", user.getName());
        }
        model.addAttribute("animalDataList", animalDataList);
        return "adoption-notice.html";
    }

    // 분실/발견 지도 페이지
    @GetMapping("/find/map")
    public String findMap(Model model, @LoginUser SessionUser user) {
        model.addAttribute("mapinfo",postsService.findAllMap());
        if(user != null) {
            model.addAttribute("userName", user.getName());
        }
        System.out.println("지도 페이지 진입");
        return "find-map.html";
    }

    // 로그인 페이지
    @GetMapping("/loginPage")
    public String login(){
        System.out.println("로그인 페이지 진입");
        return "loginPage.html";
    }

    // About 페이지
    @GetMapping("/about")
    public String about(@LoginUser SessionUser user, Model model){
        System.out.println("about 페이지 진입");
        if (user != null) {
            model.addAttribute("userName", user.getName());
        }
        return "about.html";
    }

    // 게시글 리스트 @PageableDefault(page = 1) : page는 기본으로 1페이지를 보여준다.
    @GetMapping("/posts/paging")
    public String paging(@PageableDefault(page = 1) Pageable pageable, @LoginUser SessionUser user, Model model) {
        Page<PostsResponseDto> postsPages = postsService.paging(pageable);

        if (user != null) {
            model.addAttribute("userName", user.getName());
        }

        /**
         * blockLimit : page 개수 설정
         * 현재 사용자가 선택한 페이지 앞 뒤로 3페이지씩만 보여준다.
         * ex : 현재 사용자가 4페이지라면 2, 3, (4), 5, 6
         */
        int blockLimit = 3;
        int startPage = (((int) Math.ceil(((double) pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
        int endPage = Math.min((startPage + blockLimit - 1), postsPages.getTotalPages());

        model.addAttribute("postsPages", postsPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "posts-paging";
    }

    // 게시글 작성폼
    @GetMapping("/posts/save")
    public String postsSave(Model model, @LoginUser SessionUser user){
        if(user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userId", user.getId());
            System.out.println("글 등록 진입 " + user.getName());
        }
        return "posts-save.html";
    }
    
    // 게시글 디테일 페이지
    @GetMapping("/posts/detail/{id}")
    public String postsDetail(Model model, @LoginUser SessionUser user, @PathVariable Long id){
        PostsResponseDto dto =postsService.findById(id);
        model.addAttribute("post", dto);
        if(user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userId", user.getId());
        }
        System.out.println("게시글 디테일 진입");
        return "posts-detail.html";
    }

    // 게시글 수정 페이지
    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, @LoginUser SessionUser user, Model model){
        PostsResponseDto dto= postsService.findById(id);
        model.addAttribute("post", dto);
        if(user != null) {
            model.addAttribute("userName", user.getName());
        }
        System.out.println("글 수정 진입");
        return "posts-update.html";
    }

}
