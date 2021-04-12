import queryString from "query-string";
import Cookies from "js-cookie";
import { Redirect } from "react-router-dom";

function getEmailVerify(emailCheckToken, email) {
  const path =
    "/check-email-token?token=" + emailCheckToken + "&email=" + email;
  console.log(path);
  fetch(path)
    .then((response) => response.json())
    .then((json) => {
      console.log("post email check token function");
      Cookies.set("user", json.user);
      window.location.href = "http://localhost:3000";
    })
    .catch((error) => console.log(error));
}

const EmailCheck = ({ location }) => {
  const query = queryString.parse(location.search);
  getEmailVerify(query.token, query.email);

  return <Redirect to="/" />;
};

export default EmailCheck;
