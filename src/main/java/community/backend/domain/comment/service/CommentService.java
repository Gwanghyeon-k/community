package community.backend.domain.comment.service;

import community.backend.domain.comment.dto.request.CreateCommentRequest;
import community.backend.domain.comment.dto.request.UpdateCommentRequest;
import community.backend.domain.comment.dto.response.CommentListItemResponse;
import community.backend.domain.comment.dto.response.CommentListResponse;
import community.backend.domain.comment.entity.Comment;
import community.backend.domain.comment.repository.CommentRepository;
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

  @Transactional(readOnly = true)
  public CommentListResponse listByPost(Long postId, Long lastCommentId, int size) {
    if (size < 1) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    if (!commentRepository.existsPostById(postId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }

    Long cursor = lastCommentId == 0 ? Long.MAX_VALUE : lastCommentId;
    List<CommentListItemResponse> comments = commentRepository.findByPostId(postId, cursor, size);

    boolean isLast = comments.size() < size;
    Long nextCommentId = isLast || comments.isEmpty() ? null : comments.get(comments.size() - 1).getCommentId();

    return new CommentListResponse(comments, isLast, nextCommentId);
  }

  @Transactional
  public void create(Long postId, Long userId, CreateCommentRequest request) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    if (!commentRepository.existsPostById(postId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    commentRepository.save(Comment.builder()
        .postId(postId)
        .userId(userId)
        .content(request.getComment())
        .build());
    commentRepository.increasePostCommentCount(postId);
  }

  @Transactional
  public void update(Long postId, Long commentId, Long userId, UpdateCommentRequest request) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!comment.getPostId().equals(postId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    if (!comment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    int updated = commentRepository.updateContent(commentId, request.getComment());
    if (updated == 0) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
  }

  @Transactional
  public void delete(Long postId, Long commentId, Long userId) {
    if (userId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!comment.getPostId().equals(postId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    if (!comment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    int deleted = commentRepository.delete(commentId);
    if (deleted == 0) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    commentRepository.decreasePostCommentCount(comment.getPostId());
  }
}

