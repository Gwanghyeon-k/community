package community.backend.domain.user.service;

import community.backend.domain.user.dto.request.LoginRequest;
import community.backend.domain.user.dto.response.LoginResponse;
import community.backend.domain.user.dto.response.LoginResult;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import community.backend.global.auth.JwtProperties;
import community.backend.global.auth.JwtProvider;
import community.backend.global.auth.repository.AuthRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;
  private final AuthRepository authRepository;

  public LoginResult login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

    if (!request.getPassword().equals(user.getPassword())) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    String accessToken = jwtProvider.createAccessToken(
        user.getId(), user.getEmail(), user.getNickname()
    );
    String refreshToken = jwtProvider.createRefreshToken(user.getId());

    authRepository.upsertRefreshToken(
        user.getId(),
        refreshToken,
        LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpSeconds())
    );

    LoginResponse response = LoginResponse.of(
        user,
        accessToken,
        jwtProvider.getAccessTokenValidityInMilliseconds()
    );

    return new LoginResult(response, refreshToken);
  }

  public void logout(Long userId) {
    authRepository.deleteByUserId(userId);
  }
}
