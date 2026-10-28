package com.greentea.findmydog.springboot.storage;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/** 저장 파일 이름 규칙. 사용자가 올린 파일 이름은 쓰지 않고 UUID 로 만든다. */
public final class ImageNames {

    private static final Pattern SAFE = Pattern.compile("^[A-Za-z0-9-]{1,64}\\.(jpg|jpeg|png)$");
    private static final Map<String, String> CONTENT_TYPES = Map.of("jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png");

    private ImageNames() {
    }

    /** 원본 파일 이름에서 확장자만 가져온다. 허용하지 않는 형식이면 빈 값. */
    public static Optional<String> extensionOf(String originalFileName) {
        if (originalFileName == null || !originalFileName.contains(".")) return Optional.empty();
        String ext = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return CONTENT_TYPES.containsKey(ext) ? Optional.of(ext) : Optional.empty();
    }

    public static String newName(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    public static boolean isSafe(String name) {
        return name != null && SAFE.matcher(name).matches();
    }

    public static String contentType(String name) {
        return CONTENT_TYPES.getOrDefault(name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT), "application/octet-stream");
    }
}
