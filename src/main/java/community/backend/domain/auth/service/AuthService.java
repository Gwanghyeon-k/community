package community.backend.domain.auth.service;

import community.backend.domain.user.dto.request.LoginRequest;
import community.backend.domain.user.dto.response.LoginResult;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import community.backend.global.jwt.JwtProperties;
import community.backend.global.jwt.JwtProvider;
import community.backend.global.jwt.util.CookieUtil;
import community.backend.global.redis.RefreshTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final JwtProperties jwtProperties;
  private final RefreshTokenService refreshTokenService;
  private final Environment environment;

  @Transactional
  public LoginResult login(LoginRequest request, HttpServletResponse servletResponse) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

    if (!request.getPassword().equals(user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken = jwtProvider.createAccessToken(
        user.getId(), user.getEmail(), user.getNickname()
    );
    String refreshToken = jwtProvider.createRefreshToken(user.getId());

    refreshTokenService.save(
        user.getId(),
        refreshToken,
        Duration.ofSeconds(jwtProperties.getRefreshTokenExpSeconds())
    );

    addAuthCookies(servletResponse, accessToken, refreshToken);
    return LoginResult.of(user);
  }

  @Transactional
  public void logout(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    String refreshToken = CookieUtil.getCookieValue(servletRequest, CookieUtil.REFRESH_TOKEN_COOKIE_NAME);
    if (refreshToken != null && !refreshToken.isBlank()) {
      deleteStoredRefreshToken(refreshToken);
    }
    deleteAuthCookies(servletResponse);
  }

  private void deleteStoredRefreshToken(String refreshToken) {
    try {
      if (!jwtProvider.isRefreshToken(refreshToken)) {
        return;
      }

      Long userId = jwtProvider.getUserId(refreshToken);
      refreshTokenService.findByUserId(userId)
          .filter(refreshToken::equals)
          .ifPresent(storedRefreshToken -> refreshTokenService.deleteByUserId(userId));
    } catch (JwtException | IllegalArgumentException ignored) {
    }
  }

  private void addAuthCookies(
      HttpServletResponse servletResponse,
      String accessToken,
      String refreshToken
  ) {
    boolean isLocal = isLocalProfile();
    CookieUtil.addCookie(
        servletResponse,
        CookieUtil.ACCESS_TOKEN_COOKIE_NAME,
        accessToken,
        CookieUtil.toCookieMaxAge(jwtProperties.getAccessTokenExpSeconds()),
        isLocal
    );
    CookieUtil.addCookie(
        servletResponse,
        CookieUtil.REFRESH_TOKEN_COOKIE_NAME,
        refreshToken,
        CookieUtil.toCookieMaxAge(jwtProperties.getRefreshTokenExpSeconds()),
        isLocal
    );
  }

  private void deleteAuthCookies(HttpServletResponse servletResponse) {
    boolean isLocal = isLocalProfile();
    CookieUtil.deleteCookie(servletResponse, CookieUtil.ACCESS_TOKEN_COOKIE_NAME, isLocal);
    CookieUtil.deleteCookie(servletResponse, CookieUtil.REFRESH_TOKEN_COOKIE_NAME, isLocal);
  }

  private boolean isLocalProfile() {
    return environment.acceptsProfiles(Profiles.of("local", "default"));
  }
}
