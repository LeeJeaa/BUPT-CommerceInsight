package com.bupt.commerceinsight.user;

import com.bupt.commerceinsight.common.ApiResponse;
import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.user.vo.UserStatusVO;
import com.bupt.commerceinsight.user.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserVO>> listUsers(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(userService.listUsers(pageNo, pageSize, status, keyword));
    }

    @PutMapping("/{userId}/approve")
    public ApiResponse<UserStatusVO> approve(@PathVariable Long userId) {
        return ApiResponse.success(userService.approve(userId));
    }

    @PutMapping("/{userId}/disable")
    public ApiResponse<UserStatusVO> disable(@PathVariable Long userId) {
        return ApiResponse.success(userService.disable(userId));
    }
}
