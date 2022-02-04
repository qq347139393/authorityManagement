package com.planet.system;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Web工具类
 *
 * @author xu_lw
 * @version 5.0.0
 * created  at 2020-06-19 18:55
 * copyright @2018 北京沐融信息科技股份有限公司
 */
public class WebUtil {

	/**
	 * HttpSession
	 *
	 * @return
	 */
	public static HttpSession getSession(boolean flag) {
		HttpServletRequest httpServletRequest = getRequest();
		if (httpServletRequest == null) {
			return null;
		}
		return httpServletRequest.getSession(flag);
	}

	/**
	 * HttpSession
	 *
	 * @return
	 */
	public static HttpSession getSession() {
		return getSession(true);
	}

	/**
	 * 获取HttpServletRequest
	 *
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (servletRequestAttributes == null) {
			return null;
		}
		return servletRequestAttributes.getRequest();
	}

	/**
	 * 获取HttpServletResponse
	 *
	 * @return
	 */
	public static HttpServletResponse getResponse() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (servletRequestAttributes == null) {
			return null;
		}
		return servletRequestAttributes.getResponse();
	}

	/**
	 * 根据给定的key值获取当前请求线路中的session的value
	 * @param key
	 * @return
	 */
	public static Object getSessionValueByKey(String key){
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession().getAttribute(key);
	}


}
