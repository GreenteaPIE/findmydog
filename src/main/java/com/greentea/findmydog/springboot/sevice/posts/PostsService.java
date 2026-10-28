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
import com.greentea.findmydog.springboot.domain.Geo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostsService {

    public static final int PAGE_SIZE = 12;
    public static final int MAX_IMAGES = 5;
    public static final int MAX_POSTS_PER_DAY = 10;
    public static final double NEAR_KM = 3.0;
    static final double SIMILAR_KM = 5.0;

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
        if (postsRepository.countByUserAndCreatedDateAfter(user, LocalDateTime.now().minusDays(1)) >= MAX_POSTS_PER_DAY) {
            throw new IllegalArgumentException("제보는 하루에 " + MAX_POSTS_PER_DAY + "개까지 올릴 수 있어요");
        }

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
            List<String> oldNames = fileNames(post);
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
        remove(findOwnedPost(id, userId));
    }

    // 회원 탈퇴 때 그 사람의 글을 모두 지운다
    @Transactional
    public void deleteAllByUser(Users user) {
        postsRepository.findByUser(user).forEach(this::remove);
    }

    // ---------- 관리자 ----------

    @Transactional
    public void setHidden(Long id, boolean hidden) {
        Posts post = postsRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        if (hidden) post.hide();
        else post.restore();
    }

    @Transactional
    public void deleteByAdmin(Long id) {
        remove(postsRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id)));
    }

    private void remove(Posts post) {
        List<String> names = fileNames(post);
        postsRepository.delete(post);
        deleteAfterCommit(names);
    }

    private static List<String> fileNames(Posts post) {
        return post.getImages().stream()
                .flatMap(image -> Stream.of(image.getStoredFileName(), ImageNames.thumbnailOf(image.getStoredFileName())))
                .toList();
    }

    // 게시판 검색 (page 는 1부터). lat·lng 가 있으면 그 주변 NEAR_KM 안쪽만
    @Transactional(readOnly = true)
    public Page<PostsResponseDto> search(String kind, String status, String keyword, Double lat, Double lng, int page) {
        Specification<Posts> spec = PostsSpecs.visible()
                .and(PostsSpecs.kind(kind))
                .and(PostsSpecs.status(status))
                .and(PostsSpecs.keyword(keyword))
                .and(PostsSpecs.near(lat, lng, NEAR_KM));
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return postsRepository.findAll(spec, pageable).map(post -> {
            PostsResponseDto dto = new PostsResponseDto(post);
            if (lat != null && lng != null) dto.setDistanceKm(distance(post, lat, lng));
            return dto;
        });
    }

    // API·관리용: 숨긴 글도 그대로 찾는다
    @Transactional(readOnly = true)
    public PostsResponseDto findById(Long id) {
        return new PostsResponseDto(postsRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id)));
    }

    // 화면용: 숨긴 글은 글쓴이와 관리자만 볼 수 있다
    @Transactional(readOnly = true)
    public PostsResponseDto findVisible(Long id, Long viewerId, boolean admin) {
        Posts post = postsRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        if (post.isHidden() && !admin && !post.isOwnedBy(viewerId)) {
            throw new PostNotFoundException(id);
        }
        return new PostsResponseDto(post);
    }

    /**
     * 비슷한 제보: 실종 글이면 근처의 발견 글을, 발견 글이면 근처의 실종 글을 찾는다.
     * 반경 5km 안, 실종·발견 일시가 3일 전부터 30일 뒤 사이인 글을 품종이 같으면 앞으로, 그다음 가까운 순으로.
     */
    @Transactional(readOnly = true)
    public List<PostsResponseDto> findSimilar(Long id, int limit) {
        Posts post = postsRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        Content c = post.getContent();
        String opposite = Posts.KIND_LOST.equals(post.getKind()) ? Posts.KIND_FOUND : Posts.KIND_LOST;
        Specification<Posts> spec = PostsSpecs.visible()
                .and(PostsSpecs.kind(opposite))
                .and(PostsSpecs.status(Posts.STATUS_LOOKING))
                .and(PostsSpecs.near(c.getLatitude(), c.getLongitude(), SIMILAR_KM));
        String breed = normalize(c.getBreed());
        return postsRepository.findAll(spec).stream()
                .filter(other -> withinDays(c.getLostDate(), other.getContent().getLostDate()))
                .map(other -> {
                    PostsResponseDto dto = new PostsResponseDto(other);
                    dto.setDistanceKm(distance(other, c.getLatitude(), c.getLongitude()));
                    return dto;
                })
                .filter(dto -> dto.getDistanceKm() <= SIMILAR_KM)
                .sorted(Comparator.comparing((PostsResponseDto dto) -> !normalize(dto.getContent().getBreed()).equals(breed))
                        .thenComparing(PostsResponseDto::getDistanceKm))
                .limit(limit)
                .toList();
    }

    private static boolean withinDays(LocalDateTime base, LocalDateTime other) {
        if (base == null || other == null) return true;
        return !other.isBefore(base.minusDays(3)) && !other.isAfter(base.plusDays(30));
    }

    private static String normalize(String breed) {
        return breed == null ? "" : breed.replace(" ", "").toLowerCase();
    }

    private static double distance(Posts post, double lat, double lng) {
        return Geo.distanceKm(lat, lng, post.getContent().getLatitude(), post.getContent().getLongitude());
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
        return postsRepository.findAllVisibleDesc().stream().map(MapMarkerDto::from).toList();
    }

    // 메인 화면 최근 제보
    @Transactional(readOnly = true)
    public List<PostsResponseDto> findRecent() {
        return postsRepository.findTop8ByHiddenFalseOrderByIdDesc().stream().map(PostsResponseDto::new).toList();
    }

    @Transactional(readOnly = true)
    public long countLooking() {
        return postsRepository.countByStatusAndHiddenFalse(Posts.STATUS_LOOKING);
    }

    @Transactional(readOnly = true)
    public long countReunited() {
        return postsRepository.countByStatusAndHiddenFalse(Posts.STATUS_REUNITED);
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
            ImageProcessor.Processed processed = imageProcessor.process(file);
            String name = ImageNames.newName("jpg");
            try {
                imageStorage.save(name, processed.full());
                saved.add(name);
                imageStorage.save(ImageNames.thumbnailOf(name), processed.thumbnail());
                saved.add(ImageNames.thumbnailOf(name));
            } catch (IOException e) {
                saved.forEach(imageStorage::delete);
                throw new UncheckedIOException("사진을 저장하지 못했어요", e);
            }
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
