package community.backend.global.image.storage;

import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@Profile({"dev", "prod"})
public class S3ImageStorage implements ImageStorage {

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
  private static final String USER_PROFILE_DIR = "userprofile";
  private static final String POST_IMAGE_DIR = "postImage";

  private final S3Client s3Client;
  private final String bucket;
  private final String publicBaseUrl;

  public S3ImageStorage(
      S3Client s3Client,
      @Value("${cloud.aws.s3.bucket}") String bucket,
      @Value("${cloud.aws.s3.public-base-url}") String publicBaseUrl
  ) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.publicBaseUrl = normalizePublicBaseUrl(publicBaseUrl);
  }

  @Override
  public String uploadUserProfileImage(MultipartFile file) {
    return upload(file, USER_PROFILE_DIR);
  }

  @Override
  public String uploadPostImage(MultipartFile file) {
    return upload(file, POST_IMAGE_DIR);
  }

  private String upload(MultipartFile file, String targetDirectory) {
    validateFile(file);

    String extension = extractExtension(file.getOriginalFilename());
    String key = targetDirectory + "/" + UUID.randomUUID() + "." + extension;

    try {
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();
      s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (IOException | S3Exception exception) {
      throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    return publicBaseUrl + "/" + key;
  }

  private static void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    String extension = extractExtension(file.getOriginalFilename());
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
  }

  private static String extractExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    return fileName.substring(lastDotIndex + 1).toLowerCase();
  }

  private static String normalizePublicBaseUrl(String publicBaseUrl) {
    if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
      throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    return publicBaseUrl.endsWith("/")
        ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
        : publicBaseUrl;
  }
}
