package com.springsns.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
//equals는 두 객체의 내용이 같은지, hashcode는 두 객체가 같은 객체인지 - 동시 생성
//callSuper = false가 기본 값, true인 경우 부모 클래스의 필드까지 감안.
//of = "id"는 id로만 비교하도록 설정, rest api에서 복잡하면 무한루프가 발생할 수도 있다고함.
@EqualsAndHashCode(of = "id",callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_ID")
    private Long id;

    @OneToMany(mappedBy = "account")
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "account")
    private List<Post> posts = new ArrayList<>();

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    //email 인증 여부
    private boolean emailVerified;

    //email 검증할 때 사용할 토큰 값.
    private String emailCheckToken;

    private LocalDateTime emailVerifiedDate;

    private boolean isActivate;

    public void generateEmailCheckToken() {
        //uuid를 통해 랜덤한 값 생성해서 저장하기
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public void changeInfoForDelete(){
        //계정 닉네임 변경
        this.nickname="LeftUser";
        //계정 비활성화
        this.isActivate=false;
    }

    public void verifyingEmailAuthentication(){
        this.emailVerified=true;
        this.emailVerifiedDate=LocalDateTime.now();
    }

    public void changePassword(String password){
        this.password=password;
    }

}
