package community.backend.global.apiPayload;

import community.backend.global.apiPayload.code.BaseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {

  private final int httpStatus;
  private final String code;
  private final String message;
  private final T result;

  public static <T> ApiResponse<T> of(BaseStatus status, T result) {
    return new ApiResponse<>(
        status.getStatus().value(),
        status.getCode(),
        status.getMessage(),
        result
    );
  }

  public static <T> ResponseEntity<ApiResponse<T>> toResponseEntity(BaseStatus status, T result) {
    return ResponseEntity.status(status.getStatus())
        .body(of(status, result));
  }
}
