package community.backend.global.auth.repository;

final class AuthSql {
  private AuthSql() {}

  static final String UPSERT_REFRESH_TOKEN = """
      INSERT INTO auths(user_id, token, expires_at, created_at, updated_at)
      VALUES (?, ?, ?, NOW(), NOW())
      ON DUPLICATE KEY UPDATE
        token = VALUES(token),
        expires_at = VALUES(expires_at),
        updated_at = NOW()
      """;

  static final String FIND_BY_REFRESH_TOKEN = """
      SELECT user_id, token, expires_at
      FROM auths
      WHERE token = ?
      """;

  static final String DELETE_BY_USER_ID = """
      DELETE FROM auths
      WHERE user_id = ?
      """;

  static final String DELETE_BY_REFRESH_TOKEN = """
      DELETE FROM auths
      WHERE token = ?
      """;
}
