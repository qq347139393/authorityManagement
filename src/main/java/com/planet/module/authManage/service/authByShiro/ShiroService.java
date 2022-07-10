package com.planet.module.authManage.service.authByShiro;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ShiroService {
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


    /**
     * 更新shiro的权限链缓存
     * @return
     */
    boolean updateShiroPermissions();


}
