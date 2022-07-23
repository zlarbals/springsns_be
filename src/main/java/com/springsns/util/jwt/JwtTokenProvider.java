package com.springsns.util.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {  // JWT토큰 생성 및 유효성 검증

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    //1시간
    private long tokenValidTime = 1000L * 60 * 60;

    @PostConstruct
    protected void init() {
        //secret key를 base64로 인코딩
        SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes());
    }

    //JWT 토큰 생성
    public String createToken(String accountEmail) {
        Claims claims = Jwts.claims().setSubject(accountEmail); // JWT payload에 저장되는 정보 단위
        Date now = new Date();
        Date expireTime = new Date(now.getTime()+tokenValidTime);

        return Jwts.builder()
                .setClaims(claims) //정보 저장
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(expireTime)  //Expire time 설정
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  //사용할 암호화 알고리즘과 signature에 들어갈 secret 값 설정
                .compact();
    }

    //토큰에서 회원 정보 추출
    public String getAccountEmail(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }

    //Request의 Header에서 token 값 가져오기 "Authorization" : "Bearer TOKEN값"
    public String extractToken(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if(header!=null) {
            String[] authorization = request.getHeader("Authorization").split(" ");

            if(authorization.length==2 && authorization[0].equals("Bearer")){
                return authorization[1];
            }
        }

        return null;
    }

    //토큰의 유효성 + 만료인자 확인
    public boolean validateJwtToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

}
