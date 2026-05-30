package community.backend.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

  @NotBlank(message = "REQUIRED")
  @Email(message = "INVALID_FORMAT")
  private String email;

  @NotBlank(message = "REQUIRED")
  @Size(min = 8, message = "TOO_SHORT") // 길이가 8 미만
  @Size(max = 20, message = "TOO_LONG") // 길이가 20 초과
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
      message = "INVALID_FORMAT"
  )
  private String password;
}
