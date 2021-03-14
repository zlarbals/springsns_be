import React from "react";
import { Link } from "react-router-dom";

class Navigation extends React.Component {
  render() {
    return (
      <div>
        <ul className="nav justify-content-center">
          <li className="nav-link">
            <Link to="/">Home</Link>
          </li>
          <li className="nav-link">
            <Link to="/Login">Login</Link>
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
