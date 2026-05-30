package community.backend.domain.user.repository;

import community.backend.domain.user.entity.User;
import java.util.Optional;

public interface UserRepository {

  long save(User user);

  Optional<User> findByEmail(String email);
  Optional<User> findById(Long userId);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);


  int updateNickname(Long userId, String nickname);
  int updatePassword(Long userId, String encodedPassword);
  int updateProfileImage(Long userId, String profileImageUrl);
}
