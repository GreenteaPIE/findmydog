package com.greentea.findmydog.springboot.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalDetailData {
    private String detailUrl;
    private String imageUrl;
    private String noticeNo;
    private String breed;
    private String color;
    private String sex;
    private String neuterStatus;
    private String foundLocation;
    private String receivedDate;
    private String jurisdiction;
    private String shelterName;
    private String shelterAddress;
    private String page;
}