package community.backend.global.apiPayload.exception;

import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
    ErrorCode errorCode = exception.getErrorCode();
    return ApiResponse.toResponseEntity(errorCode, null);
  }
}
