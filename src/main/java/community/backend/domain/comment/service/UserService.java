package community.backend.domain.comment.service;

import community.backend.domain.comment.dto.request.SignUpRequest;
import community.backend.domain.comment.entity.User;
import community.backend.domain.comment.repository.UserRepository;
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

}
