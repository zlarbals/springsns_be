package com.springsns.Post;

import com.springsns.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    //등록된 시간의 역순으로 정렬
//    @Query("SELECT p FROM Post p ORDER BY p.postedAt DESC ")
//    List<Post> findAllPosts();

}
