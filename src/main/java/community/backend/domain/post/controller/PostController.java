package community.backend.domain.post.controller;

import community.backend.domain.post.dto.request.CreatePostRequest;
import community.backend.domain.post.dto.request.UpdatePostRequest;
import community.backend.domain.post.dto.response.CreatePostResponse;
import community.backend.domain.post.dto.response.PostDetailResponse;
import community.backend.domain.post.dto.response.PostListResponse;
import community.backend.domain.post.service.PostService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import community.backend.global.jwt.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
  private final PostService postService;

  @PostMapping
  public ResponseEntity<ApiResponse<CreatePostResponse>> create(
      @Valid @RequestBody CreatePostRequest request,
      HttpServletRequest httpServletRequest
  ) {
    Long userId = AuthenticatedUser.getUserId(httpServletRequest);
    return ApiResponse.onSuccess(SuccessCode.CREATED, postService.create(userId, request));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PostListResponse>> list(@RequestParam(defaultValue = "0") Long lastPostId,
      @RequestParam(defaultValue = "10") int size) {
    Long cursor = lastPostId == 0 ? Long.MAX_VALUE : lastPostId;
    return ApiResponse.onSuccess(SuccessCode.OK, postService.list(cursor, size));
  }

  @GetMapping("/{postId}")
  public ResponseEntity<ApiResponse<PostDetailResponse>> detail(@PathVariable Long postId) {
    return ApiResponse.onSuccess(SuccessCode.OK, postService.detail(postId));
  }

  @PatchMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> update(
      @PathVariable Long postId,
      @Valid @RequestBody UpdatePostRequest req,
      HttpServletRequest httpServletRequest
  ) {
    Long userId = AuthenticatedUser.getUserId(httpServletRequest);
    postService.update(userId, postId, req);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable Long postId,
      HttpServletRequest httpServletRequest
  ) {
    Long userId = AuthenticatedUser.getUserId(httpServletRequest);
    postService.delete(userId, postId);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }
}