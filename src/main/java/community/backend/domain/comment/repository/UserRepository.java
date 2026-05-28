package community.backend.domain.comment.repository;

import community.backend.domain.comment.entity.User;
import java.util.Optional;

public interface UserRepository {

  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
  long save(User user);

}
