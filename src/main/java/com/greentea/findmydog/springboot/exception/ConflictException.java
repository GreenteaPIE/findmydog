package com.greentea.findmydog.springboot.exception;

/** 이미 처리된 요청 (예: 같은 글을 두 번 신고) */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
