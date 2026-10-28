package com.greentea.findmydog.springboot.web;

import com.greentea.findmydog.springboot.storage.ImageNames;
import com.greentea.findmydog.springboot.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** 게시글 이미지를 저장소에서 읽어 /images/{name} 으로 내준다. 이름이 바뀌지 않으므로 오래 캐시해도 된다. */
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorage imageStorage;

    @GetMapping("/images/{name}")
    public ResponseEntity<byte[]> image(@PathVariable String name) throws IOException {
        if (!ImageNames.isSafe(name)) return ResponseEntity.notFound().build();
        return imageStorage.load(name)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(ImageNames.contentType(name)))
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
