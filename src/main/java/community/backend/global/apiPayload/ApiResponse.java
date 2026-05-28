package community.backend.global.apiPayload;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiReponse<T> {

  private final boolean success;
  private final String code;
  private final String messgae;
  private final T data;

  public static <T> ApiResponse<T> onSuccess()

}
