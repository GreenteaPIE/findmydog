package com.greentea.findmydog.springboot.exception;

/** 업로드한 파일이 사진이 아니거나 개수·형식이 맞지 않을 때 */
public class InvalidImageException extends RuntimeException {
    public InvalidImageException(String message) {
        super(message);
    }
}
