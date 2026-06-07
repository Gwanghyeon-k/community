package community.backend.domain.comment.repository;

import static community.backend.domain.comment.entity.QComment.comment;
import static community.backend.domain.user.entity.QUser.user;
import static community.backend.domain.userprofileimage.entity.QUserProfileImage.userProfileImage;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import community.backend.domain.comment.dto.response.CommentListItemResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentQuerydslRepository {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final JPAQueryFactory queryFactory;

  public List<CommentListItemResponse> findByPostId(Long postId, Long lastCommentId, int size) {
    List<Tuple> rows = queryFactory
        .select(
            comment.id,
            user.nickname,
            userProfileImage.userProfileImageUrl,
            comment.content,
            comment.updatedAt
        )
        .from(comment)
        .join(comment.user, user)
        .leftJoin(user.userProfileImage, userProfileImage)
        .where(
            comment.post.id.eq(postId),
            lastCommentId == null ? null : comment.id.lt(lastCommentId)
        )
        .orderBy(comment.id.desc())
        .limit(size)
        .fetch();

    return rows.stream()
        .map(row -> new CommentListItemResponse(
            row.get(comment.id),
            row.get(user.nickname),
            row.get(userProfileImage.userProfileImageUrl),
            row.get(comment.content),
            formatDateTime(row.get(comment.updatedAt))
        ))
        .toList();
  }

  private static String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return null;
    }
    return value.format(FORMATTER);
  }
}
