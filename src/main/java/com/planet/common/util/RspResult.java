package com.planet.common.util;



public class RspResult<T> {
    public static final RspResult SUCCESS=new RspResult(RspResultCode.SUCCESS);
    public static final RspResult FAILED=new RspResult(RspResultCode.FAILED);
    public static final RspResult URL_ERROR=new RspResult(RspResultCode.URL_ERROR);
    public static final RspResult NOLOGIN=new RspResult(RspResultCode.NOLOGIN);
    public static final RspResult NOFUNCTION=new RspResult(RspResultCode.NOFUNCTION);
    public static final RspResult SUPER_PROHIBIT=new RspResult(RspResultCode.SUPER_PROHIBIT);
    public static final RspResult SYS_ERROR=new RspResult(RspResultCode.SYS_ERROR);
    public static final RspResult FRONT_END_PARAMETER_ERROR=new RspResult(RspResultCode.FRONT_END_PARAMETER_ERROR);

    private String msg;
    private String code;
    private T data;

    public RspResult(){
    }
    public RspResult(RspResultCode rspResultCode){
        this.msg=rspResultCode.getMsg();
        this.code=rspResultCode.getCode();
    }
    /** 使用此方法表示成功,并有响应实体数据 */
    public RspResult(T data){
        this.msg=SUCCESS.getMsg();
        this.code=SUCCESS.getCode();
        this.data=data;
    }

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
