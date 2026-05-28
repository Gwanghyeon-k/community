package community.backend.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostDetailResponse {
  private Long postId;
  private String title;
  private String description;
  private String postImageUrl;
  private Author author;
  private String updatedAt;
  private Long likesCount;
  private Long viewCount;

  @Getter
  @AllArgsConstructor
  public static class Author {
    private String nickname;
    private String profileImage;
  }
}
