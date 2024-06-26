package com.greentea.findmydog.springboot.sevice.posts;

import com.greentea.findmydog.springboot.domain.posts.*;
import com.greentea.findmydog.springboot.domain.posts.Image;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 게시글 저장
    public Long save(PostsSaveRequestDto requestDto, List<MultipartFile> imageFiles) throws IOException {
        Users user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found. ID: " + requestDto.getUserId()));

        Posts post = requestDto.toEntity(user);
        postsRepository.save(post);
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

            // 이미지를 790x680으로 리사이즈하고 공백 처리
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = resizeImage(originalImage, 790, 680);

            ImageIO.write(resizedImage, extension.substring(1), filePath.toFile());

            Image image = new Image();
            image.setOriginalFileName(originalFileName);
            image.setStoredFileName(storedFileName);
            image.setPost(post);
            imageRepository.save(image);
        }

        return post.getId();
    }

    // 게시물 수정
    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto, List<MultipartFile> imageFiles) throws IOException {
        Posts post = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        post.update(requestDto.getTitle());

        Content content = post.getContent();
        ContentDto contentDto = requestDto.getContent();
        content.setReporterName(contentDto.getReporterName());
        content.setContact(contentDto.getContact());
        content.setLostDate(contentDto.getLostDate());
        content.setLandmark(contentDto.getLandmark());
        content.setBreed(contentDto.getBreed());
        content.setPname(contentDto.getPname());
        content.setColor(contentDto.getColor());
        content.setGender(contentDto.getGender());
        content.setAge(contentDto.getAge());
        content.setFeatures(contentDto.getFeatures());
        content.setHasMicrochip(contentDto.isHasMicrochip());
        content.setLatitude(contentDto.getLatitude());
        content.setLongitude(contentDto.getLongitude());

        if (requestDto.isImageChanged() && imageFiles != null && !imageFiles.isEmpty()) {
            // 기존 이미지 삭제
            List<Image> existingImages = post.getImages();
            if (existingImages != null && !existingImages.isEmpty()) {
                for (Image image : existingImages) {
                    Path folderPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                    Path filePath = folderPath.resolve(image.getStoredFileName());
                    try {
                        Files.deleteIfExists(filePath); // 파일이 존재할 경우 삭제
                    } catch (IOException e) {
                        e.printStackTrace(); // 오류 처리
                    }
                }
            }
            imageRepository.deleteAll(existingImages);
            post.getImages().clear();

            // 새 이미지 저장
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

                // 이미지를 790x680으로 리사이즈하고 공백 처리
                BufferedImage originalImage = ImageIO.read(file.getInputStream());
                BufferedImage resizedImage = resizeImage(originalImage, 790, 680);

                ImageIO.write(resizedImage, extension.substring(1), filePath.toFile());

                Image image = new Image();
                image.setOriginalFileName(originalFileName);
                image.setStoredFileName(storedFileName);
                image.setPost(post);
                post.addImage(image);
            }
        }

        return post.getId();
    }

    // 저장된 이미지 리사이징
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 새로운 이미지 생성
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // 컬러 배경으로 초기화
        g2d.setColor(Color.decode("#D9D9D9"));
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        // 비율 계산
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth, newHeight;

        if (originalHeight > targetHeight || originalWidth > targetWidth) {
            newHeight = targetHeight;
            newWidth = (int) (targetHeight * aspectRatio);
            if (newWidth > targetWidth) {
                newWidth = targetWidth;
                newHeight = (int) (targetWidth / aspectRatio);
            }
        } else {
            newWidth = originalWidth;
            newHeight = originalHeight;
        }

        // 가운데 정렬
        int x = (targetWidth - newWidth) / 2;
        int y = (targetHeight - newHeight) / 2;

        g2d.drawImage(originalImage, x, y, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }
    
    // 게시글 삭제
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

    // 게시글 페이징
    public Page<PostsResponseDto> paging(Pageable pageable, String searchTitle, String kind) {
        int page = pageable.getPageNumber() - 1; // page 위치에 있는 값은 0부터 시작한다.
        int pageLimit = 10; // 한페이지에 보여줄 글 개수

        Page<Posts> postsPages;
        if (searchTitle == null || searchTitle.isEmpty()) {
            if (kind == null || kind.isEmpty()) {
                postsPages = postsRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            } else {
                postsPages = postsRepository.findByKind(kind, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            }
        } else {
            if (kind == null || kind.isEmpty()) {
                postsPages = postsRepository.findByTitleContaining(searchTitle, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            } else {
                postsPages = postsRepository.findByTitleContainingAndKind(searchTitle, kind, PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
            }
        }

        Page<PostsResponseDto> postsResponseDtos = postsPages.map(
                postPage -> new PostsResponseDto(postPage));

        return postsResponseDtos;
    }
    // 게시글 디테일
    public PostsResponseDto findById(Long id) {
        Posts entity = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        return new PostsResponseDto(entity);
    }

    // 게시글 리스트
    @Transactional(readOnly = true)
    public List<PostsListResponseDto> findAllDesc() {
        return postsRepository.findAllDesc().stream()
                .map(PostsListResponseDto::new)
                .collect(Collectors.toList());
    }

    // 게시글 전체 지도
    @Transactional(readOnly = true)
    public List<PostsResponseDto> findAllMap() {
        return postsRepository.findAllDesc().stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    // IndexPage slide 10개만
    public List<PostsResponseDto> findTop10() {
        return postsRepository.findTop10ByOrderByIdDesc().stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }
}