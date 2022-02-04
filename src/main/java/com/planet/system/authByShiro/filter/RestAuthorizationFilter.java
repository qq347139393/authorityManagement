package com.planet.system.authByShiro.filter;

import cn.hutool.json.JSONUtil;
import com.planet.common.util.RspResult;
import com.planet.util.JwtUtil;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 修改后的 perms 过滤器, 添加对 AJAX 请求的支持.
 */
public class RestAuthorizationFilter extends PermissionsAuthorizationFilter {

    private static final Logger log = LoggerFactory
            .getLogger(RestAuthorizationFilter.class);

    /**
     * @Description 是否允许访问
     */
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
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

    //将当前请求的 url 与所有配置的 perms 过滤器链进行匹配，是则进行权限检查，不是则接着与下一个过滤器链进行匹配
    @Override
    protected boolean pathsMatch(String path, ServletRequest request) {
        boolean flag;
        String requestURI = this.getPathWithinApplication(request);

        String[] strings = path.split("==");

        if (strings.length <= 1) {
            // 普通的 URL, 正常处理
            flag =  this.pathsMatch(strings[0], requestURI);
        } else {
            // 获取当前请求的 http method.
            String httpMethod = WebUtils.toHttp(request).getMethod().toUpperCase();

            // 匹配当前请求的method和url与过滤器链中的method和url是否一致
            flag =  httpMethod.equals(strings[1].toUpperCase()) && this.pathsMatch(strings[0], requestURI);
        }

        if (flag) {
            log.debug("URL : [{}] matching perms filter : [{}]", requestURI, path);
        }
        return flag;
    }

    /**
     * 当前请求如果没有权限而被拦截时:
     *          如果是 AJAX 请求, 则返回 JSON 数据.
     *          如果是普通请求, 则跳转到配置 UnauthorizedUrl 页面.
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        Subject subject = getSubject(request, response);
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (subject.getPrincipal() == null) {
            // 如果未登录 --后面再优化
            // AJAX 请求返回 JSON
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(JSONUtil.toJsonStr(RspResult.FAILED));
            return false;
        } else {

            // 如果已登陆, 但没有权限 --后面再优化
            // 对于 AJAX 请求返回 JSON
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(JSONUtil.toJsonStr(RspResult.FAILED));
            return false;
        }
    }
}
