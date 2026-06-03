package community.backend.global.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseStatus {

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러입니다. 관리자에게 문의하세요."),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
  AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "금지된 요청입니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "찾을 수 없는 요청입니다."),

  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401", "이메일 또는 비밀번호가 올바르지 않습니다."),

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_409_1", "중복된 이메일입니다."),
  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_409_2", "중복된 닉네임입니다."),
  USER_PROFILE_UPDATE_EMPTY(HttpStatus.BAD_REQUEST, "USER_400", "수정할 사용자 정보가 없습니다."),
  USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "USER_403", "다른 사용자 정보는 수정할 수 없습니다."),

  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404", "게시글을 찾을 수 없습니다."),
  POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "POST_403", "해당 게시글에 대한 권한이 없습니다."),

  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "댓글을 찾을 수 없습니다."),
  COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMENT_403", "해당 댓글에 대한 권한이 없습니다."),
  INVALID_COMMENT_PAGE_SIZE(HttpStatus.BAD_REQUEST, "COMMENT_400", "댓글 조회 size는 1 이상이어야 합니다."),

  POST_LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "POST_LIKE_409", "이미 좋아요를 누른 게시글입니다."),
  POST_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_LIKE_404", "좋아요 정보를 찾을 수 없습니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
