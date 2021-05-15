import React from "react";
import { Modal, ModalHeader, ModalBody } from "reactstrap";
import cookie from "js-cookie";

function submitRequest(path, requestBody, handlePost, handleError) {
  const JWT = cookie.getJSON("X-AUTH-TOKEN");

  const formData = new FormData();
  console.log(requestBody.content);
  const file = document.getElementById("file").files[0];
  console.log(file);
  formData.append("content", requestBody.content);
  if (file !== undefined) {
    formData.append("file", file);
  }

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
    .catch((error) => {
      if (error.statusCode === 403) {
        alert("다시 로그인 하세요.");
        //this.props.history.push("/");
      }
    });
}

class PostForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: "",
      imgBase64: "",
      imgFile: null,
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleError = this.handleError.bind(this);
    this.handleChangeFile = this.handleChangeFile.bind(this);
    this.setImgBase64 = this.setImgBase64.bind(this);
    this.setImgFile = this.setImgFile.bind(this);
    this.clearImg = this.clearImg.bind(this);
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

  setImgBase64(base64) {
    this.setState({
      imgBase64: base64,
    });
  }

  setImgFile(imgFile) {
    this.setState({
      imgFile: imgFile,
    });
  }

  clearImg() {
    this.setState({
      imgBase64: "",
      imgFile: null,
    });
  }

  handleChangeFile(event) {
    const file = event.target.files[0];

    if (file === undefined) {
      this.clearImg();
      return;
    }

    const type = file.type;

    if (
      !(type === "image/gif" || type === "image/jpeg" || type === "image/png")
    ) {
      alert("gif, jpeg, png 이미지 파일만 업로드 할 수 있습니다.");
      event.target.value = "";
      this.clearImg();
      return;
    }

    let reader = new FileReader();

    reader.onloadend = () => {
      const base64 = reader.result;
      if (base64) {
        this.setImgBase64(base64.toString());
      }
    };

    if (file) {
      reader.readAsDataURL(file);
      this.setImgFile(file);
    }
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
              accept="image/gif,image/jpeg,image/png"
              onChange={this.handleChangeFile}
            />

            <div>
              <img
                src={this.state.imgBase64}
                alt=""
                style={{
                  maxWidth: "150px",
                  maxhHeight: "150px",
                }}
              />
            </div>

            <div className="col-12 mt-2">
              <button type="submit" className="btn btn-primary btn-large">
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
            className="bg-primary text-white"
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
