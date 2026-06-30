package community.backend.domain.post.service;

import community.backend.domain.board.entity.Board;
import community.backend.domain.board.entity.BoardCategory;
import community.backend.domain.board.repository.BoardRepository;
import community.backend.domain.post.dto.request.CreatePostRequest;
import community.backend.domain.post.dto.request.UpdatePostRequest;
import community.backend.domain.post.dto.response.CreatePostResponse;
import community.backend.domain.post.dto.response.PostDetailResponse;
import community.backend.domain.post.dto.response.PostListDetailResponse;
import community.backend.domain.post.dto.response.PostListResponse;
import community.backend.domain.post.entity.Post;
import community.backend.domain.post.repository.PostRepository;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private static final long HOT_LIKE_THRESHOLD = 10L;
  private static final long BEST_LIKE_THRESHOLD = 100L;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final BoardRepository boardRepository;
  private final ViewCountBufferService viewCountBufferService;

  @Transactional
  public CreatePostResponse create(Long userId, CreatePostRequest request) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    Board board = boardRepository.findByCategory(request.getBoardCategory())
        .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    Post post = Post.builder()
        .board(board)
        .user(user)
        .title(request.getTitle())
        .description(request.getDescription())
        .viewCount(0L)
        .likeCount(0L)
        .commentCount(0L)
        .build();
    post.updatePostImageUrl(request.getPostImageUrl());
    post = postRepository.save(post);
    return new CreatePostResponse(post.getId(), post.getTitle());
  }

  @Transactional(readOnly = true)
  public PostListResponse list(Long lastPostId, int size) {
    Long cursor = (lastPostId == null || lastPostId == 0) ? null : lastPostId;
    List<Post> entities = cursor == null
        ? postRepository.findAllByOrderByIdDesc(PageRequest.of(0, size))
        : postRepository.findByIdLessThanOrderByIdDesc(cursor, PageRequest.of(0, size));

    return toListResponse(entities, size);
  }

  @Transactional(readOnly = true)
  public PostListResponse listByBoard(String boardCategory, Long lastPostId, int size) {
    String normalizedCategory = boardCategory == null ? "" : boardCategory.toUpperCase();
    if ("HOT".equals(normalizedCategory)) {
      return listByLikeThreshold(HOT_LIKE_THRESHOLD, lastPostId, size);
    }
    if ("BEST".equals(normalizedCategory)) {
      return listByLikeThreshold(BEST_LIKE_THRESHOLD, lastPostId, size);
    }

    BoardCategory category = parseBoardCategory(normalizedCategory);
    Long cursor = normalizeCursor(lastPostId);
    List<Post> entities = cursor == null
        ? postRepository.findByBoard_CategoryOrderByIdDesc(category, PageRequest.of(0, size))
        : postRepository.findByBoard_CategoryAndIdLessThanOrderByIdDesc(category, cursor, PageRequest.of(0, size));

    return toListResponse(entities, size);
  }

  @Transactional(readOnly = true)
  public PostDetailResponse detail(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

    viewCountBufferService.increment(postId);

    return new PostDetailResponse(
        post.getId(),
        post.getTitle(),
        post.getDescription(),
        post.getPostImageUrl(),
        new PostDetailResponse.Author(post.getUser().getNickname(), post.getUser().getProfileImageUrl()),
        formatDateTime(post.getUpdatedAt()),
        post.getLikeCount(),
        post.getViewCount() + 1 // 즉시 응답에서만 증가 반영
    );
  }

  @Transactional
  public void update(Long userId, Long postId, UpdatePostRequest request) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    if (post.isOwnedBy(userId)) {
      throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
    }
    post.update(request.getTitle(), request.getDescription(), request.getPostImageUrl());
  }

  @Transactional
  public void delete(Long userId, Long postId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    if (post.isOwnedBy(userId)) {
      throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
    }
    postRepository.delete(post);
  }

  private static String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return null;
    }
    return value.format(FORMATTER);
  }

  private PostListResponse listByLikeThreshold(Long threshold, Long lastPostId, int size) {
    Long cursor = normalizeCursor(lastPostId);
    List<Post> entities = cursor == null
        ? postRepository.findByLikeCountGreaterThanEqualOrderByIdDesc(threshold, PageRequest.of(0, size))
        : postRepository.findByLikeCountGreaterThanEqualAndIdLessThanOrderByIdDesc(
            threshold,
            cursor,
            PageRequest.of(0, size)
        );

    return toListResponse(entities, size);
  }

  private PostListResponse toListResponse(List<Post> entities, int size) {
    List<PostListDetailResponse> posts = entities.stream()
        .map(post -> new PostListDetailResponse(
            post.getId(),
            post.getTitle(),
            post.getUser().getNickname(),
            post.getUser().getProfileImageUrl(),
            formatDateTime(post.getUpdatedAt()),
            post.getLikeCount(),
            displayCount(post.getLikeCount()),
            post.getCommentCount(),
            post.getViewCount(),
            displayCount(post.getViewCount())
        ))
        .toList();

    return new PostListResponse(posts, posts.size() < size);
  }

  private static Long normalizeCursor(Long lastPostId) {
    return (lastPostId == null || lastPostId == 0) ? null : lastPostId;
  }

  private static BoardCategory parseBoardCategory(String boardCategory) {
    try {
      return BoardCategory.valueOf(boardCategory);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.BOARD_NOT_FOUND);
    }
  }

  /***
   * 조회 수 변환 메소드
   */
  private static String displayCount(Long value) {
    long target = value == null ? 0L : value;
    if (target >= 1_000_000) {
      return (target / 1_000_000) + "m";
    }
    if (target >= 1_000) {
      return (target / 1_000) + "k";
    }
    return String.valueOf(target);
  }
}
