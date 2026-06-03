package community.backend.domain.post.repository;

import community.backend.domain.post.entity.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

  List<Post> findAllByOrderByIdDesc(Pageable pageable);

  List<Post> findByIdLessThanOrderByIdDesc(Long cursorId, Pageable pageable);

}
