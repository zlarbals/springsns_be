import React from "react";
import cookie from "js-cookie";
import { Link } from "react-router-dom";

function ResendEmail(path, jwt) {
  fetch(path, {
    method: "GET",
    headers: {
      Accept: "application/json",
      "X-AUTH-TOKEN": jwt,
    },
  })
    .then((response) => {
      if (response.status === 403) {
        alert("다시 로그인 후 이용해주세요.");
        throw new Error("403 error");
      } else if (response.status !== 200) {
        throw new Error("error");
      } else {
        alert("인증 이메일 보내기에 성공했습니다.");
      }
    })
    .catch((error) => {
      alert("인증 이메일 보내기에 실패했습니다.");
    });
}

function checkCookieLoginDataWithoutEmailVerity(jwt, user) {
  if (user === undefined || jwt === undefined) {
    alert("로그인 후 이용해주세요.");
    return false;
  } else {
    return true;
  }
}

class Profile extends React.Component {
  constructor(props) {
    super(props);
    this.handleSignOut = this.handleSignOut.bind(this);
    this.resendEmail = this.resendEmail.bind(this);
  }

  handleSignOut(event) {
    event.preventDefault();
    this.props.handleSignedOut();
  }

  resendEmail() {
    const jwt = cookie.getJSON("X-AUTH-TOKEN");
    const user = cookie.getJSON("user");

    if (checkCookieLoginDataWithoutEmailVerity(jwt, user)) {
      const path = "/account/email";
      ResendEmail(path, jwt);
    }
  }

  render() {
    const user = cookie.getJSON("user");
    const jwt = cookie.getJSON("X-AUTH-TOKEN");

    let isLogin = false;
    let greetingMessage = "서비스 이용에는 로그인이 필요합니다.";
    let name;
    let isEmailVerified;
    if (user !== undefined && jwt !== undefined) {
      isLogin = true;
      greetingMessage = "환영합니다. ";
      name = user.nickname + "님!";
      isEmailVerified = user.emailVerified;
    }

    return (
      <div className="card border-primary mb-5">
        <div className="card-body">
          <h5 className="card-title">{greetingMessage}</h5>
          <h5 className="card-text">{name}</h5>
        </div>
        {isLogin ? (
          <ul className="list-group list-group-flush">
            <li className="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
              <Link to="/posts/my">내가 작성한 게시글 보기</Link>
            </li>
            <li className="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
              <Link to="/posts/my/like">좋아요한 게시글 보기</Link>
            </li>
            {isEmailVerified ? (
              <li className="list-group-item list-group-item-action">
                <Link to="/" onClick={this.props.showPostModalWindow}>
                  게시글 작성
                </Link>
              </li>
            ) : (
              <li className="list-group-item list-group-item-action">
                <Link to="/" onClick={this.resendEmail}>
                  인증 이메일 다시 보내기
                </Link>
              </li>
            )}
            <li className="list-group-item list-group-item-action">
              <Link to="/" onClick={this.handleSignOut}>
                로그아웃
              </Link>
            </li>
          </ul>
        ) : (
          <ul className="list-group list-group-flush ">
            <li className="list-group-item list-group-item-action">
              <Link to="/" onClick={this.props.showModalWindow}>
                로그인
              </Link>
            </li>
          </ul>
        )}
      </div>
    );
  }
}

export default Profile;
