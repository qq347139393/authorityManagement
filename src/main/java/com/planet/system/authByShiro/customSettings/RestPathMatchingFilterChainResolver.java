package com.planet.system.authByShiro.customSettings;

import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Locale;

public class RestPathMatchingFilterChainResolver extends PathMatchingFilterChainResolver {

    private static final Logger log = LoggerFactory.getLogger(RestPathMatchingFilterChainResolver.class);

    @Override
    public FilterChain getChain(ServletRequest request, ServletResponse response, FilterChain originalChain) {
        // 1. 判断有没有配置过滤器链, 没有一个过滤器都没有则直接返回 null
        FilterChainManager filterChainManager = getFilterChainManager();
        if (!filterChainManager.hasChains()) {
            return null;
        }
        // 2. 获取当前请求的 URL
        String requestURI = getPathWithinApplication(request);
//        //如果uri的最后带有整数值,说明是我们的id,则这时我们这里要刻意将其去掉以便进行比照;否则比照一定失败
//        String substring = requestURI.substring(requestURI.lastIndexOf("/") + 1);
//        try {
//            if(substring!=null&&!"".equals(substring)){//尝试强制成Long值
//                Long.valueOf(substring);//如果没有报错,说明是Long值
//                requestURI=requestURI.replace(substring,"");
//            }else{
//                log.info("该uri的结尾为null,不需要拆分");
//            }
//        }catch (Exception e){
//            log.info("该uri的结尾不是Long值,不需要拆分");
//        }

        // 3. 遍历所有的过滤器链:仅对restful请求的url部分进行匹配
        // the 'chain names' in this implementation are actually path patterns defined by the user.  We just use them
        // as the chain name for the FilterChainManager's requirements
        for (String pathPattern : filterChainManager.getChainNames()) {

            String[] pathPatternArray = pathPattern.split("==");

            boolean httpMethodMatchFlag = true;

            if (pathPatternArray.length > 1) {//判断请求方式是否一致
//                String a=pathPatternArray[1];
//                String b=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getMethod().toLowerCase();

                httpMethodMatchFlag = pathPatternArray[1].equals(
                        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getMethod().toLowerCase());
            }

            // 只用过滤器链的 URL 部分与请求的 URL 进行匹配
            if (pathMatches(pathPatternArray[0], requestURI) && httpMethodMatchFlag) {
                if (log.isTraceEnabled()) {
                    log.trace("Matched path pattern [" + pathPattern + "] for requestURI [" + requestURI + "].  " +
                            "Utilizing corresponding filter chain...");
                }
                return filterChainManager.proxy(originalChain, pathPattern);
            }
        }

        return null;
    }
}
