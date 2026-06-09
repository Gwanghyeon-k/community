package community.backend.domain.postlike.repository;

import community.backend.domain.postlike.entity.PostLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

  boolean existsByPostIdAndUser_Id(Long postId, Long userId);

  Optional<PostLike> findByPostIdAndUser_Id(Long postId, Long userId);
}
