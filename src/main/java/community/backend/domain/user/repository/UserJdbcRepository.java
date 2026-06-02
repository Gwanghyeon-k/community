package community.backend.domain.user.repository;

import community.backend.domain.user.entity.User;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import static community.backend.domain.user.repository.UserSql.EXISTS_BY_EMAIL;
import static community.backend.domain.user.repository.UserSql.EXISTS_BY_NICKNAME;
import static community.backend.domain.user.repository.UserSql.FIND_BY_EMAIL;
import static community.backend.domain.user.repository.UserSql.FIND_BY_ID;
import static community.backend.domain.user.repository.UserSql.SAVE_USER;
import static community.backend.domain.user.repository.UserSql.UPDATE_NICKNAME;
import static community.backend.domain.user.repository.UserSql.UPDATE_PASSWORD;
import static community.backend.domain.user.repository.UserSql.UPDATE_PROFILE_IMAGE;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
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
    List<User> rows = jdbcTemplate.query(FIND_BY_EMAIL, USER_ROW_MAPPER, email);
    return rows.stream().findFirst();
  }

  @Override
  public Optional<User> findById(Long userId) {
    List<User> rows = jdbcTemplate.query(FIND_BY_ID, USER_ROW_MAPPER, userId);
    return rows.stream().findFirst();
  }

  @Override
  public boolean existsByEmail(String email) {
    Boolean exists = jdbcTemplate.queryForObject(
        EXISTS_BY_EMAIL,
        Boolean.class,
        email
    );
    return Boolean.TRUE.equals(exists);
  }

  @Override
  public boolean existsByNickname(String nickname) {
    Boolean exists = jdbcTemplate.queryForObject(
        EXISTS_BY_NICKNAME,
        Boolean.class,
        nickname
    );
    return Boolean.TRUE.equals(exists);
  }

  @Override
  public int updateNickname(Long userId, String nickname) {
    return jdbcTemplate.update(
        UPDATE_NICKNAME,
        nickname, userId
    );
  }

  @Override
  public int updatePassword(Long userId, String encodedPassword) {
    return jdbcTemplate.update(
        UPDATE_PASSWORD,
        encodedPassword, userId
    );
  }

  @Override
  public int updateProfileImage(Long userId, String profileImageUrl) {
    return jdbcTemplate.update(
        UPDATE_PROFILE_IMAGE,
        profileImageUrl, userId
    );
  }

  @Override
  public long save(User user) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
      PreparedStatement preparedStatement = connection.prepareStatement(SAVE_USER, Statement.RETURN_GENERATED_KEYS);
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
