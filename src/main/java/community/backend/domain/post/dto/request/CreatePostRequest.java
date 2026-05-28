package community.backend.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePostRequest {

  @NotBlank(message = "제목을 입력해주세요.")
  @Size(max = 26, message = "제목은 최대 26자까지 입력 가능합니다.")
  private String title;

  @NotBlank(message = "내용을 입력해주세요.")
  private String description;

  private String postImageUrl;
}
