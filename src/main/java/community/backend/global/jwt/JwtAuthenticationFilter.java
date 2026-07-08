package community.backend.global.jwt;

import community.backend.global.jwt.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;

  private static final String[] WHITE_LIST = {
      "/users",
      "/images/**",
      "/image/**",
      "/swagger-ui/**",
      "/swagger-ui.html",
      "/v3/api-docs",
      "/v3/api-docs/**",
      "/actuator/**",
  };

  private static final String[] GET_WHITE_LIST = {
      "/posts",
      "/posts/*",
      "/posts/*/comments",
  };

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    return HttpMethod.OPTIONS.matches(request.getMethod())
        || isAuthRequest(request)
        || isReadOnlyPublicRequest(request)
        || PatternMatchUtils.simpleMatch(WHITE_LIST, request.getRequestURI());
  }

  private boolean isReadOnlyPublicRequest(HttpServletRequest request) {
    return HttpMethod.GET.matches(request.getMethod())
        && PatternMatchUtils.simpleMatch(GET_WHITE_LIST, request.getRequestURI());
  }

  private boolean isAuthRequest(HttpServletRequest request) {
    return (HttpMethod.POST.matches(request.getMethod()) || HttpMethod.DELETE.matches(request.getMethod()))
        && "/auths".equals(request.getRequestURI());
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {

    String token = resolveAccessToken(request);

    // 토큰이 없거나 형식이 틀리면 401
    if (token == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    try {
      // 토큰 서명 + 만료 검증
      jwtProvider.parse(token);

      // access 토큰인지 확인
      if (!jwtProvider.isAccessToken(token)) {
        throw new IllegalArgumentException("Not access token");
      }

      // 여기서는 인증 정보 전달 없이 통과만 시킴
      Long userId = jwtProvider.getUserId(token);
      request.setAttribute("userId", userId);
      filterChain.doFilter(request, response);

    } catch (Exception exception) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private String resolveAccessToken(HttpServletRequest request) {
    return CookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE_NAME);
  }
}
