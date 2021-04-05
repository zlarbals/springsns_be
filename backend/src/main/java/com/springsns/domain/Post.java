package com.springsns.domain;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
//equals는 두 객체의 내용이 같은지, hashcode는 두 객체가 같은 객체인지 - 동시 생성
//callSuper = false가 기본 값, true인 경우 부모 클래스의 필드까지 감안.
//of = "id"는 id로만 비교하도록 설정, rest api에서 복잡하면 무한루프가 발생할 수도 있다고함.
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorEmail;

    private String authorNickname;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private LocalDateTime postedAt;

}
