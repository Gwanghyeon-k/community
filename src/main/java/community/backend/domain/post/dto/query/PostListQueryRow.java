package community.backend.domain.post.dto.query;

import java.time.LocalDateTime;

public record PostListQueryRow(
    Long postId,
    String title,
    String nickname,
    String profileImage,
    LocalDateTime updatedAt,
    Long likeCount,
    Long commentCount,
    Long viewCount
) {
}
