package community.backend.domain.post.repository;

import community.backend.domain.post.entity.Post;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

  List<Post> findAllByOrderByIdDesc(Pageable pageable);

  List<Post> findByIdLessThanOrderByIdDesc(Long cursorId, Pageable pageable);

}
