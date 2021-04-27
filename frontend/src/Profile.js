import React from "react";
import Cookies from "js-cookie";
import { Link } from "react-router-dom";

class Profile extends React.Component {
  constructor(props) {
    super(props);
    this.handleSignOut = this.handleSignOut.bind(this);
  }

  handleSignOut(e) {
    e.preventDefault();
    const user = Cookies.getJSON("user");
    if (user === undefined) {
      console.log("Can't sign out as no user cookie found...");
      return;
    }

    console.log("Sign out: " + user);
    this.props.handleSignedOut();
    console.log("Handle Sign out");
  }

  render() {
    const user = Cookies.getJSON("user");

    let isLogin = false;
    let greetingMessage = "게시글 작성에는 로그인이 필요합니다.";
    let name;
    let isEmailVerified;
    if (user !== undefined) {
      isLogin = true;
      greetingMessage = "환영합니다. ";
      name = user.nickname + "님!";
      isEmailVerified = user.emailVerified;
    }

    console.log(user);

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
              <span className="badge bg-primary rounded-pill">
                {user.postCounts}
              </span>
            </li>
            <li className="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
              <Link to="/posts/my/like">좋아요한 게시글 보기</Link>
              <span className="badge bg-primary rounded-pill">
                {user.likeCounts}
              </span>
            </li>
            {isEmailVerified ? (
              <li className="list-group-item list-group-item-action">
                <Link
                  to="/"
                  onClick={() => {
                    this.props.showPostModalWindow();
                    console.log("profile post modal show");
                  }}
                >
                  게시글 작성
                </Link>
              </li>
            ) : (
              <li className="list-group-item list-group-item-action">
                인증 이메일 다시 보내기
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
              <Link
                to="/"
                onClick={() => {
                  this.props.showModalWindow();
                  console.log("profile login modal show");
                }}
              >
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
