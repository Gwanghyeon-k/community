package community.backend.global.auth.repository;

import community.backend.global.auth.entity.Auth;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthRepository {
  void upsertRefreshToken(Long userId, String token, LocalDateTime expiresAt);
  Optional<Auth> findByRefreshToken(String token);
  int deleteByUserId(Long userId);
  int deleteByRefreshToken(String token);
}