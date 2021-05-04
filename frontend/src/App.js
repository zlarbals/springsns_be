import { React, Component } from "react";
import Navigation from "./Navigation.js";
import { Route, BrowserRouter as Router } from "react-router-dom";
import Posts from "./Posts.js";
import MyPosts from "./MyPosts";
import LikePosts from "./LikePosts";
import Profile from "./Profile";
import LoginModalWindow from "./LoginModalWindow";
import PostModalWindow from "./PostModalWindow";
import EmailCheck from "./EmailCheck";
import cookie from "js-cookie";

class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
      showSignInModal: false,
      showPostModal: false,
      showCommentModal: false,
    };

    this.showSignInModalWindow = this.showSignInModalWindow.bind(this);
    this.toggleSignInModalWindow = this.toggleSignInModalWindow.bind(this);
    this.handleSignedIn = this.handleSignedIn.bind(this);
    this.handleSignedOut = this.handleSignedOut.bind(this);
    this.showPostModalWindow = this.showPostModalWindow.bind(this);
    this.togglePostModalWindow = this.togglePostModalWindow.bind(this);
    this.handlePost = this.handlePost.bind(this);
  }

  showSignInModalWindow() {
    const newState = Object.assign(this.state, { showSignInModal: true });
    this.setState(newState);
  }

  toggleSignInModalWindow() {
    const newState = Object.assign(this.state, {
      showSignInModal: !this.state.showSignInModal,
    });
    this.setState(newState);
  }

  handleSignedIn() {
    const newState = Object.assign(this.state, {
      showSignInModal: false,
    });
    this.setState(newState);
  }

  handleSignedOut() {
    const newState = Object.assign(this.state, {
      showSignInModal: false,
      showPostModal: false,
    });
    this.setState(newState);
    cookie.remove("user");
    cookie.remove("X-AUTH-TOKEN");
  }

  showPostModalWindow() {
    const newState = Object.assign(this.state, { showPostModal: true });
    this.setState(newState);
  }

  togglePostModalWindow() {
    const newState = Object.assign(this.state, {
      showPostModal: !this.state.showPostModal,
    });
    this.setState(newState);
  }

  handlePost() {
    const newState = Object.assign(this.state, { showPostModal: false });
    this.setState(newState);
  }

  render() {
    let isLogin = true;
    const user = cookie.getJSON("user");
    const jwt = cookie.getJSON("X-AUTH-TOKEN");

    if (user === undefined || jwt === undefined) {
      isLogin = false;
    }

    return (
      <>
        {isLogin === true && user.emailVerified === false && (
          <span className="d-block p-2 bg-primary text-white">
            이메일 인증 후 게시글을 작성할 수 있습니다.
          </span>
        )}

        <div className="container">
          <Router>
            <Navigation
              user={user}
              handleSignedOut={this.handleSignedOut}
              showModalWindow={this.showSignInModalWindow}
              showPostModalWindow={this.showPostModalWindow}
            />

            <div className="row">
              <div className="col-md-3">
                <Profile
                  handleSignedOut={this.handleSignedOut}
                  showModalWindow={this.showSignInModalWindow}
                  showPostModalWindow={this.showPostModalWindow}
                />
              </div>

              <div className="col-md-9">
                <Route exact path="/" component={Posts} />

                <Route path="/check-email-token" component={EmailCheck} />
                <Route exact path="/posts/my" component={MyPosts} />
                <Route exact path="/posts/my/like" component={LikePosts} />
              </div>
            </div>

            <LoginModalWindow
              handleSignedIn={this.handleSignedIn}
              showModal={this.state.showSignInModal}
              toggle={this.toggleSignInModalWindow}
              showModalWindow={this.showSignInModalWindow}
            />

            <PostModalWindow
              handlePost={this.handlePost}
              showModal={this.state.showPostModal}
              toggle={this.togglePostModalWindow}
              showModalWindow={this.showPostModalWindow}
            />
          </Router>
        </div>
      </>
    );
  }
}

export default App;
