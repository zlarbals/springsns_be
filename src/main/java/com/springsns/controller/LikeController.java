package com.springsns.controller;

import com.springsns.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/like/{postId}")
    public ResponseEntity addAndDeleteLike(@PathVariable Long postId, HttpServletRequest request){
        log.info("LikeController.Post./like/{postId}");

        String email = (String) request.getAttribute("SignInAccountEmail");
        likeService.processAddAndDeleteLike(email,postId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
