package community.backend.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;

  public static final String USER_ID_ATTRIBUTE = "authenticatedUserId";
  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
  private static final String[] WHITE_LIST = {
      "/users",
      "/users/login",
      "/users/token/refresh"
  };

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String uri = normalizePath(request.getRequestURI());
    String method = request.getMethod();

    if (HttpMethod.OPTIONS.matches(method)) {
      return true;
    }
    if ("/error".equals(uri) || PATH_MATCHER.match("/swagger-ui/**", uri) || PATH_MATCHER.match("/v3/api-docs/**", uri)) {
      return true;
    }
    if ("POST".equals(method) && PatternMatchUtils.simpleMatch(WHITE_LIST, uri)) {
      return true;
    }
    if ("GET".equals(method) && (PATH_MATCHER.match("/posts/**", uri) || PATH_MATCHER.match("/comments/**", uri))) {
      return true;
    }
    return false;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    // 토큰이 없거나 형식이 틀리면 401
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = authHeader.substring(7);

    try {
      jwtProvider.parse(token);
      if (!jwtProvider.isAccessToken(token)) {
        throw new IllegalArgumentException("Not access token");
      }
      request.setAttribute(USER_ID_ATTRIBUTE, jwtProvider.getUserId(token));
      filterChain.doFilter(request, response);

    } catch (Exception exception) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private String normalizePath(String path) {
    if (path != null && path.length() > 1 && path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    }
    return path;
  }
}
