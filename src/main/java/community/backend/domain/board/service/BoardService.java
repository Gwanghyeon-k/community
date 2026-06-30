package community.backend.domain.board.service;

import community.backend.domain.board.dto.response.BoardResponse;
import community.backend.domain.board.repository.BoardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

  private final BoardRepository boardRepository;

  @Transactional(readOnly = true)
  public List<BoardResponse> list() {
    return boardRepository.findByIsActiveTrueOrderByIdAsc().stream()
        .map(BoardResponse::from)
        .toList();
  }
}
