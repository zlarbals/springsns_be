package com.springsns.config;

import com.springsns.account.JwtAuthenticationFilter;
import com.springsns.account.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
        http.httpBasic().disable() // security에서 기본으로 생성하는 login페이지 사용 안 함.
                .csrf().disable() // csrf 사용 안 함.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  //JWT 인증 이므로 세션 사용
                .and()
                .authorizeRequests()
                .antMatchers("/users/signin", "/sign-up", "/check-email-token","/h2-console/*").permitAll()//모두 허용.
                .antMatchers(HttpMethod.GET, "/post","/post/image/*").permitAll()//post 같은 경우 GET만 허용
                .anyRequest().hasRole("USER")
                .and()
                .headers().frameOptions().disable()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
    }
}
