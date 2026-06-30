package community.backend.domain.board.controller;

import community.backend.domain.board.dto.response.BoardResponse;
import community.backend.domain.board.service.BoardService;
import community.backend.domain.post.dto.response.PostListResponse;
import community.backend.domain.post.service.PostService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {

  private final BoardService boardService;
  private final PostService postService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<BoardResponse>>> list() {
    return ApiResponse.onSuccess(SuccessCode.OK, boardService.list());
  }

  @GetMapping("/{boardCategory}/posts")
  public ResponseEntity<ApiResponse<PostListResponse>> listPosts(
      @PathVariable String boardCategory,
      @RequestParam(defaultValue = "0") Long lastPostId,
      @RequestParam(defaultValue = "10") int size
  ) {
    return ApiResponse.onSuccess(SuccessCode.OK, postService.listByBoard(boardCategory, lastPostId, size));
  }
}
