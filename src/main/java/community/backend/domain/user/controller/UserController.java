package community.backend.domain.user.controller;

import community.backend.domain.user.dto.request.LoginRequest;
import community.backend.domain.user.dto.request.SignUpRequest;
import community.backend.domain.user.dto.request.UpdateNicknameRequest;
import community.backend.domain.user.dto.request.UpdatePasswordRequest;
import community.backend.domain.user.dto.request.UpdateProfileImageRequest;
import community.backend.domain.user.dto.response.LoginResponse;
import community.backend.domain.user.dto.response.LoginResult;
import community.backend.domain.user.service.AuthService;
import community.backend.domain.user.service.UserService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
  private final UserService userService;
  private final AuthService authService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignUpRequest request) {
    userService.signUp(request);
    return ApiResponse.onSuccess(SuccessCode.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResult>> login(@Valid @RequestBody LoginRequest request) {
    LoginResult response = authService.login(request);
    return ApiResponse.onSuccess(SuccessCode.OK, response);
  }

  @PatchMapping("/me/nickname")
  public ResponseEntity<ApiResponse<Void>> updateNickname(@Valid @RequestBody UpdateNicknameRequest request) {
    Long userId = 1L;
    userService.updateNickname(userId, request);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }

  @PatchMapping("/me/password")
  public ResponseEntity<ApiResponse<Void>> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
    Long userId = 1L;
    userService.updatePassword(userId, request);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }

  @PostMapping("/me/profile-image")
  public ResponseEntity<ApiResponse<Void>> updateProfileImage(@Valid @RequestBody UpdateProfileImageRequest request) {
    Long userId = 1L;
    userService.updateProfileImage(userId, request);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }

  @DeleteMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    authService.logout(userId);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }
}
