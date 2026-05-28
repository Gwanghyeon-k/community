package community.backend.domain.post.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Post {
  private Long id;
  private Long userId;
  private String title;
  private String description;
  private String postImageUrl;
  private Long viewCount;
  private Long likeCount;
  private Long commentCount;
}
