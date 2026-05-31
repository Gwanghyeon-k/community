package community.backend.domain.user.dto.response;

import community.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
  private Long id;
  private String email;
  private String nickname;
  private String profileImageUrl;

  public static UserProfileResponse from(User user) {
    return new UserProfileResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProfileImageUrl()
    );
  }
}
