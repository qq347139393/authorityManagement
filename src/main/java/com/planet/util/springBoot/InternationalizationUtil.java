package com.planet.util.springBoot;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 后端国际化工具类
 */
public class InternationalizationUtil {
    /** 国际化配置文件的路径 */
    private static final String PATH="i18n/internationalization";
    /** 默认的地区-语言 */
    private static final String DEFAULT_LOCAL_LANG="zh_CN";
    /**
     * 根据指定的key获取翻译后对应的value
     * @param key
     * @return
     */
    public static String getValue(String key){
        ResourceBundle rb=getResourceBundle();
        return rb.getString(key);
    }

    /**
     * 获取翻译后的全部key-values
     * @return
     */
    public static Map<String,Object> getAllKeyValues(){
        ResourceBundle rb=getResourceBundle();
        Enumeration<String> es=rb.getKeys();
        ArrayList<String> keyList = Collections.list(es);
        Map<String,Object> map=new HashMap<>();
        for (String key:keyList) {
            map.put(key,rb.getString(key));
        }
        return map;
    }

    //************下面的方法为本类私有的工具方法**************
    //通过请求头的LOCAL_LANG字段的值,获取指定的ResourceBundle对象
    private static ResourceBundle getResourceBundle(){
        //1.从请求头中获取语言-区域的信息字符串
        RequestAttributes requestAttributes= RequestContextHolder.getRequestAttributes();
        HttpServletRequest request=null;
        String lange = null;
        if(requestAttributes!=null){
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if(request!=null){
                RequestContextHolder.currentRequestAttributes();
                lange = (String)request.getHeader("LOCAL_LANG");
            }
        }
        if(lange==null||"".equals(lange)){
            lange = DEFAULT_LOCAL_LANG;//默认值
        }
        //2.创建Locale对象，将获取到的请求头中key为LOCAL_LANG对应的value值根据指定的规则解析成创建Locale的构造器中的入参
        Locale locale = new Locale(lange.split("_")[0],lange.split("_")[1]);
        //3.创建ResourceBundle对象
        ResourceBundle rb = ResourceBundle.getBundle(PATH, locale);
        return rb;
    }

    public static void main(String[] args) {
        System.out.println(getValue("login.password"));
    }
}
