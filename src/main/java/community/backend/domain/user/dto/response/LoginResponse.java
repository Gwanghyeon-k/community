package community.backend.domain.user.dto.response;

import community.backend.domain.user.entity.User;
import community.backend.global.jwt.dto.TokenInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
  private UserInfo user;
  private TokenInfo token;

  public static LoginResponse of(
      User user,
      String accessToken,
      long expiresIn
  ) {
    return new LoginResponse(
        new UserInfo(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl()
        ),
        new TokenInfo(accessToken, expiresIn)
    );
  }

  @Getter
  @AllArgsConstructor
  public static class UserInfo {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
  }
}
