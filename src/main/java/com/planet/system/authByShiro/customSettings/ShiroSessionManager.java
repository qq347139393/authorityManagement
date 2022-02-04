package com.planet.system.authByShiro.customSettings;

import com.planet.common.constant.UtilsConstant;
import com.planet.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * @Description：自定义会话管理器
 */
public class ShiroSessionManager extends DefaultWebSessionManager {
    //自定义注入的资源类型名称
    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    /**
     * 通过请求对象获取sessionId
     * @param request
     * @param response
     * @return
     */
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        //判断request请求中是否带有jwtToken的key
        String jwtToken = WebUtils.toHttp(request).getHeader(UtilsConstant.AUTH_TOKEN);
        //1)如果没有,走默认的获得sessionId的方式
        if (jwtToken == null || "".equals(jwtToken.trim()) || "null".equalsIgnoreCase(jwtToken.trim()) || "undefined".equalsIgnoreCase(jwtToken.trim())){
            return super.getSessionId(request, response);
        }else {
            //2)如果有,走jwtToken获得sessionId的的方式
            Claims claims = JwtUtil.decodeJwt(jwtToken);
            String id = (String) claims.get("jti");
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                    REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, id);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            return id;
        }

    }

}
