package com.planet.common.constant;

public class ServiceConstant {
    /**
     * 用户二维码的宽度px
     */
    public static final int USER_QRCODE_WIDTH=300;
    /**
     * 用户二维码的高度px
     */
    public static final int USER_QRCODE_HEIGHT=300;

    /**
     * 常用的业务状态,表示打开
     */
    public static final int OPEN=1;
    /**
     * 常用的业务状态,表示关闭
     */
    public static final int CLOSE=0;

    /**
     * 字段重复性校验使用的约定方法(MybatisPlus中提供的一个查询全部记录的方法)
     */
    public static final String FIELDS_REPEAT_CHECK_METHOD="selectList";

//    /**
//     * 活跃用户默认的门槛值+
//     * 这种用的地方多的静态变量,直接初始化的时候就加载好
//     */
//    public static long ACTIVE_USER_THRESHOLD;

//    /**
//     * 用户头像图片文件的默认路径+
//     */
//    public static final String USER_PORTRAIT_DEFAULT_URL="{'userPortraitDefaultUrl':'/configureSystem/默认图片测试.jpg'}";


}
