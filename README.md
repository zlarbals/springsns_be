# Policy

## 이메일 인증이 필요한 경우(이메일 인증, JWT 모두 필요한 경우)

- 게시글 등록, 게시글 검색
- 게시글 삭제(게시글 주인만 가능, 좋아요/댓글 존재할 시 삭제할 수 없음)
- 댓글 등록

<br></br>

## 로그인이 필요한 경우(JWT가 필요한 경우)

- 비밀번호 변경, 회원 탈퇴, 인증 이메일 재전송
- 특정 유저가 작성한 게시글 전체 가져오기
- 게시글 댓글 보기
- 좋아요 등록/삭제, 좋아요한 게시글 가져오기

<br></br>

## 로그인 필요 없는 경우(JWT 필요없는 경우)

- 회원 가입, 로그인, 이메일 인증
- 게시글 페이징(slice) 가져오기, 게시글에 포함된 이미지 가져오기

<br></br>

# API

## Account

|        기능        | HTTP Method |             URI             |
| :----------------: | :---------: | :-------------------------: |
|     회원 가입      |    POST     |          /account           |
|   비밀번호 변경    |    PATCH    |          /account           |
|     회원 탈퇴      |   DELETE    |          /account           |
|       로그인       |    POST     |      /account/sign-in       |
|    이메일 인증     |     GET     | /account/check-email-token  |
| 인증 이메일 재전송 |     GET     | /account/resend-email-token |

<br></br>

## Post

|                  기능                   | HTTP Method |             URI             |
| :-------------------------------------: | :---------: | :-------------------------: |
|               게시글 등록               |    POST     |            /post            |
|      게시글 페이징(slice) 가져오기      |     GET     |            /post            |
| 특정 유저가 작성한 게시글 전체 가져오기 |     GET     |  /post/account/{nickname}   |
|     게시글에 포함된 이미지 가져오기     |     GET     | /post/image/{imageName:.\*} |
|               게시글 검색               |     GET     |   /post/search/{keyword}    |
|               게시글 삭제               |   DELETE    |       /post/{postId}        |

<br></br>

## Comment

|       기능       | HTTP Method |          URI           |
| :--------------: | :---------: | :--------------------: |
|    댓글 등록     |    POST     | /comment/post/{postId} |
| 게시글 댓글 보기 |     GET     | /comment/post/{postId} |

<br></br>

## Like

|           기능           | HTTP Method |      URI       |
| :----------------------: | :---------: | :------------: |
|     좋아요 등록/삭제     |    POST     | /like/{postId} |
| 좋아요한 게시글 가져오기 |     GET     |     /like      |
