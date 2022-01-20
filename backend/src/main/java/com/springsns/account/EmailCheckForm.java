package com.springsns.account;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;

@Getter
@Setter
@ToString
public class EmailCheckForm {

    @Email
    private String email;

    private String token;

}
