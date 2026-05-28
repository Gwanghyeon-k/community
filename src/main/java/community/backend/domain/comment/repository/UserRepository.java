package community.backend.domain.comment.repository;

import community.backend.domain.comment.entity.User;
import java.util.Optional;

public interface UserRepository {

  long save(User user);

  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);


  int updateNickname(Long userId, String nickname);
  int updatePassword(Long userId, String encodedPassword);
  int updateProfileImage(Long userId, String profileImageUrl);
}
