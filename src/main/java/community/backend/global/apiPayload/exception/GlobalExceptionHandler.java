package community.backend.global.apiPayload.exception;

import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 비즈니스 예외 처리.
   * 서비스 계층에서 던진 BusinessException을 공통 실패 응답으로 변환.
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    return ApiResponse.onFailure(errorCode);
  }

  /**
   * 처리되지 않은 예외 처리.
   * 미처리된 예외는 COMMON_500 서버 에러 코드로 통일.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
    return ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR);
  }
}
