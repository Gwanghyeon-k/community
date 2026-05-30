package community.backend.global.auth.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Auth {
  private Long userId;
  private String token;
  private LocalDateTime expiresAt;
}
