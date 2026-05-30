package community.backend.domain.comment.repository;

import community.backend.domain.comment.dto.response.CommentListItemResponse;
import community.backend.domain.comment.entity.Comment;
import static community.backend.domain.comment.repository.CommentSql.DECREASE_POST_COMMENT_COUNT;
import static community.backend.domain.comment.repository.CommentSql.EXISTS_POST_BY_ID;
import static community.backend.domain.comment.repository.CommentSql.FIND_COMMENT_BY_ID;
import static community.backend.domain.comment.repository.CommentSql.FIND_COMMENTS_BY_POST_ID;
import static community.backend.domain.comment.repository.CommentSql.INCREASE_POST_COMMENT_COUNT;
import static community.backend.domain.comment.repository.CommentSql.SAVE_COMMENT;
import static community.backend.domain.comment.repository.CommentSql.SOFT_DELETE;
import static community.backend.domain.comment.repository.CommentSql.UPDATE_CONTENT;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

  /** 게시글 존재 확인 메서드. */
  @Override
  public boolean existsPostById(Long postId) {
    Boolean exists = jdbcTemplate.queryForObject(
        EXISTS_POST_BY_ID,
        Boolean.class,
        postId
    );
    return Boolean.TRUE.equals(exists);
  }

  /** 게시글의 댓글 리스트 조회 (작성 순서 정렬)  */
  @Override
  public List<CommentListItemResponse> findByPostId(Long postId) {
    return jdbcTemplate.query(FIND_COMMENTS_BY_POST_ID, (rs, rowNum) -> toCommentListItem(rs), postId);
  }

  /** 댓글 저장 */
  @Override
  public long save(Comment comment) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(SAVE_COMMENT, Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, comment.getPostId());
      ps.setLong(2, comment.getUserId());
      ps.setString(3, comment.getContent());
      return ps;
    }, keyHolder);
    return Objects.requireNonNull(keyHolder.getKey()).longValue();
  }

  /** 댓글 ID로 댓글 상세 조회  */
  @Override
  public Optional<Comment> findById(Long commentId) {
    List<Comment> rows = jdbcTemplate.query(FIND_COMMENT_BY_ID, (rs, rowNum) -> toComment(rs), commentId);
    return rows.stream().findFirst();
  }

  /** 댓글 내용 수정 */
  @Override
  public int updateContent(Long commentId, String content) {
    return jdbcTemplate.update(UPDATE_CONTENT, content, commentId);
  }

  /** 댓글 삭제. */
  @Override
  public int softDelete(Long commentId) {
    return jdbcTemplate.update(SOFT_DELETE, commentId);
  }

  /** 게시글의 댓글 수 증가 메서드 -> 댓글 작성 시 같이 반영 */
  @Override
  public int increasePostCommentCount(Long postId) {
    return jdbcTemplate.update(INCREASE_POST_COMMENT_COUNT, postId);
  }

  /**
   * 게시글 댓글 수 감소 -> 댓글 삭제 시 같이 반영
   * 댓글 수가 0 아래로 내려갈 수 없도록 검증 로직 추가
   * */
  @Override
  public int decreasePostCommentCount(Long postId) {
    return jdbcTemplate.update(DECREASE_POST_COMMENT_COUNT, postId);
  }

  private CommentListItemResponse toCommentListItem(ResultSet rs) throws SQLException {
    return new CommentListItemResponse(
        rs.getLong("id"),
        rs.getString("nickname"),
        rs.getString("profile_image_url"),
        rs.getString("content"),
        rs.getTimestamp("updated_at").toLocalDateTime().format(F)
    );
  }

  private Comment toComment(ResultSet rs) throws SQLException {
    return Comment.builder()
        .id(rs.getLong("id"))
        .postId(rs.getLong("post_id"))
        .userId(rs.getLong("user_id"))
        .content(rs.getString("content"))
        .build();
  }
}

