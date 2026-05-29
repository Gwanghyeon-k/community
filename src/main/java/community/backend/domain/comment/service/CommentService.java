package community.backend.domain.comment.service;

import community.backend.domain.comment.dto.request.CreateCommentRequest;
import community.backend.domain.comment.dto.request.UpdateCommentRequest;
import community.backend.domain.comment.dto.response.CommentListItemResponse;
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
  public List<CommentListItemResponse> listByPost(Long postId) {
    if (!commentRepository.existsPostById(postId)) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return commentRepository.findByPostId(postId);
  }

  @Transactional
  public void create(Long postId, Long userId, CreateCommentRequest request) {
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
  public void update(Long commentId, Long userId, UpdateCommentRequest request) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!comment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    int updated = commentRepository.updateContent(commentId, request.getComment());
    if (updated == 0) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
  }

  @Transactional
  public void delete(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!comment.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    int deleted = commentRepository.softDelete(commentId);
    if (deleted == 0) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    commentRepository.decreasePostCommentCount(comment.getPostId());
  }
}

