import Cookies from "js-cookie";
import React from "react";

class Profile extends React.Component {
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
      <div className="card border-primary">
        <div className="card-body">
          <h5 className="card-title">{greetingMessage}</h5>
          <h5 className="card-text">{name}</h5>
        </div>
        {isLogin ? (
          <ul className="list-group list-group-flush">
            <li className="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
              내 게시글 갯수
              <span className="badge bg-primary rounded-pill">
                {user.likeCount}
              </span>
            </li>
            <li className="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
              내가 받은 좋아요 갯수
              <span className="badge bg-primary rounded-pill">
                {user.likeCount}
              </span>
            </li>
            {isEmailVerified ? (
              <li className="list-group-item list-group-item-action">
                게시글 작성
              </li>
            ) : (
              <li className="list-group-item list-group-item-action">
                인증 이메일 다시 보내기
              </li>
            )}
            <li className="list-group-item list-group-item-action">로그아웃</li>
          </ul>
        ) : (
          <ul className="list-group list-group-flush ">
            <li className="list-group-item list-group-item-action">로그인</li>
          </ul>
        )}
      </div>
    );
  }
}

export default Profile;
