package community.backend.global.apiPayload;

import community.backend.global.apiPayload.code.BaseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {

  // 실제 HTTP 응답 코드
  private final int httpStatus;
  // 서비스 공통 응답 비즈니스 코드
  private final String code;

  private final String message;

  private final T result;

  /**
   * 공통 응답 body를 생성하는 내부 전용 메서드.
   * private으로 설정하여 외부에서는 onSuccess/onFailure 로만 응답을 만들도록 제한
   */
  private static <T> ApiResponse<T> of(BaseStatus status, T result) {
    return new ApiResponse<>(status.getStatus().value(), status.getCode(), status.getMessage(), result);
  }

  public static <T> ResponseEntity<ApiResponse<T>> onSuccess(BaseStatus status, T result) {
    return ResponseEntity.status(status.getStatus()).body(of(status, result));
  }

  public static ResponseEntity<ApiResponse<Void>> onSuccess(BaseStatus status) {
    return ResponseEntity.status(status.getStatus()).body(of(status, null));
  }


  public static ResponseEntity<ApiResponse<Void>> onFailure(BaseStatus status) {
    return ResponseEntity.status(status.getStatus()).body(of(status, null));
  }


  public static ResponseEntity<ApiResponse<Void>> onFailure(BaseStatus status, String message) {
    ApiResponse<Void> response = new ApiResponse<>(status.getStatus().value(), status.getCode(), message, null);
    return ResponseEntity.status(status.getStatus()).body(response);
  }
}
