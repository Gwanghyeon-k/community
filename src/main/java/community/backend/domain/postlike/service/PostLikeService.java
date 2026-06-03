package community.backend.domain.postlike.service;

import community.backend.domain.post.entity.Post;
import community.backend.domain.post.repository.PostQuerydslRepository;
import community.backend.domain.post.repository.PostRepository;
import community.backend.domain.postlike.entity.PostLike;
import community.backend.domain.postlike.repository.PostLikeRepository;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

  private final PostLikeRepository postLikeRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final PostQuerydslRepository postQuerydslRepository;

  @Transactional
  public void like(Long postId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

    if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }

    try {
      postLikeRepository.save(PostLike.builder()
          .post(post)
          .user(user)
          .build());
    } catch (DataIntegrityViolationException exception) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    postQuerydslRepository.increaseLikeCount(postId);
  }

  @Transactional
  public void unlike(Long postId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    if (!postRepository.existsById(postId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));

    postLikeRepository.delete(postLike);
    postQuerydslRepository.decreaseLikeCount(postId);
  }
}
