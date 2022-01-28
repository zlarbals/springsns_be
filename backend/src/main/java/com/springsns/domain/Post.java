package com.springsns.domain;


import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
//equals는 두 객체의 내용이 같은지, hashcode는 두 객체가 같은 객체인지 - 동시 생성
//callSuper = false가 기본 값, true인 경우 부모 클래스의 필드까지 감안.
//of = "id"는 id로만 비교하도록 설정, rest api에서 복잡하면 무한루프가 발생할 수도 있다고함.
@EqualsAndHashCode(of = "id",callSuper = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Embedded
    private PostImage postImage;

    @Builder
    public Post(Account account, String content, PostImage postImage){
        this.account=account;
        this.content=content;
        this.postImage = postImage;

        //일반적으로 실행되면 연관관계가 모두 정상적으로 저장된다.
        //테스트 진행할 때 account를 생성하고 해당 account가 post를 작성하게 된다.
        //account의 posts 필드애 입력된 값이 없으므로 posts 필드가 조회되지 않는다.
        //순수 객체 상태를 고려해 양쪽 모두에 값을 세팅해야 한다.
        account.getPosts().add(this);
    }
}
