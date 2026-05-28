package community.backend.domain.comment.entity;

import community.backend.global.entity.BaseEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User extends BaseEntity {
  private Long id;
  private String email;
  private String password;
  private String nickname;
  private String profileImageUrl;
}
