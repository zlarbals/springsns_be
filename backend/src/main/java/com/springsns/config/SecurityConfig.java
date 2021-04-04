package com.springsns.config;

import com.springsns.account.JwtAuthenticationFilter;
import com.springsns.account.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //특정한 요청들을 인증 check 하지 않도록 해줌.
        http.httpBasic().disable() // security에서 기본으로 생성하는 login페이지 사용 안 함.
                .csrf().disable() // csrf 사용 안 함.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  //JWT 인증 이므로 세션 사용
        .and()
        .authorizeRequests()
                .mvcMatchers("/","/users/signin","/sign-up","/check-email","/check-email-token",
                        "/email-login","/check-email-login","login-link").permitAll()//모두 허용.
                .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll()//profile 같은 경우 GET만 허용
                //.anyRequest().authenticated();//나머지 요청은 모두 로그인 해야만 쓸 수 있음.
                .anyRequest().hasRole("USER")
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        //spring security는 csrf라는 기능이 활성화 되어 있다.
        //타사이트에서 data를 보내면서 공격하는 것을 방지하기 위해 csrf토큰을 자동으로 사용.

        //csrf토큰값이 제공안되고 data만 오거나 csrf 토큰 값이 서버측에서 보낸 값과 다르면 403에러가 발생.
        //위에 인증없어도 사용할 수 있도록 했지만 그렇다고 secure한 데이터를 처리할 수는 없다.

        //로그인 form을 submit하니 제대로 동작안함.
        //Creation of SecureRandom instance for session ID generation [SHA512] 경고 발생.
        //403 forbidden 리턴
        //아래와 같이 설정한 후 정상 작동.

        //cors는 브라우저는 request에 대해서 같은 도메인에서만 할수 있고
        //다른 도메인인 경우 cors문제 발생.
        //http.cors().and(); // cors 적용
        //http.csrf().disable(); // rest api의 경우 csrf가 필요하지 않을 수 있다.
        // 후에 좀 더 공부해서 확실히 이해하자.

    }

//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        //static한 리소스 들은 security filter들을 적용하지 마라.
//        web.ignoring()
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//    }


//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(customUserDetailService);
//    }
}
