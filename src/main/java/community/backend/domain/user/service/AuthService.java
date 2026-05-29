package community.backend.domain.user.service;

import community.backend.domain.user.dto.request.LoginRequest;
import community.backend.domain.user.dto.response.LoginResponse;
import community.backend.domain.user.entity.User;
import community.backend.domain.user.repository.UserRepository;
import community.backend.global.apiPayload.code.ErrorCode;
import community.backend.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;

  public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

    if(!(request.getPassword().equals(user.getPassword()))) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    return new LoginResponse(user.getNickname());
  }
}
