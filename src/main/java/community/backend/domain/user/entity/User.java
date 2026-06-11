package community.backend.domain.user.entity;

import community.backend.domain.comment.entity.Comment;
import community.backend.domain.post.entity.Post;
import community.backend.domain.userprofileimage.entity.UserProfileImage;
import community.backend.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_users_email", columnNames = "email"),
        @UniqueConstraint(name = "ux_users_nickname", columnNames = "nickname")
    }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email", nullable = false, length = 100)
  private String email;

  @Column(name = "password", nullable = false, length = 100)
  private String password;

  @Column(name = "nickname", nullable = false, length = 20)
  private String nickname;

  @OneToMany(mappedBy = "user")
  @Builder.Default
  private List<Post> posts = new ArrayList<>();

  @OneToMany(mappedBy = "user")
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private UserProfileImage userProfileImage;

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void updatePassword(String password) {
    this.password = password;
  }

  public String getProfileImageUrl() {
    return userProfileImage == null ? null : userProfileImage.getUserProfileImageUrl();
  }

  public void updateProfileImageUrl(String profileImageUrl) {
    if (profileImageUrl == null) {
      this.userProfileImage = null;
      return;
    }
    if (this.userProfileImage == null) {
      this.userProfileImage = UserProfileImage.of(this, profileImageUrl);
      return;
    }
    this.userProfileImage.updateUserProfileImageUrl(profileImageUrl);
  }
}
