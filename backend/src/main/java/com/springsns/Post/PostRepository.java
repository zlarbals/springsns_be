package com.springsns.Post;

import com.springsns.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    //등록된 시간의 역순으로 정렬
//    @Query("SELECT p FROM Post p ORDER BY p.postedAt DESC ")
//    List<Post> findAllPosts();

    @Query(value = "select p from Post p ORDER BY p.createdDate DESC")
    Slice<Post> findPostByPaging(Pageable pageable);

    //Like 사용.
    List<Post> findPostsByContentContaining(String keyword);

}
