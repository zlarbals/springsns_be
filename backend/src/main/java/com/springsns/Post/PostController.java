package com.springsns.Post;

import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Like;
import com.springsns.domain.Post;
import com.springsns.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final LikeRepository likeRepository;

    //@Controller로 선언된 bean 객체에서는 메서드 인자로 Principal 객체에 직접 접근할 수 있는 추가적인 옵션이 있다.
    //현재 인증된 사용자의 정보를 Principal로 직접 접근할 수 있다.
    @PostMapping("/post")
    public ResponseEntity registerPost(@RequestPart(required = false) MultipartFile file,@RequestParam String content, Principal principal) throws IOException, NoSuchAlgorithmException {
        System.out.println("here is post /post");
        String email = principal.getName();

        Account account = accountRepository.findByEmail(email);

        //이메일 인증이 안된 경우
        if (account == null || !account.isEmailVerified()) {
            return ResponseEntity.badRequest().build();
        }

        PostFile postFile = null;

        if(file!=null){
            postFile=postService.processPostFile(file);
        }

        //new post 생성.
        Post post = Post.builder()
                .account(account)
                .content(content)
                .postFile(postFile)
                .build();

        //저장.
        Post newPost = postRepository.save(post);

        PostResponseDto postResponseDto = new PostResponseDto(newPost);

        return ResponseEntity.ok().body(postResponseDto);
    }

    @GetMapping("/post")
    public ResponseEntity getAllPosts(Principal principal){
        System.out.println("here is get /post");

        String email = null;

        if (principal != null) {
            email = principal.getName();
        }

        //모든 post 가져오기.
        List<PostResponseDto> postList = postService.getAllPosts(email);


        return ResponseEntity.ok(postList);
    }

    //slice example
    //getAllPosts 메서드 대체 후 해당 메서드 삭제.
    @GetMapping("/post/page")
    public Slice<PostResponseDto> getPostByPage(@PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable){
        Slice<Post> slice = postRepository.findPostByPaging(pageable);
        Slice<PostResponseDto> dtoPage = slice.map(post->new PostResponseDto(post,false));
        return dtoPage;
    }

    @GetMapping("/post/my")
    public ResponseEntity getMyPosts(Principal principal){
        System.out.println("here is get /post/my");

        String email = principal.getName();

        Account account = accountRepository.findByEmail(email);

        List<Post> posts = account.getPosts();

        List<PostResponseDto> postList = new ArrayList<>();

        for(Post post:posts){
            if(likeRepository.existsByAccountAndPost(account,post)){
                postList.add(new PostResponseDto(post,true));
            }else{
                postList.add(new PostResponseDto(post,false));
            }
        }

        return ResponseEntity.ok(postList);


    }

    @GetMapping("/post/my/like")
    public ResponseEntity getMyLikePosts(Principal principal){
        System.out.println("here is get /post/my/like");

        String email = principal.getName();

        Account account = accountRepository.findByEmail(email);

        List<Like> likes = account.getLikes();

        List<PostResponseDto> postList = new ArrayList<>();

        for(Like like: likes){

            postList.add(new PostResponseDto(like.getPost(),true));
        }

        return ResponseEntity.ok(postList);
    }

    @GetMapping("/post/image/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws IOException {
        Resource resource = postService.loadFileAsResource(fileName);

        String contentType = null;
        contentType=request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

        if(contentType==null){
            contentType="application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+resource.getFilename()+"\"")
                .body(resource);

    }

}
