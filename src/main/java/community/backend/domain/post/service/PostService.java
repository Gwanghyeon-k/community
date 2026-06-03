package community.backend.domain.post.service;

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

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  @Transactional
  public CreatePostResponse create(Long userId, CreatePostRequest request) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    Post post = postRepository.save(Post.builder()
        .user(user)
        .title(request.getTitle())
        .description(request.getDescription())
        .postImageUrl(request.getPostImageUrl())
        .viewCount(0L)
        .likeCount(0L)
        .commentCount(0L)
        .build());
    return new CreatePostResponse(post.getId(), post.getTitle());
  }

  @Transactional(readOnly = true)
  public PostListResponse list(Long lastPostId, int size) {
    Long cursor = (lastPostId == null || lastPostId == 0) ? null : lastPostId;
    List<Post> entities = cursor == null
        ? postRepository.findAllByOrderByIdDesc(PageRequest.of(0, size))
        : postRepository.findByIdLessThanOrderByIdDesc(cursor, PageRequest.of(0, size));

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

  @Transactional
  public PostDetailResponse detail(Long postId) throws BusinessException {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    post.increaseViewCount();
    return new PostDetailResponse(
        post.getId(),
        post.getTitle(),
        post.getDescription(),
        post.getPostImageUrl(),
        new PostDetailResponse.Author(post.getUser().getNickname(), post.getUser().getProfileImageUrl()),
        formatDateTime(post.getUpdatedAt()),
        post.getLikeCount(),
        post.getViewCount()
    );
  }

  @Transactional
  public void update(Long userId, Long postId, UpdatePostRequest request) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!post.isOwnedBy(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    post.update(request.getTitle(), request.getDescription(), request.getPostImageUrl());
  }

  @Transactional
  public void delete(Long userId, Long postId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!post.isOwnedBy(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    postRepository.delete(post);
  }

  private static String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return null;
    }
    return value.format(FORMATTER);
  }

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
