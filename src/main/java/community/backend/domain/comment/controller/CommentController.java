package community.backend.domain.comment.controller;

import community.backend.domain.comment.dto.request.CreateCommentRequest;
import community.backend.domain.comment.dto.request.UpdateCommentRequest;
import community.backend.domain.comment.dto.response.CommentListItemResponse;
import community.backend.domain.comment.service.CommentService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommentController {

  private final CommentService commentService;

  @GetMapping("/posts/{postId}/comments")
  public ResponseEntity<ApiResponse<List<CommentListItemResponse>>> list(@PathVariable Long postId) {
    return ApiResponse.onSuccess(SuccessCode.OK, commentService.listByPost(postId));
  }

  @PostMapping("/posts/{postId}/comments")
  public ResponseEntity<ApiResponse<Void>> create(@PathVariable Long postId, @Valid @RequestBody CreateCommentRequest request) {
    Long userId = 1L;
    commentService.create(postId, userId, request);
    return ApiResponse.onSuccess(SuccessCode.CREATED);
  }

  @PatchMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long commentId, @Valid @RequestBody UpdateCommentRequest request) {
    Long userId = 1L;
    commentService.update(commentId, userId, request);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }

  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long commentId) {
    Long userId = 1L;
    commentService.delete(commentId, userId);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }
}

