package community.backend.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfo {

  private String accessToken;
  private long expiresIn;
}
