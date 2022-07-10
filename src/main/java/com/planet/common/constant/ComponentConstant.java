package com.planet.common.constant;

public class ComponentConstant {
//    /**
//     * 登录密码输入错误的重试次数(从0开始计)+
//     */
//    public static final Long RETRY_LIMIT_NUM=4l;
//    /**
//     * 密码输入次数超过指定限制后的等待时间,以分为单位+
//     */
//    public static final Long RETRY_LIMIT_EXCEED_WAIT_TIME=10l;

    /**
     * 用户登录次数的缓存key的前缀
     */
    public static final String USER_LOGIN_FREQUENCY="userLoginFrequency:";

    /**
     * Swagger2的基础路径
     * 要与当前项目设置的静态资源路径相匹配:web.static-path-pattern: /file/**
     */
    public static final String SWAGGER2_DEFAULT_PATH = "/file/swagger2";

//    /**
//     * 设置DAO每次批量操作记录条数+
//     * 多地方用的,直接在项目运行时加载
//     */
//    public static int DAO_BATCH_SIZE;


}
