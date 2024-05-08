package com.greentea.findmydog.springboot.sevice.posts;

import com.greentea.findmydog.springboot.domain.posts.Image;
import com.greentea.findmydog.springboot.domain.posts.ImageRepository;
import com.greentea.findmydog.springboot.domain.posts.Posts;
import com.greentea.findmydog.springboot.domain.posts.PostsRepository;
import com.greentea.findmydog.springboot.web.dto.PostsListResponseDto;
import com.greentea.findmydog.springboot.web.dto.PostsResponseDto;
import com.greentea.findmydog.springboot.web.dto.PostsSaveRequestDto;
import com.greentea.findmydog.springboot.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

@RequiredArgsConstructor
@Service
public class PostsService {
    private final PostsRepository postsRepository;
    private final ImageRepository imageRepository;

    @Value("${file.upload-dir}")
    private String uploadDir; // 수정된 부분

    public Long save(PostsSaveRequestDto requestDto, List<MultipartFile> imageFiles) throws IOException {
        Posts post = postsRepository.save(requestDto.toEntity());

        Path folderPath = Paths.get(uploadDir); // 수정된 부분
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath); // 폴더가 없다면 생성. 읽기/쓰기 권한 확인 포함
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        for (MultipartFile file : imageFiles) {
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String storedFileName = originalFileName.substring(0, originalFileName.lastIndexOf(".")) + "_" + sdf.format(new Date()) + extension; // 생성 시간을 포함한 파일명
            Path filePath = folderPath.resolve(storedFileName); // 수정된 부분

            file.transferTo(filePath.toFile()); // 파일 저장

            Image image = new Image();
            image.setOriginalFileName(originalFileName);
            image.setStoredFileName(storedFileName);
            image.setPost(post); // Image 엔티티에 Post 엔티티 연결
            imageRepository.save(image); // Image 엔티티 저장
        }

        return post.getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto){
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id="+ id));

        posts.update(requestDto.getTitle(), requestDto.getContent());

        return id;
    }
    public PostsResponseDto findById(Long id){
        Posts entity = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        return new PostsResponseDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PostsListResponseDto> findAllDesc(){
        return postsRepository.findAllDesc().stream()
                .map(PostsListResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id){
        Posts posts = postsRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        postsRepository.delete(posts);
    }
}