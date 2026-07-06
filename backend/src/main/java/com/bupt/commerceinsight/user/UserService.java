package com.bupt.commerceinsight.user;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.config.AuthContext;
import com.bupt.commerceinsight.config.CurrentUser;
import com.bupt.commerceinsight.user.vo.UserStatusVO;
import com.bupt.commerceinsight.user.vo.UserVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResponse<UserVO> listUsers(int pageNo, int pageSize, String status, String keyword) {
        requireAdmin();
        List<UserVO> all = userRepository.search(status, keyword).stream().map(this::toVO).toList();
        int from = Math.min(Math.max(pageNo - 1, 0) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return new PageResponse<>(pageNo, pageSize, all.size(), all.subList(from, to));
    }

    public UserStatusVO approve(Long userId) {
        requireAdmin();
        UserAccount updated = userRepository.updateStatus(userId, "approved");
        return new UserStatusVO(updated.userId(), updated.status());
    }

    public UserStatusVO disable(Long userId) {
        requireAdmin();
        UserAccount updated = userRepository.updateStatus(userId, "disabled");
        return new UserStatusVO(updated.userId(), updated.status());
    }

    private void requireAdmin() {
        CurrentUser user = AuthContext.requireUser();
        if (!user.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限");
        }
    }

    private UserVO toVO(UserAccount user) {
        return new UserVO(
            user.userId(), user.username(), user.realName(), user.email(),
            user.role(), user.status(), user.createdAt()
        );
    }
}
