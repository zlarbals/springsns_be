package com.springsns.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of="id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorEmail;

    private String authorNickname;

    @Column(columnDefinition = "TEXT", nullable=false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private LocalDateTime postedAt;

}
