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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  /***
   * 회원가입 / 유저 이메일 중복 검증 / 유저 닉네임 중복 검증
   * @param request
   */
  @Transactional
  public void signUp(SignUpRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
    }
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(request.getPassword())
        .nickname(request.getNickname())
        .build();
    user.updateProfileImageUrl(request.getProfileImageUrl());

    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public UserProfileResponse getUserProfile(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    return UserProfileResponse.from(user);
  }

  @Transactional
  public void updateUser(Long authenticatedUserId, Long targetUserId, UpdateUserRequest request) {
    if (authenticatedUserId == null) {
      throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    if (!authenticatedUserId.equals(targetUserId)) {
      throw new BusinessException(ErrorCode.USER_ACCESS_DENIED);
    }

    User user = userRepository.findById(targetUserId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    boolean hasNickname = request.getNickname() != null;
    boolean hasPassword = request.getPassword() != null;
    boolean hasProfileImage = request.getProfileImageUrl() != null;

    if (!hasNickname && !hasPassword && !hasProfileImage) {
      throw new BusinessException(ErrorCode.USER_PROFILE_UPDATE_EMPTY);
    }

    if (hasNickname && !request.getNickname().equals(user.getNickname())) {
      if (userRepository.existsByNickname(request.getNickname())) {
        throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
      }
      user.updateNickname(request.getNickname());
    }

    if (hasPassword) {
      user.updatePassword(request.getPassword());
    }

    if (hasProfileImage) {
      user.updateProfileImageUrl(request.getProfileImageUrl());
    }
  }

}
