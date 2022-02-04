package com.planet.common.util;

public enum RspResultCode {
    SUCCESS("000000","success"),
    FAILED("000001","failed"),
    URL_ERROR("000002","url is error"),
    NOLOGIN("000003","not login"),
    NOFUNCTION("000004","no have function"),
    SUPER_PROHIBIT("000005","super role and user prohibit operation"),
    SYS_ERROR("000006","system error"),
    FRONT_END_PARAMETER_ERROR("000007","frontEnd's parameters is not match");

    /**
     * 错误码
     */
    private String code;
    /**
     * 错误信息
     */
    private String msg;

    RspResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public String getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }

}
