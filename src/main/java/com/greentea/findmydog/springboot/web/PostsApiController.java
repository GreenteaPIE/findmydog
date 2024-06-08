package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.sevice.posts.PostsService;
import com.greentea.findmydog.springboot.sevice.posts.WebScrapingService;
import com.greentea.findmydog.springboot.web.dto.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PostsApiController {

    private final PostsService postsService;
    private final WebScrapingService webScrapingService;

    // 게시글 저장 요청
    @PostMapping("/api/v1/posts")
    public Long save(@RequestPart("post") PostsSaveRequestDto requestDto, @RequestPart("images") List<MultipartFile> files) throws IOException {
        System.out.println("게시글 저장 동작");
        return postsService.save(requestDto, files);
    }

    // 게시글 수정 요청
    @PutMapping("/api/v1/posts/{id}")
    public Long update(@PathVariable Long id, @RequestPart("post") PostsUpdateRequestDto requestDto, @RequestPart(value = "images", required = false) List<MultipartFile> files) throws IOException {
        System.out.println("게시글 업데이트 동작");
        return postsService.update(id, requestDto, files);
    }

    // 게시글 id로 찾기
    @GetMapping("/api/v1/posts/{id}")
    public PostsResponseDto findById(@PathVariable Long id) {
        System.out.println(id + "이 작성한 게시물 동작");
        return postsService.findById(id);
    }

    // 게시글 삭제 요청
    @DeleteMapping("/api/v1/posts/{id}")
    public Long delete(@PathVariable Long id) {
        postsService.delete(id);
        System.out.println("게시물 삭제 동작");
        return id;
    }
    
    // 입양공고 크롤링 요청
    @PostMapping("/adoption/search")
    public List<AnimalDetailData> searchAnimals(@RequestParam String searchSDate, @RequestParam String searchEDate, @RequestParam String searchUprCd, @RequestParam String searchOrgCd, @RequestParam String searchKindCd, Model model) {
        List<AnimalDetailData> animalDetailDataList = webScrapingService.scrapeAnimalData(searchSDate, searchEDate, searchUprCd, searchOrgCd, searchKindCd);
        return animalDetailDataList;
    }
}