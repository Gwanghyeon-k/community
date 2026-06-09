package community.backend.global.image.controller;

import community.backend.global.image.dto.response.ImageUploadResponse;
import community.backend.global.image.service.ImageUploadService;
import community.backend.global.apiPayload.ApiResponse;
import community.backend.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageUploadController {

  private final ImageUploadService imageUploadService;

  /**
   * 사용자 프로필 이미지 업로드
   */
  @PostMapping(value = "/userprofiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadUserProfileImage(
      @RequestPart("file") MultipartFile file
  ) {
    ImageUploadResponse response = imageUploadService.uploadUserProfileImage(file);
    return ApiResponse.onSuccess(SuccessCode.CREATED, response);
  }

  /**
   * 게시글 이미지를 업로드
   */
  @PostMapping(value = "/postImages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadPostImage(
      @RequestPart("file") MultipartFile file
  ) {
    ImageUploadResponse response = imageUploadService.uploadPostImage(file);
    return ApiResponse.onSuccess(SuccessCode.CREATED, response);
  }
}
