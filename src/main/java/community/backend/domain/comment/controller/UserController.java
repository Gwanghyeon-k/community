package community.backend.domain.comment.controller;

import community.backend.domain.comment.dto.request.LoginRequest;
import community.backend.domain.comment.dto.request.SignUpRequest;
import community.backend.domain.comment.dto.response.LoginResponse;
import community.backend.domain.comment.service.AuthService;
import community.backend.domain.comment.service.UserService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ApiResponse.onSuccess(SuccessCode.OK, response);
  }
}
