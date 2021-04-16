import React from "react";
import { Link } from "react-router-dom";
import cookie from "js-cookie";

class Navigation extends React.Component {
  constructor(props) {
    super(props);
    this.handleSignOut = this.handleSignOut.bind(this);
  }

  handleSignOut(e) {
    e.preventDefault();
    const user = cookie.getJSON("user");
    if (user === undefined) {
      console.log("Can't sign out as no user cookie found...");
      return;
    }

    console.log("Sign out: " + user);
    this.props.handleSignedOut();
    console.log("Handle Sign out");
  }

  render() {
    let isLogin = false;
    let emailVerified = false;

    const user = cookie.getJSON("user");

    if (user !== undefined) {
      isLogin = true;
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
                Login
              </Link>
            ) : (
              <Link to="/" onClick={this.handleSignOut}>
                Logout
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
              <Link
                to="/"
                onClick={() => {
                  this.props.showPostModalWindow();
                  console.log("post modal called");
                }}
              >
                게시글 작성
              </Link>
            </li>
          ) : (
            <li className="nav-link disabled">
              <Link
                to="/"
                onClick={() => {
                  this.props.showPostModalWindow();
                  console.log("post modal called");
                }}
              >
                게시글 작성
              </Link>
            </li>
          )}
          {/* <li className="nav-link">
            {emailVerified === true && (
              <Link
                onClick={() => {
                  this.props.showPostModalWindow();
                  console.log("post modal called");
                }}
              >
                게시글 작성
              </Link>
            )}
          </li> */}
        </ul>
      </div>
    );
  }
}

export default Navigation;
