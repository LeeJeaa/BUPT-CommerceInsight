package com.bupt.commerceinsight.auth;

import com.bupt.commerceinsight.auth.dto.LoginRequest;
import com.bupt.commerceinsight.auth.dto.RegisterRequest;
import com.bupt.commerceinsight.auth.vo.LoginVO;
import com.bupt.commerceinsight.auth.vo.RegisterVO;
import com.bupt.commerceinsight.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}
