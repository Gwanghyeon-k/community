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
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    if (postLikeRepository.existsByPostIdAndUser_Id(postId, userId)) {
      throw new BusinessException(ErrorCode.POST_LIKE_ALREADY_EXISTS);
    }

    try {
      postLikeRepository.save(PostLike.builder()
          .post(post)
          .user(user)
          .build());
    } catch (DataIntegrityViolationException exception) {
      throw new BusinessException(ErrorCode.POST_LIKE_ALREADY_EXISTS);
    }
    postQuerydslRepository.increaseLikeCount(postId);
  }

  @Transactional
  public void unlike(Long postId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }

    if (!postRepository.existsById(postId)) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }
    PostLike postLike = postLikeRepository.findByPostIdAndUser_Id(postId, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_LIKE_NOT_FOUND));

    postLikeRepository.delete(postLike);
    postQuerydslRepository.decreaseLikeCount(postId);
  }
}
