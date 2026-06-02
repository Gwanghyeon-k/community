package community.backend.domain.auth.controller;

import community.backend.domain.auth.service.UserContextService;
import community.backend.domain.user.dto.request.LoginRequest;
import community.backend.domain.user.dto.response.LoginResult;
import community.backend.domain.auth.service.AuthService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auths")
public class AuthController {

  private final AuthService authService;
  private final UserContextService userContextService;

  @PostMapping
  public ResponseEntity<ApiResponse<LoginResult>> login(@Valid @RequestBody LoginRequest request) {
    LoginResult response = authService.login(request);
    return ApiResponse.onSuccess(SuccessCode.OK, response);
  }

  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> logout() {
    Long userId = userContextService.getUserId();
    authService.logout(userId);
    return ApiResponse.onSuccess(SuccessCode.OK);
  }
}
