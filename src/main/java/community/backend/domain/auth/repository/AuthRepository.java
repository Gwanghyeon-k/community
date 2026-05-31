package community.backend.domain.auth.repository;

import community.backend.domain.auth.entity.Auth;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthRepository {
  void upsertRefreshToken(Long userId, String token, LocalDateTime expiresAt);
  int deleteByUserId(Long userId);
}