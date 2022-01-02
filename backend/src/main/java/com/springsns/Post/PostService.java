package com.springsns.Post;

import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import com.springsns.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final LikeRepository likeRepository;

    private final Path fileStorageLocation = Paths.get("src/main/resources/images");

    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPosts(String email){

        List<PostResponseDto> result = new ArrayList<>();
        //List<Post> postList = postRepository.findAllPosts();
        List<Post> postList = postRepository.findAll();

        Account account = null;
        if (email != null)
            account = accountRepository.findByEmail(email);


        if (account == null) {
            // 그냥 PostResponseDto에 담아서 넘겨줌
            for (Post post : postList) {
                result.add(new PostResponseDto(post, false));
            }
        } else {
            // PostResponseDto에 isLike 부분 true로 처리
            //이부분 db 쿼리로 수정할 것.

            for (Post post : postList) {

                if (likeRepository.existsByAccountAndPost(account, post)) {
                    result.add(new PostResponseDto(post, true));
                } else {
                    result.add(new PostResponseDto(post, false));
                }
            }
        }

        return result;
    }

    public PostFile processPostFile(MultipartFile file) throws IOException {

        String extension = getFileExtension(file);

        //file name이 중복 가능하므로 uuid로 변경.
        UUID newFileName = UUID.randomUUID();

        String originalFileName = newFileName+"."+extension;

        Path targetLocation = this.fileStorageLocation.resolve(originalFileName);

        //파일 저장.
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        PostFile postFile = new PostFile(originalFileName,newFileName.toString(),extension);

        return postFile;
    }

    public Resource loadFileAsResource(String fileName) throws MalformedURLException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        return resource;
    }

    public List<PostResponseDto> searchPosts(String keyword,String email) {

        Account account = accountRepository.findByEmail(email);

        List<Post> postsByContentContaining = postRepository.findPostsByContentContaining(keyword);

        List<PostResponseDto> result = new ArrayList<>();
        for(Post post: postsByContentContaining){
            if (likeRepository.existsByAccountAndPost(account, post)) {
                result.add(new PostResponseDto(post, true));
            } else {
                result.add(new PostResponseDto(post, false));
            }
        }

        return result;
    }

    private String getFileExtension(MultipartFile file) {

        int index = file.getOriginalFilename().lastIndexOf(".");

        String extension = file.getOriginalFilename().substring(index+1);

        return extension;
    }
}
