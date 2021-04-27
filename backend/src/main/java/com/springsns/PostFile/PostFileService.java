package com.springsns.PostFile;

import com.springsns.Util.MD5Generator;
import com.springsns.domain.PostFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@Service
public class PostFileService {

    private final PostFileRepository postFileRepository;

    public PostFile processPostFile(MultipartFile file) throws NoSuchAlgorithmException, IOException {

        String originalFileName = file.getOriginalFilename();

        String fileName = new MD5Generator(originalFileName).toString();

        System.out.println(System.getProperty("user.dir"));
        String savePath = System.getProperty("user.dir") + "\\files";

        File directory = new File(savePath);

        boolean isOk = directory.mkdir();
        if(!isOk){
            System.out.println("디렉토리 생성에 실패했습니다.");
        }

        String filePath = savePath + "\\" + fileName;
        file.transferTo(new File(filePath));

        PostFile postFile = PostFile.builder()
                .fileName(fileName)
                .originalFileName(originalFileName)
                .filePath(filePath)
                .build();

        return postFileRepository.save(postFile);
    }

}
