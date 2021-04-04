package com.springsns.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class SignInForm {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min=8,max=50)
    private String password;

}
