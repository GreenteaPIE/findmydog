package com.greentea.findmydog.springboot.sevice.posts;

import com.greentea.findmydog.springboot.domain.posts.*;
import com.greentea.findmydog.springboot.domain.user.UserRepository;
import com.greentea.findmydog.springboot.domain.user.Users;
import com.greentea.findmydog.springboot.exception.InvalidImageException;
import com.greentea.findmydog.springboot.exception.PostNotFoundException;
import com.greentea.findmydog.springboot.storage.ImageNames;
import com.greentea.findmydog.springboot.storage.ImageProcessor;
import com.greentea.findmydog.springboot.storage.ImageStorage;
import com.greentea.findmydog.springboot.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostsService {

    public static final int PAGE_SIZE = 12;
    public static final int MAX_IMAGES = 5;

    private final PostsRepository postsRepository;
    private final UserRepository userRepository;
    private final ImageStorage imageStorage;
    private final ImageProcessor imageProcessor;

    // 게시글 저장. 작성자는 로그인한 사용자로 정한다
    @Transactional
    public Long save(PostsSaveRequestDto requestDto, List<MultipartFile> imageFiles, Long userId) {
        List<MultipartFile> files = nonEmpty(imageFiles);
        if (files.isEmpty()) {
            throw new InvalidImageException("사진을 한 장 이상 올려 주세요");
        }
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("로그인 정보를 찾을 수 없어요"));

        Posts post = postsRepository.save(requestDto.toEntity(user));
        storeImages(post, files);
        return post.getId();
    }

    // 게시글 수정. 작성자만 고칠 수 있다
    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto, List<MultipartFile> imageFiles, Long userId) {
        Posts post = findOwnedPost(id, userId);

        post.update(requestDto.getKind(), requestDto.getTitle());
        requestDto.getContent().applyTo(post.getContent());

        List<MultipartFile> files = nonEmpty(imageFiles);
        if (requestDto.isImageChanged() && !files.isEmpty()) {
            List<String> oldNames = post.getImages().stream().map(Image::getStoredFileName).toList();
            post.getImages().clear(); // orphanRemoval 로 DB 에서도 지워진다
            storeImages(post, files);
            deleteAfterCommit(oldNames);
        }
        return post.getId();
    }

    // 찾았어요 / 다시 찾는 중으로 바꾸기
    @Transactional
    public void changeStatus(Long id, String status, Long userId) {
        findOwnedPost(id, userId).changeStatus(status);
    }

    // 게시글 삭제. 작성자만 지울 수 있다
    @Transactional
    public void delete(Long id, Long userId) {
        Posts post = findOwnedPost(id, userId);
        List<String> names = post.getImages().stream().map(Image::getStoredFileName).toList();
        postsRepository.delete(post);
        deleteAfterCommit(names);
    }

    // 회원 탈퇴 때 그 사람의 글을 모두 지운다
    @Transactional
    public void deleteAllByUser(Users user) {
        for (Posts post : postsRepository.findByUser(user)) {
            List<String> names = post.getImages().stream().map(Image::getStoredFileName).toList();
            postsRepository.delete(post);
            deleteAfterCommit(names);
        }
    }

    // 게시판 검색 (page 는 1부터)
    @Transactional(readOnly = true)
    public Page<PostsResponseDto> search(String kind, String status, String keyword, int page) {
        Specification<Posts> spec = PostsSpecs.kind(kind)
                .and(PostsSpecs.status(status))
                .and(PostsSpecs.keyword(keyword));
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return postsRepository.findAll(spec, pageable).map(PostsResponseDto::new);
    }

    @Transactional(readOnly = true)
    public PostsResponseDto findById(Long id) {
        return new PostsResponseDto(postsRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id)));
    }

    // 수정 화면용. 작성자가 아니면 403
    @Transactional(readOnly = true)
    public PostsResponseDto findForEdit(Long id, Long userId) {
        return new PostsResponseDto(findOwnedPost(id, userId));
    }

    @Transactional(readOnly = true)
    public List<PostsResponseDto> findByUser(Long userId) {
        return userRepository.findById(userId)
                .map(user -> postsRepository.findByUserOrderByIdDesc(user).stream().map(PostsResponseDto::new).toList())
                .orElse(List.of());
    }

    // 제보 지도
    @Transactional(readOnly = true)
    public List<MapMarkerDto> findMapMarkers() {
        return postsRepository.findAllDesc().stream().map(MapMarkerDto::from).toList();
    }

    // 메인 화면 최근 제보
    @Transactional(readOnly = true)
    public List<PostsResponseDto> findRecent() {
        return postsRepository.findTop8ByOrderByIdDesc().stream().map(PostsResponseDto::new).toList();
    }

    @Transactional(readOnly = true)
    public long countLooking() {
        return postsRepository.countByStatus(Posts.STATUS_LOOKING);
    }

    @Transactional(readOnly = true)
    public long countReunited() {
        return postsRepository.countByStatus(Posts.STATUS_REUNITED);
    }

    private Posts findOwnedPost(Long id, Long userId) {
        Posts post = postsRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        if (!post.isOwnedBy(userId)) {
            throw new AccessDeniedException("작성자만 고칠 수 있어요");
        }
        return post;
    }

    private static List<MultipartFile> nonEmpty(List<MultipartFile> files) {
        if (files == null) return List.of();
        return files.stream().filter(f -> f != null && !f.isEmpty()).toList();
    }

    // 사진을 JPEG 로 다시 저장하고 UUID 이름을 붙인다. 원래 파일 이름은 경로에 쓰지 않는다
    private void storeImages(Posts post, List<MultipartFile> files) {
        if (files.size() > MAX_IMAGES) {
            throw new InvalidImageException("사진은 " + MAX_IMAGES + "장까지 올릴 수 있어요");
        }
        List<String> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] jpeg = imageProcessor.toJpeg(file);
            String name = ImageNames.newName("jpg");
            try {
                imageStorage.save(name, jpeg);
            } catch (IOException e) {
                saved.forEach(imageStorage::delete);
                throw new UncheckedIOException("사진을 저장하지 못했어요", e);
            }
            saved.add(name);
            post.addImage(Image.builder().originalFileName(file.getOriginalFilename()).storedFileName(name).post(post).build());
        }
        // DB 저장이 실패하면 방금 저장한 파일도 지운다
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) saved.forEach(imageStorage::delete);
                }
            });
        }
    }

    // 파일은 DB 변경이 확정된 뒤에 지운다 (롤백되면 사진이 남아 있어야 하므로)
    private void deleteAfterCommit(List<String> names) {
        if (names.isEmpty()) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    names.forEach(imageStorage::delete);
                }
            });
        } else {
            names.forEach(imageStorage::delete);
        }
    }
}
