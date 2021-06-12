package com.springsns.Post;

import com.springsns.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    //등록된 시간의 역순으로 정렬
//    @Query("SELECT p FROM Post p ORDER BY p.postedAt DESC ")
//    List<Post> findAllPosts();

    @Query(value = "select p from Post p")
    Slice<Post> findPostByPaging(Pageable pageable);

}
