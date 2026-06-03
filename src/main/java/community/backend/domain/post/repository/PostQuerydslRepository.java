package community.backend.domain.post.repository;

import static community.backend.domain.post.entity.QPost.post;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}