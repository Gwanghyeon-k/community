package community.backend.domain.comment.repository;

import community.backend.domain.comment.entity.Comment;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @EntityGraph(attributePaths = {"user", "post"})
  @Query("select c from Comment c where c.id = :commentId and c.post.id = :postId")
  Optional<Comment> findByIdAndPostId(
      @Param("commentId") Long commentId,
      @Param("postId") Long postId
  );
}

