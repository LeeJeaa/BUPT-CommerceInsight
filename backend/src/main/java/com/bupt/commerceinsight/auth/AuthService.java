package com.bupt.commerceinsight.auth;

import com.bupt.commerceinsight.auth.dto.LoginRequest;
import com.bupt.commerceinsight.auth.dto.RegisterRequest;
import com.bupt.commerceinsight.auth.vo.LoginVO;
import com.bupt.commerceinsight.auth.vo.RegisterVO;
import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.config.AuthTokenStore;
import com.bupt.commerceinsight.user.UserAccount;
import com.bupt.commerceinsight.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenStore tokenStore;

    public AuthService(UserRepository userRepository, AuthTokenStore tokenStore) {
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
    }

    public RegisterVO register(RegisterRequest request) {
        UserAccount user = userRepository.register(
            request.getUsername(),
            request.getPassword(),
            request.getRealName(),
            request.getEmail()
        );
        return new RegisterVO(user.userId(), user.username(), user.status());
    }

    public LoginVO login(LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!userRepository.matchesPassword(user, request.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if ("pending".equals(user.status())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户尚未审批");
        }
        if ("disabled".equals(user.status())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户已被禁用");
        }
        String token = tokenStore.issue(user);
        return new LoginVO(token, user.userId(), user.username(), user.role(), user.status());
    }
}
