package com.greentea.findmydog.springboot.domain.posts;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** 게시판 검색 조건. 비어 있는 조건은 건너뛴다. */
public final class PostsSpecs {

    private PostsSpecs() {
    }

    public static Specification<Posts> kind(String kind) {
        return (root, query, cb) -> StringUtils.hasText(kind) ? cb.equal(root.get("kind"), kind) : null;
    }

    public static Specification<Posts> status(String status) {
        return (root, query, cb) -> StringUtils.hasText(status) ? cb.equal(root.get("status"), status) : null;
    }

    /** 제목·품종·색상·주소·주위 건물 중 하나라도 키워드를 포함하면 찾는다 */
    public static Specification<Posts> keyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String like = "%" + keyword.trim().toLowerCase() + "%";
            Join<Posts, Content> content = root.join("content");
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(content.get("breed")), like),
                    cb.like(cb.lower(content.get("color")), like),
                    cb.like(cb.lower(content.get("address")), like),
                    cb.like(cb.lower(content.get("landmark")), like));
        };
    }
}
