package community.backend.domain.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentListItemResponse {
  private Long commentId;
  private String nickname;
  private String profileImage;
  private String content;
  private String updatedAt;
}

