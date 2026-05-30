package community.backend.domain.user.service;

import community.backend.domain.user.dto.request.LoginRequest;
import community.backend.domain.user.dto.request.RefreshTokenRequest;
import community.backend.domain.user.dto.response.LoginResult;
import community.backend.domain.user.dto.response.LoginResponse;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import community.backend.global.jwt.JwtProvider;
import community.backend.global.jwt.dto.TokenInfo;
import community.backend.global.jwt.dto.TokenResult;
import community.backend.global.jwt.entity.RefreshToken;
import community.backend.global.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public LoginResult login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());
    String refreshToken = jwtProvider.createRefreshToken(user.getId());
    refreshTokenRepository.saveOrUpdate(user.getId(), refreshToken, jwtProvider.getExpiration(refreshToken));

    LoginResponse response = LoginResponse.of(
        user,
        accessToken,
        jwtProvider.getAccessTokenValidityInMilliseconds()
    );
    return new LoginResult(response, refreshToken);
  }

  @Transactional
  public TokenResult refresh(RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();

    try {
      jwtProvider.parse(refreshToken);
      if (!jwtProvider.isRefreshToken(refreshToken)) {
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
      }
    } catch (BusinessException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    if (savedToken.isExpired()) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    User user = userRepository.findById(savedToken.getUserId())
        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

    String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());
    String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
    refreshTokenRepository.saveOrUpdate(user.getId(), newRefreshToken, jwtProvider.getExpiration(newRefreshToken));

    return new TokenResult(
        new TokenInfo(
            newAccessToken,
            jwtProvider.getAccessTokenValidityInMilliseconds()
        ),
        newRefreshToken
    );
  }
}
