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

// function getPost(setPosts) {
//   const JWT = cookie.getJSON("X-AUTH-TOKEN");

//   fetch("/post", {
//     method: "GET",
//     headers: {
//       "X-AUTH-TOKEN": JWT,
//     },
//   })
//     .then((response) => response.json())
//     .then((json) => {
//       setPosts(json);
//     });
// }

function postAddLike(path) {
  console.log(path);
  const JWT = cookie.getJSON("X-AUTH-TOKEN");

  fetch(path, {
    method: "POST",
    headers: {
      // Accept: "application/json",
      // "content-Type": "application/json",
      "X-AUTH-TOKEN": JWT,
    },
    // body: JSON.stringify(requestBody),
  })
    .then((response) => {
      console.log("hrere is post like here is maybe success");
    })
    .catch((error) => {
      console.log("here is post like error");
      console.log(error);
    });
}

function getComments(path, setComments) {
  const JWT = cookie.getJSON("X-AUTH-TOKEN");
  console.log(path);
  fetch(path, {
    method: "GET",
    headers: {
      "X-AUTH-TOKEN": JWT,
    },
  })
    .then((response) => response.json())
    .then((json) => {
      console.log("here is getComments success");
      console.log(json);
      setComments(json);
    });
}

class PostCard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      comments: [],
      isCommentsOpen: false,
    };
    this.handleLike = this.handleLike.bind(this);
    this.showComments = this.showComments.bind(this);
    this.setComments = this.setComments.bind(this);
    this.closeComments = this.closeComments.bind(this);
  }

  handleLike(post) {
    const path = "like/" + post.id;
    postAddLike(path);
    console.log("here is history start");
    this.props.history.push("/");
  }

  showComments(post) {
    const path = "comment/post/" + post.id;
    getComments(path, this.setComments);
  }

  closeComments() {
    this.setState({
      isCommentsOpen: !this.state.isCommentsOpen,
    });
  }

  setComments(comments) {
    this.setState({
      comments: comments,
      isCommentsOpen: !this.state.isCommentsOpen,
    });
  }

  render() {
    const post = this.props.post;

    return (
      <Card outline color="primary" className="mb-2" key={post.id}>
        <CardHeader>{post.authorNickname}</CardHeader>
        <CardBody>
          <CardText>{post.content}</CardText>
          <Button
            onClick={() => this.handleLike(post)}
            className="border border-danger reounded-pill bg-white text-danger mr-2"
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
            <Button onClick={() => this.closeComments()} color="primary">
              댓글 접기
            </Button>
          ) : (
            <Button onClick={() => this.showComments(post)} color="primary">
              댓글 보기
            </Button>
          )}
        </CardBody>
        {this.state.isCommentsOpen === true &&
          this.state.comments.map((comment) => (
            <CardFooter key={comment.id}>
              {comment.authorNickname} : {comment.content}
            </CardFooter>
          ))}
      </Card>
    );
  }
}

class Posts extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      posts: [],
    };
    this.setPosts = this.setPosts.bind(this);
  }

  componentDidMount() {
    const JWT = cookie.getJSON("X-AUTH-TOKEN");

    fetch("/post", {
      method: "GET",
      headers: {
        "X-AUTH-TOKEN": JWT,
      },
    })
      .then((response) => response.json())
      .then((json) => {
        this.setState({
          posts: json,
        });
      });
    //getPost(this.setPosts);
  }

  componentDidUpdate() {
    const JWT = cookie.getJSON("X-AUTH-TOKEN");

    fetch("/post", {
      method: "GET",
      headers: {
        "X-AUTH-TOKEN": JWT,
      },
    })
      .then((response) => response.json())
      .then((json) => {
        if (JSON.stringify(this.state.posts) !== JSON.stringify(json)) {
          this.setState({
            posts: json,
          });
        }
      });
  }

  setPosts(json) {
    this.setState({
      posts: json,
    });
  }

  render() {
    const posts = this.state.posts;
    console.log(posts);

    let post = posts.map((post, index) => (
      <PostCard key={post.id} post={post} history={this.props.history} />
    ));

    if (post.length === 0) {
      post = "아직 게시글이 없습니다.";
    }

    return (
      <div className="container">
        <div className="row">
          <div className="col-sm">{post}</div>
        </div>
      </div>
    );
  }
}

export default Posts;
