package community.backend.domain.user.dto.response;

import community.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResult {

  private Long userId;
  private String email;
  private String nickname;
  private String accessToken;

  public static LoginResult of(User user, String accessToken) {
    return new LoginResult(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        accessToken
    );
  }
}
