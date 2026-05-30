package community.backend.global.jwt.repository;

import community.backend.global.jwt.entity.RefreshToken;
import static community.backend.global.jwt.repository.RefreshTokenSql.DELETE_BY_USER_ID;
import static community.backend.global.jwt.repository.RefreshTokenSql.FIND_BY_TOKEN;
import static community.backend.global.jwt.repository.RefreshTokenSql.SAVE_OR_UPDATE;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenJdbcRepository implements RefreshTokenRepository {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void saveOrUpdate(Long userId, String token, LocalDateTime expiresAt) {
    jdbcTemplate.update(
        SAVE_OR_UPDATE,
        userId,
        token,
        Timestamp.valueOf(expiresAt)
    );
  }

  @Override
  public Optional<RefreshToken> findByToken(String token) {
    List<RefreshToken> rows = jdbcTemplate.query(FIND_BY_TOKEN, (rs, rowNum) -> RefreshToken.builder()
        .id(rs.getLong("id"))
        .userId(rs.getLong("user_id"))
        .token(rs.getString("token"))
        .expiresAt(rs.getTimestamp("expires_at").toLocalDateTime())
        .build(), token);
    return rows.stream().findFirst();
  }

  @Override
  public int deleteByUserId(Long userId) {
    return jdbcTemplate.update(DELETE_BY_USER_ID, userId);
  }
}
