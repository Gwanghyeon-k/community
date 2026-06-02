package community.backend.domain.auth.repository;

import static community.backend.domain.auth.repository.AuthSql.DELETE_BY_USER_ID;
import static community.backend.domain.auth.repository.AuthSql.UPSERT_REFRESH_TOKEN;

import community.backend.domain.auth.entity.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthJdbcRepository implements AuthRepository {

  private final JdbcTemplate jdbcTemplate;

  private static final RowMapper<Auth> ROW_MAPPER = (rs, rowNum) ->
      new Auth(
          rs.getLong("user_id"),
          rs.getString("token"),
          rs.getTimestamp("expires_at").toLocalDateTime()
      );

  @Override
  public void upsertRefreshToken(Long userId, String token, java.time.LocalDateTime expiresAt) {
    jdbcTemplate.update(UPSERT_REFRESH_TOKEN, userId, token, expiresAt);
  }

  @Override
  public int deleteByUserId(Long userId) {
    return jdbcTemplate.update(DELETE_BY_USER_ID, userId);
  }

}
