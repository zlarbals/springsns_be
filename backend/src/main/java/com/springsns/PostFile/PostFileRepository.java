package com.springsns.PostFile;


import com.springsns.domain.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostFileRepository extends JpaRepository<PostFile,Long> {
}
