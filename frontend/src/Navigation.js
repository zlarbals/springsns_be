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
    //fetch  -- backend와 연결하면 fetch로 전달할 것.
    this.props.handleSignOut();
    console.log("Handle Sign out");
  }

  render() {
    return (
      <div>
        <ul className="nav justify-content-center">
          <li className="nav-link">
            <Link to="/">Home</Link>
          </li>
          <li className="nav-link">
            <Link
              onClick={() => {
                this.props.showModalWindow();
                console.log("show called");
              }}
            >
              Login
            </Link>
          </li>
          <li className="nav-link">
            <Link to="/Configuration">Configuration</Link>
          </li>
        </ul>
      </div>
    );
  }
}

export default Navigation;
