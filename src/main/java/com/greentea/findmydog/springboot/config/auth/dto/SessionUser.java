package com.greentea.findmydog.springboot.config.auth.dto;

import com.greentea.findmydog.springboot.domain.user.User;

import java.io.Serializable;

public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String picture;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
