import React from "react";
import {
  Button,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  CardText,
} from "reactstrap";
import cookie from "js-cookie";

async function postAddLike(path, JWT, history, place) {
  await fetch(path, {
    method: "POST",
    headers: {
      "content-Type": "application/json",
      "X-AUTH-TOKEN": JWT,
    },
  })
    .then((response) => {
      if (response.status === 403) {
        alert("다시 로그인 후 이용해주세요.");
        throw new Error("403 error");
      }
    })
    .catch((error) => {
      alert("좋아요 등록에 실패했습니다.");
    });

  history.push(place);
}

async function getComments(path, JWT, setComments, history, place) {
  await fetch(path, {
    method: "GET",
    headers: {
      Accept: "application/json",
      "X-AUTH-TOKEN": JWT,
    },
  })
    .then((response) => {
      if (response.status === 403) {
        alert("다시 로그인 후 이용해주세요.");
        throw new Error("403 error");
      }

      return response.json();
    })
    .then((json) => {
      setComments(json.comments);
    })
    .catch((error) => {
      alert("댓글 가져오기에 실패했습니다.");
    });

  history.push(place);
}

async function submitComment(path, JWT, requestBody, history, place) {
  await fetch(path, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "content-Type": "application/json",
      "X-AUTH-TOKEN": JWT,
    },
    body: JSON.stringify(requestBody),
  })
    .then((response) => {
      if (response.status === 403) {
        alert("다시 로그인 후 이용해주세요.");
        throw new Error("403 error");
      } else if (response.status !== 200) {
        throw new Error("error");
      }
    })
    .catch((error) => {
      alert("댓글 등록에 실패했습니다.");
    });

  history.push(place);
}

function checkCookieLoginData(jwt, user) {
  if (user === undefined || jwt === undefined) {
    alert("로그인 후 이용해주세요.");
    return false;
  } else if (user.emailVerified === false) {
    alert("이메일 인증 후 이용해주세요.");
    return false;
  } else {
    return true;
  }
}

function checkCookieLoginDataWithoutEmailVerity(jwt, user) {
  if (user === undefined || jwt === undefined) {
    alert("로그인 후 이용해주세요.");
    return false;
  } else {
    return true;
  }
}

class PostCard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      comments: [],
      isCommentsOpen: false,
      content: "",
    };
    this.postLike = this.postLike.bind(this);
    this.showComments = this.showComments.bind(this);
    this.setComments = this.setComments.bind(this);
    this.closeComments = this.closeComments.bind(this);
    this.handleInputComment = this.handleInputComment.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.clearContent = this.clearContent.bind(this);
  }

  postLike(event) {
    event.preventDefault();
    const jwt = cookie.getJSON("X-AUTH-TOKEN");
    const user = cookie.getJSON("user");

    if (checkCookieLoginDataWithoutEmailVerity(jwt, user)) {
      const path = "/like/" + this.props.post.id;
      postAddLike(path, jwt, this.props.history, this.props.place);
    }
  }

  showComments(event) {
    event.preventDefault();
    const jwt = cookie.getJSON("X-AUTH-TOKEN");
    const user = cookie.getJSON("user");

    if (checkCookieLoginDataWithoutEmailVerity(jwt, user)) {
      const path = "/comment/post/" + this.props.post.id;
      getComments(
        path,
        jwt,
        this.setComments,
        this.props.history,
        this.props.place
      );
    }
  }

  handleInputComment(event) {
    event.preventDefault();

    const jwt = cookie.getJSON("X-AUTH-TOKEN");
    const user = cookie.getJSON("user");

    if (checkCookieLoginData(jwt, user)) {
      const path = "/comment/post/" + this.props.post.id;
      submitComment(
        path,
        jwt,
        this.state,
        this.props.history,
        this.props.place
      );

      if (this.state.isCommentsOpen === true) {
        getComments(
          path,
          jwt,
          this.setComments,
          this.props.history,
          this.props.place
        );
      }
    }

    this.clearContent();
  }

  closeComments() {
    this.setState({
      isCommentsOpen: !this.state.isCommentsOpen,
    });
  }

  setComments(comments) {
    this.setState({
      comments: comments,
      isCommentsOpen: true,
    });
  }

  handleChange(event) {
    const name = event.target.name;
    const value = event.target.value;
    this.setState({
      [name]: value,
    });
  }

  clearContent() {
    this.setState({
      content: "",
    });
  }

  render() {
    const post = this.props.post;
    let isLogin = true;
    const jwt = cookie.getJSON("X-AUTH-TOKEN");
    const user = cookie.getJSON("user");

    if (jwt === undefined || user === undefined) {
      isLogin = false;
    }

    return (
      <Card outline color="primary" className="mb-2" key={post.id}>
        <CardHeader>{post.authorNickname}</CardHeader>
        <CardBody>
          {post.existFile && (
            <CardText className="text-center">
              <img
                src={`/post/image/${post.fileName}`}
                alt=""
                // style={{
                //   maxWidth: "150px",
                //   maxhHeight: "150px",
                // }}
                className="img-fluid"
              />
            </CardText>
          )}

          <CardText>{post.content}</CardText>
          <Button
            onClick={this.postLike}
            className="mr-2"
            outline
            color="danger"
          >
            {post.like === true ? (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="16"
                height="16"
                fill="currentColor"
                className="bi bi-heart-fill mb-1 mr-1"
                viewBox="0 0 16 16"
              >
                <path
                  fillRule="evenodd"
                  d="M8 1.314C12.438-3.248 23.534 4.735 8 15-7.534 4.736 3.562-3.248 8 1.314z"
                />
              </svg>
            ) : (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="16"
                height="16"
                fill="currentColor"
                className="bi bi-heart mb-1 mr-1"
                viewBox="0 0 16 16"
              >
                <path d="m8 2.748-.717-.737C5.6.281 2.514.878 1.4 3.053c-.523 1.023-.641 2.5.314 4.385.92 1.815 2.834 3.989 6.286 6.357 3.452-2.368 5.365-4.542 6.286-6.357.955-1.886.838-3.362.314-4.385C13.486.878 10.4.28 8.717 2.01L8 2.748zM8 15C-7.333 4.868 3.279-3.04 7.824 1.143c.06.055.119.112.176.171a3.12 3.12 0 0 1 .176-.17C12.72-3.042 23.333 4.867 8 15z" />
              </svg>
            )}
            좋아요
          </Button>
          {this.state.isCommentsOpen === true ? (
            <Button onClick={this.closeComments} outline color="primary">
              댓글 접기
            </Button>
          ) : (
            <Button onClick={this.showComments} outline color="primary">
              댓글 보기
            </Button>
          )}
        </CardBody>
        {this.state.isCommentsOpen === true &&
          (this.state.comments.length !== 0 ? (
            this.state.comments.map((comment) => (
              <CardFooter key={comment.id}>
                {comment.authorNickname} : {comment.content}
              </CardFooter>
            ))
          ) : (
            <CardFooter>작성된 댓글이 없습니다.</CardFooter>
          ))}

        {isLogin && (
          <CardFooter>
            <form onSubmit={this.handleInputComment}>
              <h5 className="mb-4">댓글 작성</h5>
              <div className="form-group">
                <input
                  name="content"
                  type="text"
                  className="form-control"
                  id={post.id}
                  value={this.state.content}
                  onChange={this.handleChange}
                />
                <div className="col-12 mt-2">
                  <button type="submit" className="btn btn-primary btn-large">
                    작성 완료
                  </button>
                </div>
              </div>
            </form>
          </CardFooter>
        )}
      </Card>
    );
  }
}

export default PostCard;
