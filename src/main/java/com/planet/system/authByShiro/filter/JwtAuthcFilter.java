package com.planet.system.authByShiro.filter;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.planet.common.util.RspResult;
import com.planet.util.JwtUtil;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @Description：自定义登录验证过滤器
 */
//@Component("jwtAuthcFilter")
public class JwtAuthcFilter extends FormAuthenticationFilter {

    /**
     * @Description 是否允许访问
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        //判断当前请求头中是否带有jwtToken的字符串
        String jwtToken = WebUtils.toHttp(request).getHeader("jwtToken");
        //如果有：走jwt校验
        if (jwtToken != null && !"".equals(jwtToken.trim()) && !"null".equalsIgnoreCase(jwtToken.trim()) && !"undefined".equalsIgnoreCase(jwtToken.trim())){
            boolean verifyToken = JwtUtil.checkJwt(jwtToken);
            if (verifyToken){
                //如果jwtToken合格,那么还要检查sessionId是否合格
                return super.isAccessAllowed(request, response, mappedValue);//接着将jwtToken中拿出的sessionId进行下一步比对
            }else {
                //如果jwtToken不合格,则拒绝访问
                return false;
            }
        }else{
            //如果没有jwtToken,则拒绝访问
            return false;
        }
        //没有没有：走原始校验
//        return super.isAccessAllowed(request, response, mappedValue);
    }

    /**
     * @Description 访问拒绝时调用
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
//        //判断当前请求头中是否带有jwtToken的字符串
//        String jwtToken = WebUtils.toHttp(request).getHeader("jwtToken");
//        //如果有：返回json的应答
//        if (jwtToken != null && !"".equals(jwtToken.trim()) && !"null".equalsIgnoreCase(jwtToken.trim()) && !"undefined".equalsIgnoreCase(jwtToken.trim())){
//            //构建错误的响应信息
//            response.setCharacterEncoding("UTF-8");
//            response.setContentType("application/json; charset=utf-8");
//            response.getWriter().write(JSONUtil.toJsonStr(RspResult.FAILED));
//            return false;
//        }
//        //如果没有：走原始方式
//        return super.onAccessDenied(request, response);
        //构建错误的响应信息
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(JSONUtil.toJsonStr(RspResult.NOLOGIN));
        return false;
    }
}
