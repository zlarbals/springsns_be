import { React, Component } from "react";
import Navigation from "./Navigation.js";
import { Route, BrowserRouter as Router } from "react-router-dom";
import Configuration from "./Configuration.js";
import Posts from "./Posts.js";
import Profile from "./Profile";
import ModalWindow from "./modalwindow";
import cookie from "js-cookie";

class App extends Component {
  constructor(props) {
    super(props);
    const user = cookie.getJSON("user") || { loggedin: false };
    this.state = {
      user: user,
      showSignInModal: false,
    };

    this.handleSignedIn = this.handleSignedIn.bind(this);
    this.handleSignedOut = this.handleSignedOut.bind(this);
    this.showSignInModalWindow = this.showSignInModalWindow.bind(this);
    this.toggleSignInModalWindow = this.toggleSignInModalWindow.bind(this);
  }

  handleSignedIn(user) {
    console.log("Sign in happening...");
    const state = this.state;

    //user 객체의 속성 복사
    const newState = Object.assign({}, state, {
      user: user,
      showSignInModal: false,
    });
    this.setState(newState);
  }

  handleSignedOut() {
    console.log("Signed out happening...");
    const state = this.state;
    const newState = Object.assign({}, state, { user: { loggedin: false } });
    this.setState(newState);
    cookie.set("user", { loggedin: false });

    window.location.href = "http://localhost:3000";
  }

  showSignInModalWindow() {
    const state = this.state;
    const newState = Object.assign({}, state, { showSignInModal: true });
    this.setState(newState);
  }

  toggleSignInModalWindow() {
    const state = this.state;
    const newState = Object.assign({}, state, {
      showSignInModal: !state.showSignInModal,
    });
    this.setState(newState);
  }

  render() {
    //쿠키 확인하는 것으로 변경 필요.
    let isLogin = true;
    return (
      <div className="container">
        <Router>
          <Navigation
            user={this.state.user}
            handleSignedOut={this.handleSignedOut}
            showModalWindow={this.showSignInModalWindow}
          />

          <div className="row">
            {isLogin === true && (
              <div className="col-6 col-md-4">
                <Profile />
              </div>
            )}

            <div className="col-md-8">
              <Route exact path="/" component={Posts} />
              {/* <Route path="/Login" component={Login} /> */}
              <Route path="/Configuration" component={Configuration} />
            </div>
          </div>

          <ModalWindow
            handleSignedIn={this.handleSignedIn}
            showModal={this.state.showSignInModal}
            toggle={this.toggleSignInModalWindow}
          />
        </Router>
      </div>
    );
  }
}

export default App;
