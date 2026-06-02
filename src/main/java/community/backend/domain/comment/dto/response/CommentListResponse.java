package community.backend.domain.comment.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentListResponse {
  private List<CommentListItemResponse> comments;
  private boolean isLast;
  private Long nextCommentId;
}
