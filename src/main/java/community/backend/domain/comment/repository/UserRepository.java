package community.backend.domain.comment.repository;

import community.backend.domain.comment.entity.User;

public interface UserRepository {
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
  long save(User user);

}
