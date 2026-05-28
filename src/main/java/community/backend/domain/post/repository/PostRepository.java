package community.backend.domain.post.repository;

import community.backend.domain.post.dto.response.PostDetailResponse;
import community.backend.domain.post.dto.response.PostListDetailResponse;
import community.backend.domain.post.entity.Post;
import java.util.List;
import java.util.Optional;

public interface PostRepository {

  long save(Post post);

  List<PostListDetailResponse> findList(Long lastPostId, int size);

  Optional<PostDetailResponse> findDetail(Long postId);

  Optional<Post> findById(Long postId);

  int increaseViewCount(Long postId);

  int updatePost(Long postId, String title, String description, String postImageUrl);

  int softDelete(Long postId);
}
