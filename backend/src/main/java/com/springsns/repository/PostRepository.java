package com.springsns.repository;

import com.springsns.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    //처음 요청하면 lastIndex가 0이므로 아무것도 안넘어온다.
    @Query(value = "select p from Post p WHERE p.id < :lastIndex ORDER BY p.createdDate DESC")
    List<Post> findPostsByNoOffsetPaging(Pageable pageable, long lastIndex);

    //첫 요청일 경우 사용해야 한다.
    @Query(value = "select p from Post p ORDER BY p.createdDate DESC")
    List<Post> findPostsByNoOffsetPagingFirst(Pageable pageable);

    //TODO 쿼리를 동적으로 할 수 있도록 해야 한다. 예를들어 첫 요청일 경우 p.id< :lastIndex가 작동하지 않도록...
    //TODO jpql에서는 limit가 작동하지 않는다. 어쩔 수 없이 Pageable을 파라미터로 사용해야 한다...
    //TODO query dsl 알아보고 적용해보자. 동적으로. limit 때문에 쓰는 거라면 없애는 방향으로...

    //Like 사용.
    List<Post> findPostsByContentContaining(String keyword);

    @Query(value = "select p from Post p where p.account.nickname=:nickname ORDER BY p.createdDate DESC")
    List<Post> findPostsByNickname(String nickname);

}
