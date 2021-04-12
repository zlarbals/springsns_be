import React from "react";
import { Button, Card, CardText, CardTitle } from "reactstrap";

// class Post extends React.Component{

// render(){

//   return(

//   <Card body outline color="primary">
//     <CardTitle tag="h5">{this.props.nickname}</CardTitle>
//     <CardText>{this.props.content}</CardText>
//     <Button>댓글 작성</Button>
//   </Card>
//   );
// }
// }

class Posts extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      posts: [],
    };
  }

  componentDidMount() {
    fetch("/post")
      .then((response) => response.json())
      .then((json) => {
        this.setState({
          posts: json,
        });
      });
  }

  render() {
    const posts = this.state.posts;

    let post = posts.map((post) => (
      <Card body outline color="primary" className="mb-2" key={post.id}>
        <CardTitle tag="h5">{post.authorNickname}</CardTitle>
        <CardText>{post.content}</CardText>
        <Button>댓글 작성</Button>
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
