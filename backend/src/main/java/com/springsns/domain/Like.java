package com.springsns.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="POST_LIKE")
@NoArgsConstructor
public class Like {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public Like(Account account,Post post){
        this.account=account;
        this.post=post;
    }
}
