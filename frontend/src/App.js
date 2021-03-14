import { React, Component } from "react";
import Navigation from "./Navigation.js";
import { Route, BrowserRouter as Router } from "react-router-dom";
import Configuration from "./Configuration.js";
import Login from "./Login.js";
import Posts from "./Posts.js";
import Profile from "./Profile";

class App extends Component {
  render() {
    let isLogin = false;
    return (
      <div className="container">
        <Router>
          <Navigation />

          <div className="row">
            {isLogin === true && (
              <div className="col-6 col-md-4">
                <Profile />
              </div>
            )}
            git test
            <div className="col-md-8">
              <Route exact path="/" component={Posts} />
              <Route path="/Login" component={Login} />
              <Route path="/Configuration" component={Configuration} />
            </div>
          </div>
        </Router>
      </div>
    );
  }
}

export default App;
