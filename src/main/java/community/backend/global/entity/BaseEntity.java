package community.backend.global.entity;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BaseEntity {

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;
}
