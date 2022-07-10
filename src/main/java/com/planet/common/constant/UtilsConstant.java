package com.planet.common.constant;

/**
 * 工具类用到的常量
 */
public class UtilsConstant {
    /**
     * 给redis序列化用的默认字符集
     */
    public static final String REDIS_CHARACTER_SET = "ISO-8859-1";
    /**
     * redis的userInfo的key的父级名
     */
    public static final String REDIS_USER_ID_FOR_USER_INFO="userInfo:";
    /**
     * redis的userRolesPermits的key的父级名
     */
    public static final String REDIS_USER_ID_FOR_ROLES_PERMITS="userRolesPermits:";
    /**
     * redis的userFunctionsPermits的key的父级名
     */
    public static final String REDIS_USER_ID_FOR_FUNCTIONS_PERMITS="userFunctionsPermits:";
//    /**
//     * redis存键值对的默认过期时间+
//     * 这种用的地方多的静态变量,直接初始化的时候就加载好
//     */
//    public static Long TTL_REDIS_DAO_MILLISECOND;
//    /**
//     * jwt过期时间+
//     * 这种用的地方多的静态变量,直接初始化的时候就加载好
//     */
//    public static Long TTL_JWT_MILLISECOND;
    /**
     * JWT的签名秘钥
     */
    public static final String JWT_SIGNATURE="planet";
    /**
     * 存入redis缓存中的sessionId的前缀
     */
    public static final String SESSION_KEY="sessionKey:";
//    /**
//     * 存入redis缓存中的session的过期时间+
//     * 这种用的地方多的静态变量,直接初始化的时候就加载好
//     */
//    public static Long TTL_SESSION_MILLISECOND;
    /**
     * 用于[鉴证]的jwtToken的固定字段名
     */
    public static final String AUTH_TOKEN="jwtToken";
    /**
     * jwt的固定签发者:我们选用当前项目名缩写
     */
    public static final String JWT_ISS="auth";
    /**
     * 用户对应的sessionId的前缀
     */
    public static final String USER_SESSION_ID="userSessionId:";

}
