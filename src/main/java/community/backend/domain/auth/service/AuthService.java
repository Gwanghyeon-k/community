package community.backend.domain.auth.service;

import community.backend.global.jwt.JwtProperties;
import community.backend.global.jwt.JwtProvider;
import community.backend.domain.auth.entity.Auth;
import community.backend.domain.auth.repository.AuthRepository;
import community.backend.domain.user.dto.request.LoginRequest;
import community.backend.domain.user.dto.response.LoginResult;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;
  private final AuthRepository authRepository;

  @Transactional
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

    LocalDateTime refreshExpiresAt = LocalDateTime.now()
        .plusSeconds(jwtProperties.getRefreshTokenExpSeconds());
    Auth auth = authRepository.findByUserId(user.getId())
        .map(existing -> {
          existing.updateToken(refreshToken, refreshExpiresAt);
          return existing;
        })
        .orElseGet(() -> Auth.builder()
            .user(user)
            .token(refreshToken)
            .expiresAt(refreshExpiresAt)
            .build());
    authRepository.save(auth);

    return LoginResult.of(user, accessToken);
  }

  @Transactional
  public void logout(Long userId) {
    authRepository.deleteByUserId(userId);
  }
}
