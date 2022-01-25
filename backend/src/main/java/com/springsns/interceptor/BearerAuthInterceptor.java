package com.springsns.interceptor;

import com.springsns.account.JwtTokenProvider;
import com.springsns.exception.AccountUnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Slf4j
@Component
public class BearerAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.info("BearerAuthInterceptor preHandle");

        String token = jwtTokenProvider.extractToken(request);

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        if(token == null && isPassedRequest(requestURI,requestMethod)){
            return true;
        }

        if(!jwtTokenProvider.validateJwtToken(token)){
            throw new AccountUnauthorizedException("유효하지 않은 토큰입니다.");
        }

        String email = jwtTokenProvider.getAccountEmail(token);
        request.setAttribute("SignInAccountEmail",email);
        return true;
    }

    private boolean isPassedRequest(String requestURI,String requestMethod){
        if(isPostAccount(requestURI,requestMethod) || isGetPost(requestURI,requestMethod)){
            return true;
        }

        return false;
    }

    //게시글 가져오기의 경우 인증 여부에 상관없이 통과시켜야 한다.
    private boolean isGetPost(String requestURI, String requestMethod) {
        if(requestURI.equals("/post") && requestMethod.equals("GET")){
            return true;
        }

        return false;
    }

    //회원가입의 경우 인증이 필요없다. 즉 POST /account 의 경우 인증이 필요없다.
    //rest 형식이고 설정에서 요청 메소드로 필터하지 못하기 때문에 해당 내용이 필요하다.
    private boolean isPostAccount(String requestURI, String requestMethod) {
        if(requestURI.equals("/account") && requestMethod.equals("POST")){
            return true;
        }

        return false;
    }
}
