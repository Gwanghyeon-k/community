package community.backend.domain.user.controller;

import community.backend.domain.auth.service.UserContextService;
import community.backend.domain.user.dto.request.SignUpRequest;
import community.backend.domain.user.dto.request.UpdateUserRequest;
import community.backend.domain.user.dto.response.UserProfileResponse;
import community.backend.domain.user.service.UserService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
  private final UserService userService;
  private final UserContextService userContextService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
    userService.signUp(request);
    return ApiResponse.onSuccess(SuccessCode.CREATED);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserProfileResponse>> getUser(@PathVariable Long userId) {
    UserProfileResponse response = userService.getUserProfile(userId);
    return ApiResponse.onSuccess(SuccessCode.OK, response);
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<ApiResponse<Void>> updateUser(
      @PathVariable Long userId,
      @Valid @RequestBody UpdateUserRequest request
  ) {
    Long authenticatedUserId = userContextService.getUserId();
    userService.updateUser(authenticatedUserId, userId, request);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }

}
