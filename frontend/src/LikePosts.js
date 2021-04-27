import React from "react";
import cookie from "js-cookie";
import PostCard from "./PostCard";

class LikePosts extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      posts: [],
    };
    this.setPosts = this.setPosts.bind(this);
  }

  componentDidMount() {
    const JWT = cookie.getJSON("X-AUTH-TOKEN");

    fetch("/post/my/like", {
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
  }

  componentDidUpdate() {
    const JWT = cookie.getJSON("X-AUTH-TOKEN");

    fetch("/post/my/like", {
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
    const place = "/posts/my/like";
    console.log(posts);

    let post = posts.map((post, index) => (
      <PostCard
        key={post.id}
        post={post}
        history={this.props.history}
        place={place}
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

export default LikePosts;
