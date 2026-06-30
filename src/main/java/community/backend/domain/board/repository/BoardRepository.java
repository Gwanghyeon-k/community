package community.backend.domain.board.repository;


import community.backend.domain.board.entity.Board;
import community.backend.domain.board.entity.BoardCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

  Optional<Board> findByCategory(BoardCategory category);

  List<Board> findByIsActiveTrueOrderByIdAsc();
}
