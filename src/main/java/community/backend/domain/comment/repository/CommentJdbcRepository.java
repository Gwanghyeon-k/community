package community.backend.domain.comment.repository;

import community.backend.domain.comment.dto.response.CommentListItemResponse;
import community.backend.domain.comment.entity.Comment;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentJdbcRepository implements CommentRepository {

  private final JdbcTemplate jdbcTemplate;
  private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  public boolean existsPostById(Long postId) {
    Boolean exists = jdbcTemplate.queryForObject(
        """
            SELECT EXISTS(
              SELECT 1
              FROM posts
              WHERE id = ?
                AND deleted_at IS NULL
            )
            """,
        Boolean.class,
        postId
    );
    return Boolean.TRUE.equals(exists);
  }

  @Override
  public List<CommentListItemResponse> findByPostId(Long postId) {
    String sql = """
        SELECT c.id, u.nickname, u.profile_image_url, c.content, c.updated_at
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.post_id = ?
          AND c.deleted_at IS NULL
        ORDER BY c.id ASC
        """;
    return jdbcTemplate.query(sql, (rs, rowNum) -> new CommentListItemResponse(
        rs.getLong("id"),
        rs.getString("nickname"),
        rs.getString("profile_image_url"),
        rs.getString("content"),
        rs.getTimestamp("updated_at").toLocalDateTime().format(F)
    ), postId);
  }

  @Override
  public long save(Comment comment) {
    String sql = """
        INSERT INTO comments(post_id, user_id, content, created_at, updated_at)
        VALUES (?, ?, ?, NOW(), NOW())
        """;
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, comment.getPostId());
      ps.setLong(2, comment.getUserId());
      ps.setString(3, comment.getContent());
      return ps;
    }, keyHolder);
    return Objects.requireNonNull(keyHolder.getKey()).longValue();
  }

  @Override
  public Optional<Comment> findById(Long commentId) {
    String sql = """
        SELECT id, post_id, user_id, content
        FROM comments
        WHERE id = ?
          AND deleted_at IS NULL
        """;
    List<Comment> rows = jdbcTemplate.query(sql, (rs, rowNum) -> Comment.builder()
        .id(rs.getLong("id"))
        .postId(rs.getLong("post_id"))
        .userId(rs.getLong("user_id"))
        .content(rs.getString("content"))
        .build(), commentId);
    return rows.stream().findFirst();
  }

  @Override
  public int updateContent(Long commentId, String content) {
    return jdbcTemplate.update(
        "UPDATE comments SET content = ?, updated_at = NOW() WHERE id = ? AND deleted_at IS NULL",
        content, commentId
    );
  }

  @Override
  public int softDelete(Long commentId) {
    return jdbcTemplate.update(
        "UPDATE comments SET deleted_at = NOW(), updated_at = NOW() WHERE id = ? AND deleted_at IS NULL",
        commentId
    );
  }

  @Override
  public int increasePostCommentCount(Long postId) {
    return jdbcTemplate.update(
        "UPDATE posts SET comment_count = comment_count + 1 WHERE id = ? AND deleted_at IS NULL",
        postId
    );
  }

  @Override
  public int decreasePostCommentCount(Long postId) {
    return jdbcTemplate.update(
        "UPDATE posts SET comment_count = CASE WHEN comment_count > 0 THEN comment_count - 1 ELSE 0 END WHERE id = ? AND deleted_at IS NULL",
        postId
    );
  }
}

