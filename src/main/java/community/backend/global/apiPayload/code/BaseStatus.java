package community.backend.global.apiPayload.code;

import org.springframework.http.HttpStatus;


public interface BaseStatus {

  // HTTP 레벨 상태값
  HttpStatus getStatus();
  // 비즈니스 에러/성공 코드
  String getCode();

  String getMessage();
}
