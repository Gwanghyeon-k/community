package community.backend.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileImageRequest {
  @NotBlank(message = "프로필 사진을 추가해주세요.")
  private String profileImageUrl;
}
