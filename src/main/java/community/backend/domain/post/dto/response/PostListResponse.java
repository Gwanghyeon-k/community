package community.backend.domain.post.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostListResponse {
  private List<PostListDetailResponse> posts;
  private boolean isLast;
}
