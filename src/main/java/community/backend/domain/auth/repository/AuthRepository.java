package community.backend.domain.auth.repository;

import community.backend.domain.auth.entity.Auth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<Auth, Long> {

  Optional<Auth> findByUserId(Long userId);

  void deleteByUserId(Long userId);
}