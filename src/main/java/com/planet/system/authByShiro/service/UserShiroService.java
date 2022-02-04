package com.planet.system.authByShiro.service;

public interface UserShiroService {
    /**
     * 根据用户的userId来管理其下的session:用于限制用户并发登录次数
     * @param userId
     * @param sessionId
     */
    void userSessionManage(Long userId,String sessionId);

    /**
     * 删除UserId对应的userSession队列
     * @param userId
     */
    void deleteUserSessionByUserId(Long userId);
}
