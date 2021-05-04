import React from "react";
import { Link } from "react-router-dom";
import cookie from "js-cookie";

class Navigation extends React.Component {
  constructor(props) {
    super(props);
    this.handleSignOut = this.handleSignOut.bind(this);
    this.alertEmailVerify = this.alertEmailVerify.bind(this);
  }

  alertEmailVerify(event) {
    event.preventDefault();

    const JWT = cookie.getJSON("X-AUTH-TOKEN");

    if (JWT === undefined) {
      alert("로그인을 하셔야 이용할 수 있습니다.");
    } else {
      alert("이메일 인증을 하셔야 이용할 수 있습니다.");
    }
  }

  handleSignOut(event) {
    event.preventDefault();
    this.props.handleSignedOut();
  }

  render() {
    let isLogin = true;
    let emailVerified = true;

    const user = cookie.getJSON("user");
    const jwt = cookie.getJSON("X-AUTH-TOKEN");

    if (user === undefined || jwt === undefined) {
      isLogin = false;
    } else {
      emailVerified = user.emailVerified;
    }

    return (
      <div className="mb-5">
        <ul className="nav justify-content-center">
          <li className="nav-link">
            {isLogin === false ? (
              <Link
                to="/"
                onClick={() => {
                  this.props.showModalWindow();
                  console.log("show called");
                }}
              >
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
          {emailVerified === true ? (
            <li className="nav-link">
              <Link to="/" onClick={this.props.showPostModalWindow}>
                게시글 작성
              </Link>
            </li>
          ) : (
            <li className="nav-link">
              <Link to="/" onClick={this.alertEmailVerify}>
                게시글 작성
              </Link>
            </li>
          )}
        </ul>
      </div>
    );
  }
}

export default Navigation;
