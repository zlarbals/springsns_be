import React from "react";
import queryString from "query-string";
import Cookies from "js-cookie";

async function getEmailVerify(emailCheckToken, email, history) {
  const path =
    "/check-email-token?token=" + emailCheckToken + "&email=" + email;

  await fetch(path, {
    method: "GET",
    headers: {
      Accept: "application/json",
    },
  })
    .then((response) => response.json())
    .then((json) => {
      Cookies.set("user", json.user);
      alert("이메일 인증에 성공했습니다. 자유롭게 이용해 주세요.");
    })
    .catch((error) => {
      alert("이메일 인증에 실패했습니다. 다시 시도해 주세요.");
    });

  //history.goBack();
  history.push("/");
}

class EmailCheck extends React.Component {
  render() {
    const query = queryString.parse(this.props.location.search);
    getEmailVerify(query.token, query.email, this.props.history);

    return null;
  }
}

export default EmailCheck;
