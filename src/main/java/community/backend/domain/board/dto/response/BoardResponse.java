package community.backend.domain.board.dto.response;

import community.backend.domain.board.entity.Board;
import community.backend.domain.board.entity.BoardCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardResponse {

  private Long boardId;
  private BoardCategory category;
  private String name;
  private String description;

  public static BoardResponse from(Board board) {
    return new BoardResponse(
        board.getId(),
        board.getCategory(),
        board.getName(),
        board.getDescription()
    );
  }
}
