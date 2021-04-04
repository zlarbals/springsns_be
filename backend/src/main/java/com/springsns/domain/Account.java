package com.springsns.domain;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
//equals는 두 객체의 내용이 같은지, hashcode는 두 객체가 같은 객체인지 - 동시 생성
//callSuper = false가 기본 값, true인 경우 부모 클래스의 필드까지 감안.
//of = "id"는 id로만 비교하도록 설정, rest api에서 복잡하면 무한루프가 발생할 수도 있다고함.
@EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account implements UserDetails {

    @Id @GeneratedValue
    private Long id;

    @Column(unique=true)
    private String email;

    @Column(unique=true)
    private String nickname;

    private String password;

    //email 인증 여부
    private boolean emailVerified;

    //email 검증할 때 사용할 토큰 값.
    private String emailCheckToken;

    private LocalDateTime joinedAt;

    //좋아요 받은 총 갯수
    private int likeCount;

    //싫어요 받은 총 갯수
    private int dislikeCount;


    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    public void generateEmailCheckToken() {
        //uuid를 통해 랜덤한 값 생성해서 저장하기
        this.emailCheckToken= UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        this.emailVerified=true;
        this.joinedAt=LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
