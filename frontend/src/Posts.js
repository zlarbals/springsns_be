import React from "react";
import { Button, Card, CardText, CardTitle } from "reactstrap";
import cookie from "js-cookie";

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

class Posts extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      posts: [],
    };
    this.handleLike = this.handleLike.bind(this);
  }

  componentDidMount() {
    const JWT = cookie.getJSON("X-AUTH-TOKEN");

    // fetch("/post")
    //   .then((response) => response.json())
    //   .then((json) => {
    //     this.setState({
    //       posts: json,
    //     });
    //   });

    fetch("/post", {
      method: "GET",
      headers: {
        // Accept: "application/json",
        // "content-Type": "application/json",
        "X-AUTH-TOKEN": JWT,
      },
      // body: JSON.stringify(requestBody),
    })
      .then((response) => response.json())
      .then((json) => {
        this.setState({
          posts: json,
        });
      });
  }

  handleLike(post) {
    const path = "like/" + post.id;
    postAddLike(path);
  }

  render() {
    const posts = this.state.posts;
    console.log(posts);

    let post = posts.map((post) => (
      <Card body outline color="primary" className="mb-2" key={post.id}>
        <CardTitle tag="h5">{post.authorNickname}</CardTitle>
        <CardText>{post.content}</CardText>
        <Button
          onClick={() => this.handleLike(post)}
          className="border border-danger reounded-pill bg-white text-danger mb-2"
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
        <Button>댓글 보기</Button>
      </Card>
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
