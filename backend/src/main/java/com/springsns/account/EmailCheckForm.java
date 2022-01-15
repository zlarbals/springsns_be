package com.springsns.account;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmailCheckForm {

    private String email;

    private String token;

}
