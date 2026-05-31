package community.backend.domain.post.repository;

final class PostSql {

  private PostSql() {
  }

  static final String SAVE_POST = """
      INSERT INTO posts(user_id,title,description,post_image_url,view_count,like_count,comment_count,created_at,updated_at)
      VALUES (?,?,?,?,0,0,0,NOW(),NOW())
      """;

  static final String FIND_LIST = """
      SELECT p.id, p.title, u.nickname, u.profile_image_url, p.updated_at, p.like_count, p.comment_count, p.view_count
      FROM posts p JOIN users u ON p.user_id=u.id
      WHERE p.id < ?
      ORDER BY p.id DESC LIMIT ?
      """;

  static final String FIND_DETAIL = """
      SELECT p.id, p.title, p.description, p.post_image_url, p.updated_at, p.like_count, p.view_count,
             u.nickname, u.profile_image_url
      FROM posts p JOIN users u ON p.user_id=u.id
      WHERE p.id=?
      """;

  static final String FIND_POST_BY_ID = """
      SELECT id, user_id, title, description, post_image_url, view_count, like_count, comment_count
      FROM posts
      WHERE id = ?
      """;

  static final String INCREASE_VIEW_COUNT = """
      UPDATE posts
      SET view_count = view_count + 1
      WHERE id = ?
      """;

  static final String UPDATE_POST = """
      UPDATE posts
      SET title = ?, description = ?, post_image_url = ?, updated_at = NOW()
      WHERE id = ?
      """;

  static final String DELETE_POST = """
      DELETE FROM posts
      WHERE id = ?
      """;
}
