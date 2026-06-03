package community.backend.domain.comment.repository;

import community.backend.domain.comment.dto.response.CommentListItemResponse;
import community.backend.domain.comment.entity.Comment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @EntityGraph(attributePaths = "user")
  @Query("""
      select c
      from Comment c
      where c.post.id = :postId
        and (:lastCommentId is null or c.id < :lastCommentId)
      order by c.id desc
      """)
  List<Comment> findByPostIdSource(
      @Param("postId") Long postId,
      @Param("lastCommentId") Long lastCommentId,
      Pageable pageable
  );

  default List<CommentListItemResponse> findByPostId(Long postId, Long lastCommentId, int size) {
    Long cursor = (lastCommentId == null || lastCommentId == 0 || Long.MAX_VALUE == lastCommentId) ? null : lastCommentId;
    List<Comment> comments = findByPostIdSource(postId, cursor, PageRequest.of(0, size));
    return comments.stream()
        .map(comment -> new CommentListItemResponse(
            comment.getId(),
            comment.getUser().getNickname(),
            comment.getUser().getProfileImageUrl(),
            comment.getContent(),
            formatDateTime(comment.getUpdatedAt())
        ))
        .toList();
  }

  @EntityGraph(attributePaths = {"user", "post"})
  @Query("select c from Comment c where c.id = :commentId and c.post.id = :postId")
  Optional<Comment> findByIdAndPostId(
      @Param("commentId") Long commentId,
      @Param("postId") Long postId
  );

  private static String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return null;
    }
    return value.format(FORMATTER);
  }
}

