package community.backend.global.apiPayload.exception;

import community.backend.global.apiPayload.code.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  // 메시지는 ErrorCode의 메시지 사용.
  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
