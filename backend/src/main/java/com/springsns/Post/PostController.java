package com.springsns.Post;

import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    //@Controller로 선언된 bean 객체에서는 메서드 인자로 Principal 객체에 직접 접근할 수 있는 추가적인 옵션이 있다.
    //현재 인증된 사용자의 정보를 Principal로 직접 접근할 수 있다.
    @PostMapping("/post")
    public ResponseEntity registerPost(@RequestBody PostForm postForm, Principal principal) {
        System.out.println("here is post /post");
        String email = principal.getName();

        Account account = accountRepository.findByEmail(email);

        //이메일 인증이 안된 경우
        if (account==null || !account.isEmailVerified()) {
            return ResponseEntity.badRequest().build();
        }

        //new post 생성.
        Post post = Post.builder()
                .authorEmail(account.getEmail())
                .content(postForm.getContent())
                .postedAt(LocalDateTime.now())
                .authorNickname(account.getNickname())
                .build();

        //저장.
        Post newPost = postRepository.save(post);

        return ResponseEntity.ok().body(newPost);
    }

    @GetMapping("/post")
    public ResponseEntity findAllPosts() {

        System.out.println("here is get /post");

        //모든 post 가져오기.
        List<Post> postList = postService.findAllPosts();

        return ResponseEntity.ok(postList);
    }

}
