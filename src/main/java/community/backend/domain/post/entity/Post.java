package community.backend.domain.post.entity;

import community.backend.domain.user.entity.User;
import community.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "posts")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "description", nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(name = "post_image_url")
  private String postImageUrl;

  @Column(name = "view_count", nullable = false)
  private Long viewCount;

  @Column(name = "like_count", nullable = false)
  private Long likeCount;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public void increaseViewCount() {
    this.viewCount = this.viewCount + 1;
  }

  public void update(String title, String description, String postImageUrl) {
    this.title = title;
    this.description = description;
    this.postImageUrl = postImageUrl;
  }

  public boolean isOwnedBy(Long userId) {
    return this.user != null && this.user.getId().equals(userId);
  }

  public void increaseCommentCount() {
    this.commentCount = this.commentCount + 1;
  }

  public void decreaseCommentCount() {
    this.commentCount = this.commentCount > 0 ? this.commentCount - 1 : 0;
  }

  public void increaseLikeCount() {
    this.likeCount = this.likeCount + 1;
  }

  public void decreaseLikeCount() {
    this.likeCount = this.likeCount > 0 ? this.likeCount - 1 : 0;
  }
}
