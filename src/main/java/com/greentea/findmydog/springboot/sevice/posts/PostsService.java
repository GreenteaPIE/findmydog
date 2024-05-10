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
    private String uploadDir;

    public Long save(PostsSaveRequestDto requestDto, List<MultipartFile> imageFiles) throws IOException {
        Posts post = postsRepository.save(requestDto.toEntity());

        Path folderPath = Paths.get(uploadDir).toAbsolutePath().normalize(); // 절대 경로로 정규화
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS"); // 밀리초까지 포함하여 더욱 고유한 파일명 생성

        for (MultipartFile file : imageFiles) {
            // 파일 확장자 검사 (예시: JPEG, PNG 파일만 허용)
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
            if (!extension.equals(".jpeg") && !extension.equals(".jpg") && !extension.equals(".png")) {
                throw new IOException("Unsupported file type: " + extension); // 지원하지 않는 파일 형식 예외 처리
            }

            String storedFileName = originalFileName.substring(0, originalFileName.lastIndexOf(".")) + "_" + sdf.format(new Date()) + extension;
            Path filePath = folderPath.resolve(storedFileName);

            file.transferTo(filePath.toFile());

            Image image = new Image();
            image.setOriginalFileName(originalFileName);
            image.setStoredFileName(storedFileName);
            image.setPost(post);
            imageRepository.save(image);
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