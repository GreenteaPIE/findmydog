package com.greentea.findmydog.springboot.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageDto {

    private Long id;
    private String originalFileName;
    private String storedFileName;

    public ImageDto(Long id, String originalFileName, String storedFileName){
        this.id = id;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
    }
}
