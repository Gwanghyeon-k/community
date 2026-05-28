package community.backend.global.apiPayload.code;

import org.springframework.http.HttpStatus;

/***
 * 성공/실패를 공통 인터페이스로 관리
 */
public interface BaseStatus {

  HttpStatus getStatus();
  String getCode();
  String getMessage();
}
