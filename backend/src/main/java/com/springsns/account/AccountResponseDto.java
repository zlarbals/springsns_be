package com.springsns.account;

import com.springsns.domain.Account;
import lombok.Getter;

@Getter
public class AccountResponseDto {

    private Long id;

    private String email;

    private String nickname;

    private boolean emailVerified;

    public AccountResponseDto(Account account){
        this.id=account.getId();
        this.email=account.getEmail();
        this.nickname=account.getNickname();
        this.emailVerified=account.isEmailVerified();
    }

}
