package community.backend.global.redis;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh:";

  private final StringRedisTemplate redisTemplate;

  public void save(Long userId, String refreshToken, Duration ttl) {
    String key = key(userId);
    redisTemplate.opsForValue().set(key, refreshToken, ttl);
  }

  public Optional<String> findByUserId(Long userId) {
    String key = key(userId);
    Optional<String> refreshToken = Optional.ofNullable(redisTemplate.opsForValue().get(key));
    return refreshToken;
  }

  public void deleteByUserId(Long userId) {
    String key = key(userId);
    redisTemplate.delete(key);
  }

  private String key(Long userId) {
    return REFRESH_TOKEN_KEY_PREFIX + userId;
  }
}
