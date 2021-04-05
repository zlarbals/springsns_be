package com.springsns.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //헤더에서 JWT 받아오기.
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) servletRequest);

        //유효한 토큰인지 확인
        if (token != null && jwtTokenProvider.validateJwtToken(token)) {
            //토큰이 유효하면 토큰으로부터 유저 정보 받기
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            //authentication 객체 저장.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
