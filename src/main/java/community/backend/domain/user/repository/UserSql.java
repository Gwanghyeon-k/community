package community.backend.domain.user.repository;

final class UserSql {

  private UserSql() {
  }

  static final String FIND_BY_EMAIL = """
      SELECT id, email, password, nickname, profile_image_url, created_at, updated_at
      FROM users
      WHERE email = ?
      """;

  static final String FIND_BY_ID = """
      SELECT id, email, password, nickname, profile_image_url, created_at, updated_at
      FROM users
      WHERE id = ?
      """;

  static final String EXISTS_BY_EMAIL = """
      SELECT EXISTS(
        SELECT 1
        FROM users
        WHERE email = ?
      )
      """;

  static final String EXISTS_BY_NICKNAME = """
      SELECT EXISTS(
        SELECT 1
        FROM users
        WHERE nickname = ?
      )
      """;

  static final String UPDATE_NICKNAME = """
      UPDATE users
      SET nickname = ?, updated_at = NOW()
      WHERE id = ?
      """;

  static final String UPDATE_PASSWORD = """
      UPDATE users
      SET password = ?, updated_at = NOW()
      WHERE id = ?
      """;

  static final String UPDATE_PROFILE_IMAGE = """
      UPDATE users
      SET profile_image_url = ?, updated_at = NOW()
      WHERE id = ?
      """;

  static final String SAVE_USER = """
      INSERT INTO users(email, password, nickname, profile_image_url, created_at, updated_at)
      VALUES (?, ?, ?, ?, NOW(), NOW())
      """;
}
