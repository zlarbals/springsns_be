import { React, Component } from "react";
import Navigation from "./Navigation.js";
import { Route, BrowserRouter as Router } from "react-router-dom";
import Posts from "./Posts.js";
import MyPosts from "./MyPosts";
import LikePosts from "./LikePosts";
import Profile from "./Profile";
import ModalWindow from "./modalwindow";
import PostModalWindow from "./postmodalwindow";
import EmailCheck from "./EmailCheck";
import cookie from "js-cookie";

class App extends Component {
  constructor(props) {
    super(props);
    const user = cookie.getJSON("user");

    this.state = {
      user: user,
      showSignInModal: false,
      showPostModal: false,
    };

    this.handleSignedIn = this.handleSignedIn.bind(this);
    this.handleSignedOut = this.handleSignedOut.bind(this);
    this.showSignInModalWindow = this.showSignInModalWindow.bind(this);
    this.toggleSignInModalWindow = this.toggleSignInModalWindow.bind(this);
    this.showPostModalWindow = this.showPostModalWindow.bind(this);
    this.togglePostModalWindow = this.togglePostModalWindow.bind(this);
    this.showPostModalWindow = this.showPostModalWindow.bind(this);
    this.handlePost = this.handlePost.bind(this);
  }

  showPostModalWindow() {
    const state = this.state;
    const newState = Object.assign({}, state, { showPostModal: true });
    this.setState(newState);
  }

  togglePostModalWindow() {
    const state = this.state;
    const newState = Object.assign({}, state, {
      showPostModal: !state.showPostModal,
    });
    this.setState(newState);
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

  handlePost() {
    console.log("post happening....");
    const state = this.state;

    const newState = Object.assign({}, state, { showPostModal: false });
    this.setState(newState);
  }

  handleSignedOut() {
    console.log("Signed out happening...");
    const state = this.state;
    const newState = Object.assign({}, state, { user: null });
    this.setState(newState);
    console.log("-----------------------");
    console.log(this.state.user);
    console.log(newState);
    console.log("00000000000000000");
    cookie.remove("user");
    cookie.remove("X-AUTH-TOKEN");
  }

  render() {
    let isLogin = true;

    const user = cookie.getJSON("user");

    console.log("!!!!!!!!!!!!");
    console.log(user);
    console.log("@@@@@@@@@@@@@@@");

    if (user === undefined) {
      isLogin = false;
    }

    console.log("here is App.js");

    return (
      <>
        {isLogin === true && user.emailVerified === false && (
          <span className="d-block p-2 bg-primary text-white">
            이메일 인증을 하셔야 게시글을 작성할 수 있습니다. Configuration에서
            인증 이메일을 재전송 할 수 있습니다.
          </span>
        )}

        <div className="container">
          <Router>
            <Navigation
              //user={this.state.user}
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

              {/* <div className="col-md-1"></div> */}

              <div className="col-md-9">
                <Route exact path="/" component={Posts} />

                <Route path="/check-email-token" component={EmailCheck} />
                <Route exact path="/posts/my" component={MyPosts} />
                <Route exact path="/posts/my/like" component={LikePosts} />
              </div>
            </div>

            {/* //우선은 나눠서 모달창 구현하고 후에 합치자.
              //props에 구별할수 있는 부분을 넣어주면 부품처럼 갈아끼워서 사용가능하다.
              //example post=true, signin=false  /  post=false,signin=true */}
            <ModalWindow
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
