package community.backend.domain.userprofileimage.entity;

import community.backend.domain.user.entity.User;
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
    name = "user_profile_images",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_user_profile_images_user_id", columnNames = "user_id")
    }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "user_profile_image_url", nullable = false)
  private String userProfileImageUrl;

  public static UserProfileImage of(User user, String userProfileImageUrl) {
    return UserProfileImage.builder()
        .user(user)
        .userProfileImageUrl(userProfileImageUrl)
        .build();
  }

  public void updateUserProfileImageUrl(String userProfileImageUrl) {
    this.userProfileImageUrl = userProfileImageUrl;
  }
}
