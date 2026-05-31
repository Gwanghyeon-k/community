package community.backend.domain.user.dto.response;

import community.backend.domain.auth.dto.TokenInfo;
import community.backend.domain.user.entity.User;
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
