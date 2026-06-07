package community.backend.domain.postimage.entity;

import community.backend.domain.post.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "post_images",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_post_images_post_id", columnNames = "post_id")
    }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @Column(name = "post_image_url", nullable = false)
  private String postImageUrl;

  public static PostImage of(Post post, String postImageUrl) {
    return PostImage.builder()
        .post(post)
        .postImageUrl(postImageUrl)
        .build();
  }

  public void updatePostImageUrl(String postImageUrl) {
    this.postImageUrl = postImageUrl;
  }
}
