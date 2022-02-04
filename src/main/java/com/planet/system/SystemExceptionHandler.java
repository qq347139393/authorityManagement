package com.planet.system;

import com.planet.common.util.RspResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 系统异常统一处理
 */
@ControllerAdvice
@Slf4j
public class SystemExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public RspResult errorHandler(Exception e) {
        log.error(e.getMessage());
//        Map map = new HashMap();
//        map.put("code", 400);
//        //判断异常的类型,返回不一样的返回值
//        if(ex instanceof MissingServletRequestParameterException){
//            map.put("msg","缺少必需参数："+((MissingServletRequestParameterException) ex).getParameterName());
//        }
//        else if(ex instanceof MyException){
//            map.put("msg","这是自定义异常");
//        }
        //判断异常的类型,返回不一样的返回值
        if(e instanceof MethodArgumentNotValidException){
            //如果是前端传入的参数错误,返回对应的错误信息
            log.error("前端传入的参数与实体类dto不匹配");
            return RspResult.FRONT_END_PARAMETER_ERROR;
        }


        return RspResult.SYS_ERROR;
    }

}
