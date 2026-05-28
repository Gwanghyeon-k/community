package community.backend.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePostResponse {

  private Long postId;
  private String title;
}
