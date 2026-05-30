package community.backend.global.jwt.repository;

import community.backend.global.jwt.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository {

  void saveOrUpdate(Long userId, String token, LocalDateTime expiresAt);

  Optional<RefreshToken> findByToken(String token);

  int deleteByUserId(Long userId);
}
