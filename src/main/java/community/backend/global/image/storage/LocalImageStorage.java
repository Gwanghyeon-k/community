package community.backend.global.image.storage;

import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile("local")
public class LocalImageStorage implements ImageStorage {

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
  private static final String USER_PROFILE_DIR = "userprofile";
  private static final String POST_IMAGE_DIR = "postImage";

  private final Path imageRootPath;
  private final String imagePathPrefix;
  private final String publicBaseUrl;

  public LocalImageStorage(
      @Value("${app.image.root-dir}") String rootDir,
      @Value("${app.image.base-url}") String baseUrl,
      @Value("${app.image.public-base-url}") String publicBaseUrl
  ) {
    this.imageRootPath = Path.of(System.getProperty("user.dir")).resolve(rootDir).toAbsolutePath().normalize();
    this.imagePathPrefix = toFullUrl(baseUrl);
    this.publicBaseUrl = publicBaseUrl;
  }

  @Override
  public String uploadUserProfileImage(MultipartFile file) {
    return upload(file, USER_PROFILE_DIR);
  }

  @Override
  public String uploadPostImage(MultipartFile file) {
    return upload(file, POST_IMAGE_DIR);
  }

  private String upload(MultipartFile file, String targetSubDirectory) {
    validateFile(file);

    String extension = extractExtension(file.getOriginalFilename());
    String storedFileName = UUID.randomUUID() + "." + extension;

    Path targetDirectory = imageRootPath.resolve(targetSubDirectory);
    Path targetFilePath = targetDirectory.resolve(storedFileName);

    try {
      Files.createDirectories(targetDirectory);
      Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    //상대 경로에 현재 서버 도메인을 붙여 전체 URL 생성
    return publicBaseUrl + imagePathPrefix + "/" + targetSubDirectory + "/" + storedFileName;
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

  private static String toFullUrl(String urlPath) {
    if (urlPath == null || urlPath.isBlank()) {
      return "/image";
    }
    String normalized = urlPath.startsWith("/") ? urlPath : "/" + urlPath;
    return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
  }

}
