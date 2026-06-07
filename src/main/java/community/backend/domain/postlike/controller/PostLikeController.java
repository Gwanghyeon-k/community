package community.backend.domain.postlike.controller;

import community.backend.domain.auth.service.UserContextService;
import community.backend.domain.postlike.service.PostLikeService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/likes")
public class PostLikeController {

  private final PostLikeService postLikeService;
  private final UserContextService userContextService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> like(@PathVariable Long postId) {
    Long userId = userContextService.getUserId();
    postLikeService.like(postId, userId);
    return ApiResponse.onSuccess(SuccessCode.CREATED);
  }

  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> unlike(@PathVariable Long postId) {
    Long userId = userContextService.getUserId();
    postLikeService.unlike(postId, userId);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }
}
