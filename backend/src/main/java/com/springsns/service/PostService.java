package com.springsns.service;

import com.springsns.controller.dto.PostForm;
import com.springsns.domain.PostImage;
import com.springsns.controller.dto.PostResponseDto;
import com.springsns.repository.AccountRepository;
import com.springsns.repository.CommentRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Like;
import com.springsns.domain.Post;
import com.springsns.exception.EmailNotVerifiedException;
import com.springsns.exception.ImageNotFoundException;
import com.springsns.exception.PostNotFoundException;
import com.springsns.repository.LikeRepository;
import com.springsns.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    private final Path fileStorageLocation = Paths.get("src/main/resources/images");

    @Transactional
    public Post processRegisterPost(PostForm postForm, String email) throws IOException {
        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));
        validateEmailVerified(account);

        PostImage postImage = makePostFileFormatOrNull(postForm.getImage());

        return saveNewPost(account,postForm.getContent(), postImage);

    }

    public Resource getImageAsResource(String imageName) throws MalformedURLException {
        Resource resource = loadFileAsResource(imageName);
        if(!resource.exists()){
            throw new ImageNotFoundException("존재하지 않는 이미지 파일입니다.");
        }

        return resource;
    }

    @Transactional
    public void deletePost(long postId,String email) {

        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        Post post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException("존재하지 않는 게시글 입니다."));

        if(!isValidPostDeleteCondition(post,account)){
            throw new IllegalArgumentException("게시글 삭제 조건에 맞지 않습니다.");
        }

        //게시글에 이미지가 존재할 경우
        if(post.getPostImage()!=null){
            //이미지 삭제
            String fileName = post.getPostImage().getOriginalFileName();
            deleteImage(fileName);
        }

        postRepository.delete(post);
    }

    public List<Post> findLikedPosts(String email) {

        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        List<Like> likes = likeRepository.findAllByAccount(account);

        return likes.stream()
                .map(like -> like.getPost())
                .collect(Collectors.toList());
    }

    public Slice<PostResponseDto> findPostsByPagingAsDto(Pageable pageable, String email){

        Slice<Post> postsByPaging = postRepository.findPostsByPaging(pageable);

        if (email == null) {
            return postsByPaging.map(post -> new PostResponseDto(post,false));
        }

        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));
        return postsByPaging.map(post -> {
            if (likeRepository.existsByAccountAndPost(account, post)) {
                return new PostResponseDto(post, true);
            } else {
                return new PostResponseDto(post, false);
            }
        });
    }

    public List<PostResponseDto> findPostsByNicknameAsDto(String nickname, String email) {
        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        List<Post> postsByNickname = postRepository.findPostsByNickname(nickname);

        return postsByNickname.stream().map(post -> {
            if (likeRepository.existsByAccountAndPost(account, post)) {
                return new PostResponseDto(post, true);
            } else {
                return new PostResponseDto(post, false);
            }
        }).collect(Collectors.toList());

    }

    public List<PostResponseDto> findPostsByKeywordSearchAsDto(String keyword, String email) {
        Account account = accountRepository.findActivateAccountByEmail(email).orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        List<Post> postsByKeywordSearch = postRepository.findPostsByContentContaining(keyword);

        return postsByKeywordSearch.stream().map(post -> {
            if (likeRepository.existsByAccountAndPost(account, post)) {
                return new PostResponseDto(post, true);
            } else {
                return new PostResponseDto(post, false);
            }
        }).collect(Collectors.toList());
    }

    private boolean isValidPostDeleteCondition(Post post, Account account) {
        //본인이 작성한 게시글인지 확인
        if (!isPostRegisteredByAccount(post,account)) {
            return false;
        }

        //좋아요나 댓글 존재하는지 확인
        if (isPostHaveLikeOrComment(post)) {
            return false;
        }

        return true;
    }

    private boolean isPostRegisteredByAccount(Post post, Account account){
        if(post.getAccount().equals(account)){
            return true;
        }

        return false;
    }

    private boolean isPostHaveLikeOrComment(Post post) {
        if(likeRepository.existsByPost(post) || commentRepository.existsByPost(post)){
            return true;
        }

        return false;
    }

    private void validateEmailVerified(Account account){
        if(!account.isEmailVerified()){
            throw new EmailNotVerifiedException("이메일 인증이 완료되지 않았습니다.");
        }
    }

    private PostImage makePostFileFormatOrNull(MultipartFile image) throws IOException {
        //image로 넘어온 파일이 없는 경우.
        if(image==null){
            return null;
        }

        //파일 확장자
        String extension = getFileExtensionOrNull(image);
        if(!isImageFile(extension)){
            throw new IllegalArgumentException("이미지 파일(.jpg/.jpeg/.png/.gif)만 가능합니다.");
        }

        //file name이 중복 가능하므로 uuid로 변경.
        UUID newFileName = UUID.randomUUID();
        String originalFileName = newFileName+"."+extension;

        Path targetLocation = this.fileStorageLocation.resolve(originalFileName);

        //파일 저장.
        Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return new PostImage(originalFileName,newFileName.toString(),extension);
    }

    private boolean isImageFile(String extension){
        if(extension==null){
            return false;
        }

        if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("gif")){
            return true;
        }

        return false;
    }

    private Post saveNewPost(Account account, String content, PostImage postImage) {
        Post post = Post.builder()
                .account(account)
                .content(content)
                .postImage(postImage)
                .build();

        return postRepository.save(post);
    }

    private Resource loadFileAsResource(String fileName) throws MalformedURLException {
        URI fileUri = getFileUri(fileName);
        Resource resource = new UrlResource(fileUri);

        return resource;
    }

    private void deleteImage(String fileName) {
        URI fileUri = getFileUri(fileName);
        File file = new File(fileUri);
        file.delete();
    }

    private URI getFileUri(String fileName){
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

        return filePath.toUri();
    }

    private String getFileExtensionOrNull(MultipartFile file) {

        int index = file.getOriginalFilename().lastIndexOf(".");

        if(index<0){
            return null;
        }

        String extension = file.getOriginalFilename().substring(index+1);

        return extension.toLowerCase();
    }
}
