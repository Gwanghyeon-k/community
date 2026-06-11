package community.backend.domain.post.repository;

import static community.backend.domain.post.entity.QPost.post;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PostQuerydslRepository {

  private final JPAQueryFactory queryFactory;

  public void increaseLikeCount(Long postId) {
    queryFactory
        .update(post)
        .set(post.likeCount, post.likeCount.add(1L))
        .where(post.id.eq(postId))
        .execute();
  }

  public void decreaseLikeCount(Long postId) {
    NumberExpression<Long> decreasedLikeCount = new CaseBuilder()
        .when(post.likeCount.gt(0L)).then(post.likeCount.subtract(1L))
        .otherwise(0L);

    queryFactory
        .update(post)
        .set(post.likeCount, decreasedLikeCount)
        .where(post.id.eq(postId))
        .execute();
  }

  @Transactional
  public long increaseViewCount(Long postId, long delta) {
    // 조회 수에 마이너스 연산이 실행되는 것을 막기 위함
    if(delta <= 0) return 0L;
    return queryFactory
        .update(post)
        .set(post.viewCount, post.viewCount.add(delta))
        .where(post.id.eq(postId))
        .execute();
  }

  public long increaseCommentCount(Long postId) {
    return queryFactory
        .update(post)
        .set(post.commentCount, post.commentCount.add(1L))
        .where(post.id.eq(postId))
        .execute();
  }
  public long decreaseCommentCount(Long postId) {
    NumberExpression<Long> decreasedCommentCount = new CaseBuilder()
        .when(post.commentCount.gt(0L)).then(post.commentCount.subtract(1L))
        .otherwise(0L);
    return queryFactory
        .update(post)
        .set(post.commentCount, decreasedCommentCount)
        .where(post.id.eq(postId))
        .execute();
  }
}