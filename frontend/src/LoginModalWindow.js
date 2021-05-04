import React from "react";
import cookie from "js-cookie";
import { Modal, ModalHeader, ModalBody } from "reactstrap";

function submitRequest(path, requestBody, handleSignedIn, handleError) {
  fetch(path, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "content-Type": "application/json",
    },
    body: JSON.stringify(requestBody),
  })
    .then((response) => response.json())
    .then((json) => {
      console.log("Response received....");
      console.log(json);
      if (json.error === undefined || !json.error) {
        console.log("Sign in Success...");
        cookie.set("X-AUTH-TOKEN", json.jwtToken);
        cookie.set("user", json.user);
        handleSignedIn(json.user);
      } else {
        console.log("Sign in error here");
        handleError(json.error);
      }
    })
    .catch((error) => console.log(error));
}

function submitSignUpRequest(path, requestBody, handleError, showSignInModal) {
  console.log(path);

  fetch(path, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "content-Type": "application/json",
    },
    body: JSON.stringify(requestBody),
  })
    .then((response) => response.json())
    .then((json) => {
      console.log("Response received....");
      if (json.error === undefined || !json.error) {
        console.log("Sign Up Success...");
        console.log(json);
        showSignInModal();
      } else {
        console.log(json);
        handleError();
      }
    })
    .catch((error) => console.log(error));
}

class SignInForm extends React.Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleError = this.handleError.bind(this);
    this.state = {
      errorMessage: "",
    };
  }

  handleChange(event) {
    const name = event.target.name;
    const value = event.target.value;
    this.setState({
      [name]: value,
    });
  }

  handleSubmit(event) {
    event.preventDefault();
    submitRequest(
      "users/signin",
      this.state,
      this.props.handleSignedIn,
      this.handleError
    );
  }

  handleError() {
    this.setState({
      errorMessage: "로그인에 실패했습니다.",
    });
  }

  render() {
    let message = null;
    if (this.state.errorMessage.length !== 0) {
      message = <h5 className="mb-4 text-danger">{this.state.errorMessage}</h5>;
    }

    return (
      <div>
        {message}
        <form onSubmit={this.handleSubmit}>
          <h5 className="mb-4">Sign In</h5>
          <div className="form-group">
            <label htmlFor="email">Email:</label>
            <input
              name="email"
              type="email"
              className="form-control"
              id="email"
              onChange={this.handleChange}
            />
          </div>
          <div className="form-group">
            <label htmlFor="pass">Password:</label>
            <input
              name="password"
              type="password"
              className="form-control"
              id="pass"
              onChange={this.handleChange}
            />
          </div>
          <div className="form-row text-center">
            <div className="col-12 mt-2">
              <button type="submit" className="btn btn-primary btn-large">
                Sign In
              </button>
            </div>
            <div className="col-12 mt-2">
              <button
                type="submit"
                className="btn btn-link"
                onClick={() => this.props.handleNewUser()}
              >
                회원 가입
              </button>
            </div>
          </div>
        </form>
      </div>
    );
  }
}

class SignUpForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      errormessage: [],
    };

    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleError = this.handleError.bind(this);
  }

  handleSubmit(event) {
    event.preventDefault();
    const userInfo = this.state;

    if (userInfo.pass1 !== userInfo.pass2) {
      // input 밑에 같은지 다른지 미리 표시해둘 것. handle change로 하면 될 듯하다.
      alert("PASSWORD DO NOT MATCH");
      return;
    }

    const requestBody = {
      nickname: userInfo.nickname,
      email: userInfo.email,
      password: userInfo.pass1,
    };

    submitSignUpRequest(
      "sign-up",
      requestBody,
      this.handleError,
      this.props.showSignInModal
    );

    // this.props.showSignInModal();

    console.log("Registration form: " + requestBody);
  }

  handleChange(event) {
    event.preventDefault();
    const name = event.target.name;
    const value = event.target.value;
    this.setState({
      [name]: value,
    });
  }

  handleError(error) {
    this.setState({
      errormessage: error,
    });
  }

  render() {
    // let message = null;
    // if (this.state.errormessage.length !== 0) {
    //   message = <h5 className="mb-4 text-danger">{this.state.errormessage}</h5>;
    // }

    const errorMessages = this.state.errormessage;
    let message = errorMessages.map((errorMessage) => (
      <h5 className="mb-4 text-danger">{errorMessage}</h5>
    ));

    return (
      <div>
        {message}
        <form onSubmit={this.handleSubmit}>
          <h5 className="mb-4">Sign Up</h5>
          <div className="form-group">
            <label htmlFor="username">Nickname:</label>
            <input
              id="nickname"
              name="nickname"
              className="form-control"
              placeholder="John Doe"
              type="text"
              onChange={this.handleChange}
            />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email:</label>
            <input
              type="email"
              name="email"
              className="form-control"
              id="email"
              onChange={this.handleChange}
            />
          </div>
          <div className="form-group">
            <label htmlFor="pass">Password:</label>
            <input
              type="password"
              name="pass1"
              className="form-control"
              id="pass1"
              onChange={this.handleChange}
            />
          </div>
          <div className="form-group">
            <label htmlFor="pass">Confirm password:</label>
            <input
              type="password"
              name="pass2"
              className="form-control"
              id="pass2"
              onChange={this.handleChange}
            />
          </div>
          <div className="form-row text-center">
            <div className="col-12 mt-2">
              <button type="submit" className="btn btn-primary btn-large">
                SignUp
              </button>
            </div>
          </div>
        </form>
      </div>
    );
  }
}

export default class LoginModalWindow extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showSignUpForm: false,
    };

    this.handleNewUser = this.handleNewUser.bind(this);
    this.handleModalClose = this.handleModalClose.bind(this);
  }

  handleNewUser() {
    this.setState({
      showSignUpForm: true,
    });
  }

  handleModalClose() {
    this.setState({
      showSignUpForm: false,
    });
  }

  render() {
    let modalBody = (
      <SignInForm
        handleNewUser={this.handleNewUser}
        handleSignedIn={this.props.handleSignedIn}
      />
    );
    if (this.state.showSignUpForm === true) {
      modalBody = <SignUpForm showSignInModal={this.handleModalClose} />;
    }

    return (
      <Modal
        id="register"
        tabIndex="-1"
        role="dialog"
        isOpen={this.props.showModal}
        toggle={this.props.toggle}
        onClosed={this.handleModalClose}
      >
        <div role="document">
          <ModalHeader
            toggle={this.props.toggle}
            className="bg-primary text-white"
          >
            Spring SNS
          </ModalHeader>
          <ModalBody>{modalBody}</ModalBody>
        </div>
      </Modal>
    );
  }
}