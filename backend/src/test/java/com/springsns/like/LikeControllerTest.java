package com.springsns.like;

import com.springsns.Post.PostRepository;
import com.springsns.account.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class LikeControllerTest {

    private MockMvc mockMvc;

    private AccountRepository accountRepository;

    private LikeRepository likeRepository;

    private PostRepository postRepository;

    @DisplayName("좋아용 테스트 - 정상")
    @Test
    void addLike_with_correct_input() throws Exception{

    }

}