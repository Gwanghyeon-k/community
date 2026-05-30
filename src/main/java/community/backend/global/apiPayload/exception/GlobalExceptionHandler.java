package community.backend.global.apiPayload.exception;

import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
   * @Valid 바디 검증 실패 처리.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
    List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
    if (fieldErrors.isEmpty()) {
      return ApiResponse.onFailure(ErrorCode.BAD_REQUEST);
    }

    fieldErrors.sort(Comparator.comparing(FieldError::getField));
    return ApiResponse.onFailure(ErrorCode.BAD_REQUEST, fieldErrors.getFirst().getDefaultMessage());
  }

  /**
   * @Validated 파라미터/경로 변수 검증 실패 처리.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
    String message = exception.getConstraintViolations().stream()
        .map(violation -> violation.getMessage())
        .sorted()
        .findFirst()
        .orElse(ErrorCode.BAD_REQUEST.getMessage());
    return ApiResponse.onFailure(ErrorCode.BAD_REQUEST, message);
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
