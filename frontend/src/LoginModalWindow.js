import React from "react";
import cookie from "js-cookie";
import { Modal, ModalHeader, ModalBody } from "reactstrap";

function submitSignInRequest(path, requestBody, handleSignedIn, handleError) {
  fetch(path, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "content-Type": "application/json",
    },
    body: JSON.stringify(requestBody),
  })
    .then((response) => {
      if (response.status !== 200) {
        throw new Error("error");
      }

      return response.json();
    })
    .then((json) => {
      cookie.set("X-AUTH-TOKEN", json.jwtToken);
      cookie.set("user", json.user);
      handleSignedIn();
    })
    .catch((error) => {
      handleError();
    });
}

function submitSignUpRequest(path, requestBody, handleError, showSignInModal) {
  fetch(path, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "content-Type": "application/json",
    },
    body: JSON.stringify(requestBody),
  })
    .then((response) => {
      console.log(response);
      if (response.json.status !== 200) {
        throw new Error(response.json().error);
      }

      showSignInModal();
      //return response.json();
    })
    // .then((json) => {
    //   showSignInModal();
    // })
    .catch((error) => {
      console.log(error);
      handleError(error);
    });
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

    submitSignInRequest(
      "/users/signin",
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
                onClick={this.props.showSignUpForm}
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
      "/sign-up",
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

    this.showSignUpForm = this.showSignUpForm.bind(this);
    this.handleModalClose = this.handleModalClose.bind(this);
  }

  showSignUpForm() {
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
        showSignUpForm={this.showSignUpForm}
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
