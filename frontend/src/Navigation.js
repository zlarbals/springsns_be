import React from "react";
import { Link } from "react-router-dom";
import cookie from "js-cookie";

function checkCookieLoginData(jwt, user) {
  if (user === undefined || jwt === undefined) {
    alert("로그인 후 이용해주세요.");
    return false;
  } else if (user.emailVerified === false) {
    alert("이메일 인증 후 이용해주세요.");
    return false;
  } else {
    return true;
  }
}

class Navigation extends React.Component {
  constructor(props) {
    super(props);
    this.handleSignOut = this.handleSignOut.bind(this);
    this.showPostModalWindow = this.showPostModalWindow.bind(this);
  }

  handleSignOut() {
    this.props.handleSignedOut();
  }

  showPostModalWindow(event) {
    event.preventDefault();
    const jwt = cookie.getJSON("X-AUTH-TOKEN");
    const user = cookie.getJSON("user");

    if (checkCookieLoginData(jwt, user)) {
      this.props.showPostModalWindow();
    }
  }

  render() {
    let isLogin = true;

    const user = cookie.getJSON("user");
    const jwt = cookie.getJSON("X-AUTH-TOKEN");

    if (user === undefined || jwt === undefined) {
      isLogin = false;
    }

    return (
      <div className="mb-5">
        <ul className="nav justify-content-center">
          <li className="nav-link">
            {isLogin === false ? (
              <Link to="/" onClick={this.props.showModalWindow}>
                로그인
              </Link>
            ) : (
              <Link to="/" onClick={this.handleSignOut}>
                로그아웃
              </Link>
            )}
          </li>
          <li className="nav-link">
            <h5>
              <Link to="/">Spring SNS</Link>
            </h5>
          </li>
          <li className="nav-link">
            <Link to="/" onClick={this.showPostModalWindow}>
              게시글 작성
            </Link>
          </li>
        </ul>
      </div>
    );
  }
}

export default Navigation;
