package community.backend.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateUserRequest {

  @Size(max = 10, message = "닉네임은 최대 10자까지 작성 가능합니다.")
  @Pattern(regexp = "^\\S+$", message = "띄어쓰기를 없애주세요.")
  private String nickname;

  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$",
      message = "비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."
  )
  private String password;

  private String profileImageUrl;
}
