package com.springsns.post;

import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import com.springsns.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    private final Path fileStorageLocation = Paths.get("src/main/resources/images");

    public Post processRegisterPost(PostForm postForm, String email) throws IOException {
        Account account = accountRepository.findByEmail(email);

        PostImage postImage = makePostFileFormat(postForm.getImage());

        return saveNewPost(account,postForm.getContent(), postImage);

    }

    public Resource getImageAsResource(String imageName) throws MalformedURLException {
        Resource resource = loadFileAsResource(imageName);

        return resource;
    }

    public void deletePost(long postId) {
        //앞에 valid check에서 확인했으므로 반드시 존재.
        Post post = postRepository.findById(postId).get();

        //게시글에 이미지가 존재할 경우
        if(post.getPostImage()!=null){
            //이미지 삭제
            String fileName = post.getPostImage().getOriginalFileName();
            deleteImage(fileName);
        }

        postRepository.delete(post);
    }

    private PostImage makePostFileFormat(MultipartFile image) throws IOException {
        //image로 넘어온 파일이 없는 경우.
        if(image==null){
            return null;
        }

        //파일 확장자
        String extension = getFileExtension(image);

        //file name이 중복 가능하므로 uuid로 변경.
        UUID newFileName = UUID.randomUUID();
        String originalFileName = newFileName+"."+extension;

        Path targetLocation = this.fileStorageLocation.resolve(originalFileName);

        //파일 저장.
        Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return new PostImage(originalFileName,newFileName.toString(),extension);
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

    private String getFileExtension(MultipartFile file) {

        int index = file.getOriginalFilename().lastIndexOf(".");
        String extension = file.getOriginalFilename().substring(index+1);

        return extension;
    }

}
