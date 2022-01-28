package com.springsns.repository;

import com.springsns.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select p from Post p ORDER BY p.createdDate DESC")
    Slice<Post> findPostsByPaging(Pageable pageable);

    //Like 사용.
    List<Post> findPostsByContentContaining(String keyword);

    @Query(value = "select p from Post p where p.account.nickname=:nickname ORDER BY p.createdDate DESC")
    List<Post> findPostsByNickname(String nickname);

}
