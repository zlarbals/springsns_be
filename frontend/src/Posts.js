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
      if (!state.isLast) {
        setPosts(json);
      }
    })
    .catch((error) => {
      alert("게시글 가져오기에 실패했습니다.");
      history.push("/");
    });
}

class Posts extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      posts: [],
      page: 0,
      isLast: false,
    };
    this.setPosts = this.setPosts.bind(this);
  }

  componentDidMount() {
    const jwt = cookie.getJSON("X-AUTH-TOKEN");

    const path = "/post?page=" + this.state.page;
    getPosts(path, jwt, this.setPosts, this.state, this.props.history);
    window.addEventListener("scroll", this.infiniteScroll);
  }

  componentWillUnmount() {
    window.removeEventListener("scroll", this.infiniteScroll);
  }

  infiniteScroll = () => {
    const { documentElement, body } = document;
    const page = this.state.page;

    const scrollHeight = Math.max(
      documentElement.scrollHeight,
      body.scrollHeight
    );
    const scrollTop = Math.max(documentElement.scrollTop, body.scrollTop);
    const clientHeight = documentElement.clientHeight;

    if (!this.state.isLast && scrollTop + clientHeight >= scrollHeight) {
      this.setState({
        page: page + 1,
      });

      const jwt = cookie.getJSON("X-AUTH-TOKEN");
      const path = "/post?page=" + this.state.page;
      getPosts(path, jwt, this.setPosts, this.state, this.props.history);
    }
  };

  setPosts(json) {
    const posts = this.state.posts;
    const newPosts = json.content;
    const isLast = json.last;
    console.log(json);
    this.setState({
      posts: [...posts, ...newPosts],
      isLast: isLast,
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
