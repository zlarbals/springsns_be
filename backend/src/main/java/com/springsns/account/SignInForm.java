package com.springsns.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;

@Getter
@AllArgsConstructor
@ToString
public class SignInForm {

    @Email
    private String email;

    @Length(min=8,max=50)
    private String password;

}
