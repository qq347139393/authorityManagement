package com.planet.common.util;

public enum RspResultCode {
    SUCCESS("000000","success"),
    FAILED("000001","failed"),
    URL_ERROR("000002","url is error"),
    NOLOGIN("000003","not login"),
    NOFUNCTION("000004","no have function"),
    SUPER_PROHIBIT("000005","super role and user prohibit operation"),
    SYS_ERROR("000006","system error"),
    FRONT_END_PARAMETER_ERROR("000007","frontEnd's parameters is not match"),

    PAPAMETER_ERROR("000008","parameter is null"),
    VERIFICATION_CODE_ERROR("000009","verification code is error"),
    USER_NULL("000010","user is null") ,
    SELECT_NULL("000011","select result is null"),
    EXCEL_IMPORT_ERROR("000012","excel import is error"),
    FIELDS_REPEAT_ERROR("000013","field values repeat error"),
    UPDATE_SHIRO_PERMISSIONS_FAILED("000014","update shiro permissions failed"),
    PASSWORD_WRONG("000015","submit password is wrong"),
    ACCOUNT_NON_EXISTENT("000016","account is not exist"),
    JWT_TOKEN_EMBEZZLE("000017","jwtToken is embezzled");

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
