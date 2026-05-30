package community.backend.domain.user.service;

import community.backend.domain.user.dto.request.SignUpRequest;
import community.backend.domain.user.dto.request.UpdateNicknameRequest;
import community.backend.domain.user.dto.request.UpdatePasswordRequest;
import community.backend.domain.user.dto.request.UpdateProfileImageRequest;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void signUp(SignUpRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .profileImageUrl(request.getProfileImageUrl())
        .build();

    userRepository.save(user);
  }

  @Transactional
  public void updateNickname(Long userId, UpdateNicknameRequest request) {
    if(userRepository.existsByNickname(request.getNickname())) {
      throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
    }
    userRepository.updateNickname(userId, request.getNickname());
  }

  @Transactional
  public void updateProfileImage(Long userId, UpdateProfileImageRequest request) {
    userRepository.updateProfileImage(userId, request.getProfileImageUrl());
  }

  @Transactional
  public void updatePassword(Long userId, UpdatePasswordRequest request) {
    userRepository.updatePassword(userId, passwordEncoder.encode(request.getPassword()));
  }

}
