package community.backend.domain.comment.repository;

final class CommentSql {

  private CommentSql() {
  }

  static final String EXISTS_POST_BY_ID = """
      SELECT EXISTS(
        SELECT 1
        FROM posts
        WHERE id = ?
      )
      """;

  static final String FIND_COMMENTS_BY_POST_ID = """
      SELECT c.id, u.nickname, u.profile_image_url, c.content, c.updated_at
      FROM comments c
      JOIN users u ON c.user_id = u.id
      WHERE c.post_id = ?
        AND c.id < ?
      ORDER BY c.id DESC
      LIMIT ?
      """;

  static final String SAVE_COMMENT = """
      INSERT INTO comments(post_id, user_id, content, created_at, updated_at)
      VALUES (?, ?, ?, NOW(), NOW())
      """;

  static final String FIND_COMMENT_BY_ID = """
      SELECT id, post_id, user_id, content
      FROM comments
      WHERE id = ?
      """;

  static final String UPDATE_CONTENT = """
      UPDATE comments
      SET content = ?, updated_at = NOW()
      WHERE id = ?
      """;

  static final String DELETE_COMMENT = """
      DELETE FROM comments
      WHERE id = ?
      """;

  static final String INCREASE_POST_COMMENT_COUNT = """
      UPDATE posts
      SET comment_count = comment_count + 1
      WHERE id = ?
      """;

  static final String DECREASE_POST_COMMENT_COUNT = """
      UPDATE posts
      SET comment_count = CASE WHEN comment_count > 0 THEN comment_count - 1 ELSE 0 END
      WHERE id = ?
      """;
}
