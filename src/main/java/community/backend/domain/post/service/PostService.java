package community.backend.domain.post.service;

import community.backend.domain.post.dto.request.CreatePostRequest;
import community.backend.domain.post.dto.request.UpdatePostRequest;
import community.backend.domain.post.dto.response.CreatePostResponse;
import community.backend.domain.post.dto.response.PostDetailResponse;
import community.backend.domain.post.dto.response.PostListResponse;
import community.backend.domain.post.entity.Post;
import community.backend.domain.post.repository.PostRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  @Transactional
  public CreatePostResponse create(Long userId, CreatePostRequest request) {
    long postId = postRepository.save(Post.builder()
        .userId(userId)
        .title(request.getTitle())
        .description(request.getDescription())
        .postImageUrl(request.getPostImageUrl())
        .build());
    return new CreatePostResponse(postId, request.getTitle());
  }

  @Transactional(readOnly = true)
  public PostListResponse list(Long lastPostId, int size) {
    List<?> posts = postRepository.findList(lastPostId, size);
    boolean isLast = posts.size() < size;
    return new PostListResponse((List) posts, isLast);
  }

  @Transactional
  public PostDetailResponse detail(Long postId) throws BusinessException {
    if(postRepository.increaseViewCount(postId) == 0) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return postRepository.findDetail(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
  }

  @Transactional
  public void update(Long userId, Long postId, UpdatePostRequest request) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if(!post.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    int updated = postRepository.updatePost(
        postId,
        request.getTitle(),
        request.getDescription(),
        request.getPostImageUrl()
    );
    if (updated == 0) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
  }

  @Transactional
  public void delete(Long userId, Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    if (!post.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }
    postRepository.softDelete(postId);
  }
}
