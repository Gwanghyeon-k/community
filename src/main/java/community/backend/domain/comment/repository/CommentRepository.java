package community.backend.domain.comment.repository;

import community.backend.domain.comment.dto.response.CommentListItemResponse;
import community.backend.domain.comment.entity.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentRepository {
  boolean existsPostById(Long postId);
  List<CommentListItemResponse> findByPostId(Long postId);
  long save(Comment comment);
  Optional<Comment> findById(Long commentId);
  int updateContent(Long commentId, String content);
  int softDelete(Long commentId);
  int increasePostCommentCount(Long postId);
  int decreasePostCommentCount(Long postId);
}

