package community.backend.domain.post.dto.request;

import community.backend.domain.board.entity.BoardCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

  @NotNull(message = "게시판을 선택해주세요.")
  private BoardCategory boardCategory;

  @NotBlank(message = "제목을 입력해주세요.")
  @Size(max = 26, message = "제목은 최대 26자까지 입력 가능합니다.")
  private String title;

  @NotBlank(message = "내용을 입력해주세요.")
  private String description;

  private String postImageUrl;
}
