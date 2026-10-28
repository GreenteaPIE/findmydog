package com.greentea.findmydog.springboot.domain.user;

import com.greentea.findmydog.springboot.domain.BaseTimeEntity;
import com.greentea.findmydog.springboot.domain.posts.Posts;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Users extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private String picture;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Posts> posts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Users(String name, String email, String picture, Role role) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
    }

    public Users update(String name, String picture){
        this.name = name;
        this.picture = picture;

        return this;
    }

    public String getRoleKey(){
        return this.role.getKey();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    /** 관리자도 일반 사용자 기능을 쓸 수 있도록 ROLE_USER 를 함께 준다 */
    public List<SimpleGrantedAuthority> authorities() {
        return isAdmin()
                ? List.of(new SimpleGrantedAuthority(Role.USER.getKey()), new SimpleGrantedAuthority(Role.ADMIN.getKey()))
                : List.of(new SimpleGrantedAuthority(getRoleKey()));
    }
}
