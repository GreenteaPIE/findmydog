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

        Path folderPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        for (MultipartFile file : imageFiles) {
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
            if (!extension.equals(".jpeg") && !extension.equals(".jpg") && !extension.equals(".png")) {
                throw new IOException("Unsupported file type: " + extension);
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

        posts.update(requestDto.getTitle());

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

    @Transactional(readOnly = true)
    public List<PostsResponseDto> findAllMap(){
        return postsRepository.findAllDesc().stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id){
        // 해당 게시글 찾기
        Posts posts = postsRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        // 해당 게시글에 연관된 이미지 정보 조회
        List<Image> images = imageRepository.findByPost(posts);

        // 파일 시스템에서 이미지 파일 삭제
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                Path folderPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Path filePath = folderPath.resolve(image.getStoredFileName());
                try {
                    Files.deleteIfExists(filePath); // 파일이 존재할 경우 삭제
                } catch (IOException e) {
                    e.printStackTrace(); // 오류 처리
                }
            }
        }

        // 데이터베이스에서 이미지 정보 삭제
        imageRepository.deleteAll(images);

        // 데이터베이스에서 게시글 정보 삭제
        postsRepository.delete(posts);
    }
}