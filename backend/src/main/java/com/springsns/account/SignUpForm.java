package com.springsns.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {

    @NotBlank(message="닉네임을 작성해주세요.")
    @Length(min = 3, max = 20,message = "닉네임 길이가 3에서 20 사이여아 합니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$",message = "닉네임은 숫자,한글,알파벳소문자,특수문자(_,-)로 구성되야 합니다.")
    private String nickname;

    @Email(message="이메일 형식이여야 합니다.")
    @NotBlank(message = "이메일을 작성해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 작성해주세요.")
    @Length(min=8,max=50,message = "비밀번호 길이가 8에서 50 사이여야 합니다.")
    private String password;

}
