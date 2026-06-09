package community.backend.global.image.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {

  String uploadUserProfileImage(MultipartFile file);

  String uploadPostImage(MultipartFile file);
}
