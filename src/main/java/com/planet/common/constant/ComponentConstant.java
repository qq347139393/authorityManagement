package com.planet.common.constant;

public class ComponentConstant {
    /**
     * 登录密码输入错误的重试次数(从0开始计)
     */
    public static final Long RETRY_LIMIT_NUM=4l;
    /**
     * 密码输入次数超过指定限制后的等待时间,以秒为单位
     */
    public static final Long RETRY_LIMIT_EXCEED_WAIT_TIME=10l;
    /**
     * 密码输入次数超过指定限制后的错误提示信息
     */
    public static final String RETRY_LIMIT_EXCEED_MSG="密码次数错误"+(RETRY_LIMIT_NUM+1)+"次，请"+RETRY_LIMIT_EXCEED_WAIT_TIME+"分钟后重试";
    /**
     * 用户登录次数的缓存key的前缀
     */
    public static final String USER_LOGIN_FREQUENCY="userLoginFrequency:";


}
