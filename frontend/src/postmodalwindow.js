import React from "react";
import { Modal, ModalHeader, ModalBody } from "reactstrap";
import cookie from "js-cookie";

function submitRequest(path, requestBody, handlePost, handleError) {
  const JWT = cookie.getJSON("X-AUTH-TOKEN");

  const formData = new FormData();
  console.log(requestBody.content);
  console.log(document.getElementById("file").files[0]);
  formData.append("content", requestBody.content);
  formData.append("file", document.getElementById("file").files[0]);

  fetch(path, {
    method: "POST",
    headers: {
      Accept: "application/json",
      // "content-Type": "application/json",
      "X-AUTH-TOKEN": JWT,
    },
    //body: JSON.stringify(requestBody),
    body: formData,
  })
    .then((response) => response.json())
    .then((json) => {
      console.log("Post Response received....");
      console.log(json);
      if (json.error === undefined || !json.error) {
        console.log("post success...");
        handlePost();
      } else {
        console.log("post error here");
        handleError(json.error);
      }
    })
    .catch((error) => console.log(error));
}

class PostForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: "",
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleError = this.handleError.bind(this);
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
    submitRequest("/post", this.state, this.props.handlePost, this.handleError);
  }

  handleError(error) {
    this.setState({
      errorMessage: error,
    });
  }

  render() {
    let message = null;
    if (this.state.errorMessage.length !== 0) {
      message = <h5 className="mb-4 textdanger">{this.state.errorMessage}</h5>;
    }

    return (
      <div>
        {message}
        <form onSubmit={this.handleSubmit}>
          <h5 className="mb-4">게시글 작성</h5>
          <div className="form-group">
            <input
              name="content"
              type="text"
              className="form-control"
              id="content"
              onChange={this.handleChange}
            />
            <input
              name="file"
              type="file"
              className="form-contorl"
              id="file"
              onChange={this.handleChange}
            />

            <div className="col-12 mt-2">
              <button type="submit" className="btn btn-success btn-large">
                작성 완료
              </button>
            </div>
          </div>
        </form>
      </div>
    );
  }
}

export default class PostModalWindow extends React.Component {
  render() {
    let modalBody = <PostForm handlePost={this.props.handlePost} />;

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
            className="bg-success text-white"
            toggle={this.props.toggle}
          >
            Spring SNS
          </ModalHeader>
          <ModalBody>{modalBody}</ModalBody>
        </div>
      </Modal>
    );
  }
}
