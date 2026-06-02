package community.backend.domain.user.service;

import community.backend.domain.user.dto.request.SignUpRequest;
import community.backend.domain.user.dto.request.UpdateUserRequest;
import community.backend.domain.user.dto.response.UserProfileResponse;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public void signUp(SignUpRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(request.getPassword())
        .nickname(request.getNickname())
        .profileImageUrl(request.getProfileImageUrl())
        .build();

    userRepository.save(user);
  }

  public UserProfileResponse getUserProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    return UserProfileResponse.from(user);
  }

  public void updateUser(Long authenticatedUserId, Long targetUserId, UpdateUserRequest request) {
    if (authenticatedUserId == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    if (!authenticatedUserId.equals(targetUserId)) {
      throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    User user = userRepository.findById(targetUserId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

    boolean hasNickname = request.getNickname() != null;
    boolean hasPassword = request.getPassword() != null;
    boolean hasProfileImage = request.getProfileImageUrl() != null;

    if (!hasNickname && !hasPassword && !hasProfileImage) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }

    if (hasNickname && !request.getNickname().equals(user.getNickname())) {
      if (userRepository.existsByNickname(request.getNickname())) {
        throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
      }
      userRepository.updateNickname(targetUserId, request.getNickname());
    }

    if (hasPassword) {
      userRepository.updatePassword(targetUserId, request.getPassword());
    }

    if (hasProfileImage) {
      userRepository.updateProfileImage(targetUserId, request.getProfileImageUrl());
    }
  }

}
