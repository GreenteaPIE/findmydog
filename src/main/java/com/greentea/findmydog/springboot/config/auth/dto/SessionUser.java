package com.greentea.findmydog.springboot.config.auth.dto;

import com.greentea.findmydog.springboot.domain.user.Users;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private long id;
    private String name;
    private String email;
    private String picture;

    public SessionUser(Users user) {
        this.id=user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
