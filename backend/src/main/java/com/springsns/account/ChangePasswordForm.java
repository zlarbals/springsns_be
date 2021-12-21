package com.springsns.account;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class ChangePasswordForm {

    @NotBlank(message = "비밀번호를 작성해주세요.")
    @Length(min=8,max=50,message = "비밀번호 길이가 8에서 50 사이여야 합니다.")
    private String password;
}
