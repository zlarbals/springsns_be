package com.springsns.Post;

import com.springsns.account.AccountRepository;
import com.springsns.comment.CommentRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import com.springsns.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    //@Controller로 선언된 bean 객체에서는 메서드 인자로 Principal 객체에 직접 접근할 수 있는 추가적인 옵션이 있다.
    //현재 인증된 사용자의 정보를 Principal로 직접 접근할 수 있다.
    @PostMapping("/post")
    public ResponseEntity registerPost(@RequestPart(required = false) MultipartFile file, @RequestParam String content, Principal principal) throws IOException, NoSuchAlgorithmException {
        System.out.println("post /post");
        String email = principal.getName();

        Account account = accountRepository.findByEmail(email);

        //이메일 인증이 안된 경우
        if (account == null || !account.isEmailVerified()) {
            return ResponseEntity.badRequest().build();
        }

        PostFile postFile = null;

        if (file != null) {
            postFile = postService.processPostFile(file);
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

//    @GetMapping("/post")
//    public ResponseEntity getAllPosts(Principal principal) {
//        System.out.println("here is get /post");
//
//        String email = null;
//
//        if (principal != null) {
//            email = principal.getName();
//        }
//
//        //모든 post 가져오기.
//        List<PostResponseDto> postList = postService.getAllPosts(email);
//
//
//        return ResponseEntity.ok(postList);
//    }

    //slice example
    //getAllPosts 메서드 대체 후 해당 메서드 삭제.
    @GetMapping("/post")
    public Slice<PostResponseDto> getPostByPage(@PageableDefault(size=5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable, Principal principal) {
        System.out.println("get /post");
        Slice<Post> slice = postRepository.findPostByPaging(pageable);
        Slice<PostResponseDto> dtoPage;

        if(principal==null){
            dtoPage = slice.map(post->new PostResponseDto(post,false));
        }else{
            String email = principal.getName();
            Account account = accountRepository.findByEmail(email);
            dtoPage=slice.map(post->{
                if(likeRepository.existsByAccountAndPost(account,post)){
                    return new PostResponseDto(post,true);
                }else{
                    return new PostResponseDto(post,false);
                }
            });
        }

        return dtoPage;
    }

    //특정 유저가 작성한 게시글 가져오기
    @GetMapping("/post/account/{nickname}")
    public ResponseEntity getAccountPosts(@PathVariable String nickname) {
        System.out.println("get /post/account/{nickname}");

        Account account = accountRepository.findByNickname(nickname);

        List<Post> posts = account.getPosts();

        List<PostResponseDto> postList = new ArrayList<>();

        for(int i=posts.size()-1;i>=0;i--){
            Post tempPost = posts.get(i);

            if (likeRepository.existsByAccountAndPost(account, tempPost)) {
                postList.add(new PostResponseDto(tempPost, true));
            } else {
                postList.add(new PostResponseDto(tempPost, false));
            }
        }

//        for (Post post : posts) {
//            if (likeRepository.existsByAccountAndPost(account, post)) {
//                postList.add(new PostResponseDto(post, true));
//            } else {
//                postList.add(new PostResponseDto(post, false));
//            }
//        }



        return ResponseEntity.ok(postList);
    }

    @GetMapping("/post/image/{imageName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String imageName, HttpServletRequest request) throws IOException {
        Resource resource = postService.loadFileAsResource(imageName);

        String contentType = null;
        contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

    }

    @GetMapping("/post/search/{keyword}")
    public ResponseEntity<Resource> searchPostByKeyword(@PathVariable String keyword,Principal principal){
        System.out.println("GET /post/search/{keyword}");

        String email = principal.getName();

        Account account = accountRepository.findByEmail(email);

        if(account == null || !account.isEmailVerified()){
            return ResponseEntity.badRequest().build();
        }

        List<PostResponseDto> postResponseDtoList = postService.searchPosts(keyword,email);

        return new ResponseEntity(postResponseDtoList, HttpStatus.OK);
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Resource> deletePost(@PathVariable long postId,Principal principal){
        System.out.println("DELETE /post/{postId}");

        String email = principal.getName();

        //이메일 인증 확인
        Account account = accountRepository.findByEmail(email);
        if(account==null || !account.isEmailVerified()){
            return ResponseEntity.badRequest().build();
        }

        //게시글이 있는지 확인
        Optional<Post> post = postRepository.findById(postId);
        if(!post.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        //본인이 작성한 게시글인지 확인
        if(!post.get().getAccount().equals(account)){
            return ResponseEntity.badRequest().build();
        }

        //좋아요나 댓글 존재하는지 확인
        if(likeRepository.existsByPost(post.get()) || commentRepository.existsByPost(post.get())){
            return ResponseEntity.badRequest().build();
        }

        //삭제
        postRepository.delete(post.get());

        return new ResponseEntity(HttpStatus.OK);
    }

}
