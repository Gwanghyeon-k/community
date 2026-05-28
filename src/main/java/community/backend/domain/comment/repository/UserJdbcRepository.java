package community.backend.domain.comment.repository;

import community.backend.domain.comment.entity.User;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserJdbcRepository implements UserRepository {
  private final JdbcTemplate jdbcTemplate;

  private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> User.builder()
      .id(rs.getLong("id"))
      .email(rs.getString("email"))
      .password(rs.getString("password"))
      .nickname(rs.getString("nickname"))
      .profileImageUrl(rs.getString("profile_image_url"))
      .build();

  @Override
  public Optional<User> findByEmail(String email) {
    String sql = """
      SELECT id, email, password, nickname, profile_image_url, created_at, updated_at, deleted_at
      FROM users
      WHERE email = ? AND deleted_at IS NULL
      """;
    List<User> rows = jdbcTemplate.query(sql, USER_ROW_MAPPER, email);
    return rows.stream().findFirst();
  }

  @Override
  public boolean existsByEmail(String email) {
    Boolean exists = jdbcTemplate.queryForObject(
        """
            SELECT EXISTS(
              SELECT 1
              FROM users
              WHERE email = ?
                AND deleted_at IS NULL)
                """,
        Boolean.class,
        email
    );
    return Boolean.TRUE.equals(exists);
  }

  @Override
  public boolean existsByNickname(String nickname) {
    Boolean exists = jdbcTemplate.queryForObject(
        """
        SELECT EXISTS(
        SELECT 1
        FROM users
        WHERE nickname = ?
          AND deleted_at is NULL
        )
        """,
        Boolean.class,
        nickname
    );
    return Boolean.TRUE.equals(exists);
  }

  @Override
  public long save(User user) {
    String sql = """
        INSERT INTO users(email, password, nickname, profile_image_url, created_at, updated_at)
        VALUES (?, ?, ?, ?, NOW(), NOW())
        """;

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, user.getEmail());
      preparedStatement.setString(2, user.getPassword());
      preparedStatement.setString(3, user.getNickname());
      preparedStatement.setString(4, user.getProfileImageUrl());
      return preparedStatement;
    }, keyHolder);

    Number key = keyHolder.getKey();

    if(key==null) {
      throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    return key.longValue();
  }
}
