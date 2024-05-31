package com.greentea.findmydog.springboot.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalData {
    private String imageUrl;
    private String breed;
    private String id;
    private String sex;
    private String location;
    private String characteristics;
}
