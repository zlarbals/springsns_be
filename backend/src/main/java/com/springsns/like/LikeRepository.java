package com.springsns.like;


import com.springsns.domain.Account;
import com.springsns.domain.Like;
import com.springsns.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface LikeRepository extends JpaRepository<Like,Long> {

    Like findByAccountAndPost(Account account, Post post);

    boolean existsByAccountAndPost(Account account,Post post);

    boolean existsByPost(Post post);

}
