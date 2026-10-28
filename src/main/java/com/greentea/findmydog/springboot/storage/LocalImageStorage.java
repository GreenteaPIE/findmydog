package com.greentea.findmydog.springboot.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Component
public class LocalImageStorage implements ImageStorage {

    private final Path folder;

    public LocalImageStorage(@Value("${file.upload-dir}") String uploadDir) {
        this.folder = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    // 이름은 ImageNames 로 검증된 것만 오지만, 폴더 밖으로 나가는 경로는 한 번 더 막는다
    private Path resolve(String name) {
        Path path = folder.resolve(name).normalize();
        if (!path.getParent().equals(folder)) {
            throw new IllegalArgumentException("잘못된 파일 이름입니다: " + name);
        }
        return path;
    }

    @Override
    public void save(String name, byte[] data) throws IOException {
        Files.createDirectories(folder);
        Files.write(resolve(name), data);
    }

    @Override
    public Optional<byte[]> load(String name) throws IOException {
        Path path = resolve(name);
        return Files.isRegularFile(path) ? Optional.of(Files.readAllBytes(path)) : Optional.empty();
    }

    @Override
    public void delete(String name) {
        try {
            Files.deleteIfExists(resolve(name));
        } catch (IOException e) {
            log.warn("이미지 삭제 실패: {}", name, e);
        }
    }
}
