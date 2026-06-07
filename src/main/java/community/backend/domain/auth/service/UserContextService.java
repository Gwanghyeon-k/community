package community.backend.domain.auth.service;

import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class UserContextService {

  private static final String USER_ID = "userId";

  public Long getUserId() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }

    Object userId = servletAttributes.getRequest().getAttribute(USER_ID);
    if (userId instanceof Long value) {
      return value;
    }
    if (userId instanceof Number value) {
      return value.longValue();
    }

    throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
  }
}
