package community.backend.global.jwt.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

public final class CookieUtil {

  public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  private CookieUtil() {
  }

  public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean isLocal) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setSecure(!isLocal);  // 로컬에서는 false, 배포에서는 true
    cookie.setMaxAge(maxAge);
    if (!isLocal) {
      cookie.setAttribute("SameSite", "None");
    } else {
      cookie.setAttribute("SameSite", "Lax");
    }
    response.addCookie(cookie);
  }

  public static void deleteCookie(HttpServletResponse response, String name, boolean isLocal) {
    Cookie cookie = new Cookie(name, "");
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    cookie.setSecure(!isLocal);

    if (!isLocal) {
      cookie.setAttribute("SameSite", "None");
    } else {
      cookie.setAttribute("SameSite", "Lax");
    }

    response.addCookie(cookie);
  }

  public static String getCookieValue(HttpServletRequest request, String name) {
    return Optional.ofNullable(request.getCookies())
        .flatMap(cookies -> Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(name))
            .map(Cookie::getValue)
            .findFirst())
        .orElse(null);
  }

  public static int toCookieMaxAge(long seconds) {
    return Math.toIntExact(seconds);
  }
}
