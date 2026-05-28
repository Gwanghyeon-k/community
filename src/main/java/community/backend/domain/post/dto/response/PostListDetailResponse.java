package community.backend.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostListDetailResponse {
  private Long postId;
  private String title;
  private String nickname;
  private String profileImage;
  private String updatedAt;
  private Long likesCount;
  private String displayLikes;
  private Long commentCount;
  private Long viewCount;
  private String displayViewCount;

}
