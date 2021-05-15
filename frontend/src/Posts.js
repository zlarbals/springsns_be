import React from "react";
import cookie from "js-cookie";
import PostCard from "./PostCard";

function getPosts(path, jwt, setPosts, state, history) {
  fetch(path, {
    method: "GET",
    headers: {
      Accept: "application/json",
      "X-AUTH-TOKEN": jwt,
    },
  })
    .then((response) => {
      if (response.status === 403) {
        alert("다시 로그인 후 이용해주세요.");
        throw new Error("403 error");
      } else if (response.status === 200) {
        return response.json();
      } else {
        throw new Error("error");
      }
    })
    .then((json) => {
      if (JSON.stringify(state.posts) !== JSON.stringify(json)) {
        setPosts(json);
      }
    })
    .catch((error) => {
      alert("게시글 가져오기에 실패했습니다.");
      //history.goBack();
      history.push("/");
    });
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
    const jwt = cookie.getJSON("X-AUTH-TOKEN");

    getPosts("/post", jwt, this.setPosts, this.state, this.props.history);
  }

  componentDidUpdate() {
    const jwt = cookie.getJSON("X-AUTH-TOKEN");

    getPosts("/post", jwt, this.setPosts, this.state, this.props.history);
  }

  setPosts(json) {
    this.setState({
      posts: json,
    });
  }

  render() {
    const posts = this.state.posts;
    const place = "/";
    console.log(posts);

    let post = posts.map((post, index) => (
      <PostCard
        key={post.id}
        post={post}
        history={this.props.history}
        place={place}
        showCommentModalWindow={this.props.showCommentModalWindow}
      />
    ));

    if (post.length === 0) {
      post = "게시글이 없습니다.";
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
