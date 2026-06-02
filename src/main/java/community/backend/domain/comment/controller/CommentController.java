package community.backend.domain.comment.controller;

import community.backend.domain.auth.service.UserContextService;
import community.backend.domain.comment.dto.request.CreateCommentRequest;
import community.backend.domain.comment.dto.request.UpdateCommentRequest;
import community.backend.domain.comment.dto.response.CommentListResponse;
import community.backend.domain.comment.service.CommentService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommentController {

  private final CommentService commentService;
  private final UserContextService userContextService;

  @GetMapping("/posts/{postId}/comments")
  public ResponseEntity<ApiResponse<CommentListResponse>> list(
      @PathVariable Long postId,
      @RequestParam(defaultValue = "0") Long lastCommentId,
      @RequestParam(defaultValue = "20") int size
  ) {
    return ApiResponse.onSuccess(SuccessCode.OK, commentService.listByPost(postId, lastCommentId, size));
  }

  @PostMapping("/posts/{postId}/comments")
  public ResponseEntity<ApiResponse<Void>> create(
      @PathVariable Long postId,
      @Valid @RequestBody CreateCommentRequest request
  ) {
    Long userId = userContextService.getUserId();
    commentService.create(postId, userId, request);
    return ApiResponse.onSuccess(SuccessCode.CREATED);
  }

  @PatchMapping("/posts/{postId}/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> update(
      @PathVariable Long postId,
      @PathVariable Long commentId,
      @Valid @RequestBody UpdateCommentRequest request
  ) {
    Long userId = userContextService.getUserId();
    commentService.update(postId, commentId, userId, request);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }

  @DeleteMapping("/posts/{postId}/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable Long postId,
      @PathVariable Long commentId
  ) {
    Long userId = userContextService.getUserId();
    commentService.delete(postId, commentId, userId);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }
}

