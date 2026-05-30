package community.backend.domain.user.dto.response;

import community.backend.domain.user.entity.User;
import community.backend.global.auth.dto.TokenInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

  private User user;
  private TokenInfo token;

  public static LoginResponse of(
      User user,
      String accessToken,
      long expiresIn
  ) {
    return new LoginResponse(
        user,
        new TokenInfo(accessToken, expiresIn)
    );
  }
}
