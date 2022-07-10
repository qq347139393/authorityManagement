package com.planet.common.util;


import com.planet.util.springBoot.InternationalizationUtil;

public class RspResult<T> {
    public static final RspResult SUCCESS=new RspResult(RspResultCode.SUCCESS);
    public static final RspResult FAILED=new RspResult(RspResultCode.FAILED);
    public static final RspResult URL_ERROR=new RspResult(RspResultCode.URL_ERROR);
    public static final RspResult NOLOGIN=new RspResult(RspResultCode.NOLOGIN);
    public static final RspResult NOFUNCTION=new RspResult(RspResultCode.NOFUNCTION);
    public static final RspResult SUPER_PROHIBIT=new RspResult(RspResultCode.SUPER_PROHIBIT);
    public static final RspResult SYS_ERROR=new RspResult(RspResultCode.SYS_ERROR);
    public static final RspResult FRONT_END_PARAMETER_ERROR=new RspResult(RspResultCode.FRONT_END_PARAMETER_ERROR);

    public static final RspResult PAPAMETER_ERROR=new RspResult(RspResultCode.PAPAMETER_ERROR);
    public static final RspResult VERIFICATION_CODE_ERROR=new RspResult(RspResultCode.VERIFICATION_CODE_ERROR);
    public static final RspResult USER_NULL=new RspResult(RspResultCode.USER_NULL);
    public static final RspResult SELECT_NULL=new RspResult(RspResultCode.SELECT_NULL);
    public static final RspResult EXCEL_IMPORT_ERROR=new RspResult(RspResultCode.EXCEL_IMPORT_ERROR);
    public static final RspResult FIELDS_REPEAT_ERROR=new RspResult(RspResultCode.FIELDS_REPEAT_ERROR);
    public static final RspResult UPDATE_SHIRO_PERMISSIONS_FAILED=new RspResult(RspResultCode.UPDATE_SHIRO_PERMISSIONS_FAILED);
    public static final RspResult PASSWORD_WRONG=new RspResult(RspResultCode.PASSWORD_WRONG);
    public static final RspResult ACCOUNT_NON_EXISTENT=new RspResult(RspResultCode.ACCOUNT_NON_EXISTENT);
    public static final RspResult JWT_TOKEN_EMBEZZLE=new RspResult(RspResultCode.JWT_TOKEN_EMBEZZLE);

    private String msg;
    private String code;
    private T data;

    public RspResult(){
    }
    public RspResult(RspResultCode rspResultCode){
        this.msg=InternationalizationUtil.getValue(rspResultCode.getCode());
        this.code=rspResultCode.getCode();
    }
    public RspResult(RspResultCode rspResultCode,T data){
        this.msg=InternationalizationUtil.getValue(rspResultCode.getCode());
        this.code=rspResultCode.getCode();
        this.data=data;
    }
    /** 使用此方法表示成功,并有响应实体数据 */
    public RspResult(T data){
//        this.msg=SUCCESS.getMsg();
        this.msg=InternationalizationUtil.getValue(SUCCESS.getCode());
        this.code=SUCCESS.getCode();
        this.data=data;
    }

    //本项目的code和msg用来在国际化的配置中充当key和value..所以每个新的msg都得对应新的code
    public RspResult(String msg,String code,T data){
        this.msg=msg;
        this.code=code;
        this.data=data;
    }

    public String getMsg() {
        //在返回值中进行国际化翻译
//        msg= InternationalizationUtil.getValue(getCode());
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RspResult{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                ", data=" + data +
                '}';
    }
}
