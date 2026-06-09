package community.backend.global.image.service;

import community.backend.global.image.dto.response.ImageUploadResponse;
import community.backend.global.image.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

  private final ImageStorage imageStorage;

  public ImageUploadResponse uploadUserProfileImage(MultipartFile file) {
    return new ImageUploadResponse(imageStorage.uploadUserProfileImage(file));
  }

  public ImageUploadResponse uploadPostImage(MultipartFile file) {
    return new ImageUploadResponse(imageStorage.uploadPostImage(file));
  }
}
