package community.backend.global.jwt.repository;

final class RefreshTokenSql {

  private RefreshTokenSql() {
  }

  static final String SAVE_OR_UPDATE = """
      INSERT INTO refresh_tokens (user_id, token, expires_at, created_at, updated_at)
      VALUES (?, ?, ?, NOW(), NOW())
      ON DUPLICATE KEY UPDATE
        token = VALUES(token),
        expires_at = VALUES(expires_at),
        updated_at = NOW()
      """;

  static final String FIND_BY_TOKEN = """
      SELECT id, user_id, token, expires_at
      FROM refresh_tokens
      WHERE token = ?
      """;

  static final String DELETE_BY_USER_ID = """
      DELETE FROM refresh_tokens
      WHERE user_id = ?
      """;
}
