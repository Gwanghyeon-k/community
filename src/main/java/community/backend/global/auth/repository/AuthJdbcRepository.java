package community.backend.global.auth.repository;

import static community.backend.global.auth.repository.AuthSql.*;

import community.backend.global.auth.entity.Auth;
import java.util.List;
import java.util.Optional;
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
  public Optional<Auth> findByRefreshToken(String token) {
    List<Auth> rows = jdbcTemplate.query(FIND_BY_REFRESH_TOKEN, ROW_MAPPER, token);
    return rows.stream().findFirst();
  }

  @Override
  public int deleteByUserId(Long userId) {
    return jdbcTemplate.update(DELETE_BY_USER_ID, userId);
  }

  @Override
  public int deleteByRefreshToken(String token) {
    return jdbcTemplate.update(DELETE_BY_REFRESH_TOKEN, token);
  }
}
