package com.greentea.findmydog.springboot.storage;

import java.io.IOException;
import java.util.Optional;

/**
 * 게시글 이미지 저장소. 기본은 로컬 디스크이고, 운영에서는 S3 호환 저장소로 바꿔 끼울 수 있다.
 * 이미지는 항상 /images/{name} 으로 제공되므로 저장소가 바뀌어도 화면 코드는 그대로다.
 */
public interface ImageStorage {

    void save(String name, byte[] data) throws IOException;

    Optional<byte[]> load(String name) throws IOException;

    void delete(String name);
}
