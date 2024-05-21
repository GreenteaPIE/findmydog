package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.config.auth.LoginUser;
import com.greentea.findmydog.springboot.config.auth.dto.SessionUser;
import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import com.greentea.findmydog.springboot.web.dto.PostsResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final HttpSession httpSession;

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        model.addAttribute("posts", postsService.findAllDesc());
        model.addAttribute("mapinfo",postsService.findAllMap());
        if(user != null) {
            model.addAttribute("userName", user.getName());
            System.out.println("로그인 확인 "+ user.getName());
        }
        System.out.println("메인페이지 진입");
        return "index.html";
    }

    @GetMapping("/loginPage")
    public String login(){
        System.out.println("로그인 페이지 진입");
        return "loginPage.html";
    }

    @GetMapping("/posts/list")
    public String postsList(Model model, @LoginUser SessionUser user) {
        model.addAttribute("posts", postsService.findAllDesc());
        if (user != null) {
            model.addAttribute("userName", user.getName());
        }
        System.out.println("글 리스트 진입");
        return "posts-list.html";
    }

    @GetMapping("/posts/save")
    public String postsSave(Model model, @LoginUser SessionUser user){
        if(user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userId", user.getId());
            System.out.println("글 등록 진입 " + user.getName());
        }
        return "posts-save.html";
    }
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

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model){
        PostsResponseDto dto= postsService.findById(id);
        model.addAttribute("post", dto);
        System.out.println("글 수정 진입");
        return "posts-update.html";
    }

}
