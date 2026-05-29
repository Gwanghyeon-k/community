package community.backend.domain.comment.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Comment {
  private Long id;
  private Long postId;
  private Long userId;
  private String content;
}

