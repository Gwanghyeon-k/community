package community.backend.global.jwt;

import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;

public final class AuthenticatedUser {

  private AuthenticatedUser() {
  }

  public static Long getUserId(HttpServletRequest request) {
    Object userId = request.getAttribute(JwtAuthenticationFilter.USER_ID_ATTRIBUTE);
    if (!(userId instanceof Long)) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    return (Long) userId;
  }
}
