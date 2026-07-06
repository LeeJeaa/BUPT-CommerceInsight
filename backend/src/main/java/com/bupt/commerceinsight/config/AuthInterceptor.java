package com.bupt.commerceinsight.config;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenStore tokenStore;

    public AuthInterceptor(AuthTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (isPublicPath(path) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        CurrentUser user = tokenStore.findByToken(token)
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或 token 无效"));

        if (!user.isApproved()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户尚未审批或已被禁用");
        }
        AuthContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isPublicPath(String path) {
        return "/api/health".equals(path)
            || "/api/auth/login".equals(path)
            || "/api/auth/register".equals(path)
            || path.startsWith("/error");
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        String xToken = request.getHeader("X-Token");
        return xToken == null ? "" : xToken.trim();
    }
}
