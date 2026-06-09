package community.backend.domain.comment.service;

import community.backend.domain.comment.dto.request.CreateCommentRequest;
import community.backend.domain.comment.dto.request.UpdateCommentRequest;
import community.backend.domain.comment.dto.response.CommentListItemResponse;
import community.backend.domain.comment.dto.response.CommentListResponse;
import community.backend.domain.comment.entity.Comment;
import community.backend.domain.comment.repository.CommentQuerydslRepository;
import community.backend.domain.comment.repository.CommentRepository;
import community.backend.domain.post.entity.Post;
import community.backend.domain.post.repository.PostQuerydslRepository;
import community.backend.domain.post.repository.PostRepository;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentQuerydslRepository commentQuerydslRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final PostQuerydslRepository postQuerydslRepository;


  @Transactional(readOnly = true)
  public CommentListResponse listByPost(Long postId, Long lastCommentId, int size) {
    if (size < 1) {
      throw new BusinessException(ErrorCode.INVALID_COMMENT_PAGE_SIZE);
    }
    if (!postRepository.existsById(postId)) {
      throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }

    Long cursor = (lastCommentId == null || lastCommentId == 0) ? null : lastCommentId;
    List<CommentListItemResponse> comments = commentQuerydslRepository.findByPostId(postId, cursor, size);

    boolean isLast = comments.size() < size;
    Long nextCommentId = isLast || comments.isEmpty() ? null : comments.get(comments.size() - 1).getCommentId();

    return new CommentListResponse(comments, isLast, nextCommentId);
  }

  @Transactional
  public void create(Long postId, Long userId, CreateCommentRequest request) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    commentRepository.save(Comment.builder()
        .post(post)
        .user(user)
        .content(request.getComment())
        .build());
    postQuerydslRepository.increaseCommentCount(postId);
  }

  @Transactional
  public void update(Long postId, Long commentId, Long userId, UpdateCommentRequest request) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    if (!comment.isOwnedBy(userId)) {
      throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
    }
    comment.updateContent(request.getComment());
  }

  @Transactional
  public void delete(Long postId, Long commentId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    if (!comment.isOwnedBy(userId)) {
      throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
    }
    commentRepository.delete(comment);
    postQuerydslRepository.decreaseCommentCount(postId);
  }
}

