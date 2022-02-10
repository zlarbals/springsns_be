# 실행 가이드

## [maven으로 직접 실행하는 경우]

### 1. git clone

```
git clone https://github.com/zlarbals/SpringSNS.git
```

### 2. 디렉토리 이동

```
cd springsns/backend
```

### 3. 프로젝트 BUILD

```
./mvnw clean package
```

### 4. 디렉토리 이동

```
cd target
```

### 5. 프로젝트 실행

```
java -jar springsns-0.0.1-SNAPSHOT.jar
```

<br></br>

## [docker 사용 하는 경우]

### 1. git clone

```
git clone https://github.com/zlarbals/SpringSNS.git
```

### 2. 디렉토리 이동

```
cd springsns/backend
```

### 3. 프로젝트 BUILD

```
./mvnw clean package
```

### 4. docker image 생성

```
docker build -t spring-sns-image .
```

### 5. docker container 실행

```
docker run --name spring-sns -p 8080:8080 -d spring-sns-image
```

<br></br>

## [주의사항]

### 1. DB

DB의 경우 InMemory DB인 h2를 사용하므로 따로 설정할 필요 없습니다.

다른 DB로 변경할 경우 yml 설정 파일에서 변경 후 사용하면 됩니다.

<!-- ### 2. 이메일 전송

기본적으로 spring.profiles.active가 local로 설정되어 있으므로

인증 이메일 전송 같은 경우 해당 내용이 로그에 남기 때문에

위 실행가이드를 그대로 따라하면 됩니다.

실제 이메일 전송을 원할 경우 네이버 smtp 설정을 진행한 후에

application-prod.yml 파일의 naver id, password, secret key의 빈칸을 채우신 후에

yml파일에 spring.profiles.active를 prod로 변경하거나

다음과 같이 실행하면 됩니다.

```
java -Dspring.profiles.active=prod -jar springsns-0.0.1-SNAPSHOT.jar
```

도커로 실행할 경우

Dockerfile의 ENTRYPOINT 부분을 다음과 같이 변경하고 위 실행가이드를 따라하면 됩니다.

```
ENTRYPOINT ["java","-jar","spring-sns.jar"] ->

ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","spring-sns.jar"]

하지만 현재 docker로 실행할 경우 naver smtp에서 인증 오류가 발생하기 때문에 해결방법을 찾고 있습니다.
``` -->

<br></br>

# Policy

## 이메일 인증이 필요한 경우(이메일 인증, JWT 모두 필요한 경우)

- 게시글 등록
- 게시글 삭제(게시글 주인만 가능, 좋아요/댓글 존재할 시 삭제할 수 없음)
- 댓글 등록

<br></br>

## 로그인이 필요한 경우(JWT가 필요한 경우)

- 비밀번호 변경, 회원 탈퇴, 인증 이메일 재전송
- 특정 유저가 작성한 게시글 전체 가져오기, 게시글 검색
- 게시글 댓글 보기
- 좋아요 등록/삭제, 좋아요한 게시글 가져오기

<br></br>

## 로그인 필요 없는 경우(JWT 필요없는 경우)

- 회원 가입, 로그인, 이메일 인증
- 게시글 페이징(slice) 가져오기, 게시글에 포함된 이미지 가져오기

<br></br>

# API

## Account

|        기능        | HTTP Method |                   URI                    |   비고   |
| :----------------: | :---------: | :--------------------------------------: | :------: |
|     회원 가입      |    POST     |                 /account                 |
|   비밀번호 변경    |    PATCH    |                 /account                 | JWT 필요 |
|     회원 탈퇴      |   DELETE    |                 /account                 | JWT 필요 |
|       로그인       |    POST     |             /account/sign-in             |
|    이메일 인증     |     GET     | /account/check-email-token?email=&token= |
| 인증 이메일 재전송 |     GET     |       /account/resend-email-token        | JWT 필요 |

<br></br>

## Post

|                  기능                   | HTTP Method |            URI             |             비고             |
| :-------------------------------------: | :---------: | :------------------------: | :--------------------------: |
|               게시글 등록               |    POST     |           /post            |    JWT, 이메일 인증 필요     |
|      게시글 페이징(slice) 가져오기      |     GET     | /post?pageSize=&lastIndex= | pageSize,lastIndex 생략 가능 |
| 특정 유저가 작성한 게시글 전체 가져오기 |     GET     |  /post/account/{nickname}  |           JWT 필요           |
|     게시글에 포함된 이미지 가져오기     |     GET     |  /post/image/{imageName}   |
|               게시글 검색               |     GET     |   /post/search/{keyword}   |           JWT 필요           |
|               게시글 삭제               |   DELETE    |       /post/{postId}       |    JWT, 이메일 인증 필요     |
|        좋아요한 게시글 가져오기         |     GET     |         /post/like         |           JWT 필요           |

<br></br>

## Comment

|       기능       | HTTP Method |          URI           |         비고          |
| :--------------: | :---------: | :--------------------: | :-------------------: |
|    댓글 등록     |    POST     | /comment/post/{postId} | JWT, 이메일 인증 필요 |
| 게시글 댓글 보기 |     GET     | /comment/post/{postId} |       JWT 필요        |

<br></br>

## Like

|       기능       | HTTP Method |      URI       |   비고   |
| :--------------: | :---------: | :------------: | :------: |
| 좋아요 등록/삭제 |    POST     | /like/{postId} | JWT 필요 |

<br></br>

# ERD

![jpg_1](./ETC/ERD.PNG)
