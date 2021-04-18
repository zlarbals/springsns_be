package com.springsns.like;

import com.springsns.account.AccountRepository;
import com.springsns.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/like/{postId}")
    public ResponseEntity addLike(Principal principal, @PathVariable Long postId){
        System.out.println("here is post like");

        String email = principal.getName();

        likeService.addLike(email,postId);

        return new ResponseEntity(HttpStatus.OK);
    }

}
