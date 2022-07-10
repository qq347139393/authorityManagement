package com.planet.common.constant;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.util.RspResult;
import com.planet.common.util.RspResultCode;
import com.planet.module.authManage.dao.mysql.mapper.ConfigureSysMapper;
import com.planet.module.authManage.entity.mysql.ConfigureSys;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.system.fieldsRepeatCheck.FieldsRepeatCheckResult;
import com.planet.system.fieldsRepeatCheck.FieldsRepeatCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 所有的需要动态配置的静态变量都放在这里
 * 这个后面可以优化成利用反射进行通用性处理的方式,甚至可以封装成一个独立的专门进行缓存的框架
 */
@Component("localCacheConstantService")
public class LocalCacheConstantService {
    LocalCacheConstantService(){}
    @Autowired
    private ConfigureSysMapper configureSysMapper;
    /** 存动态系统共享变量的集合 */
    private static volatile Map<String,Object> localCacheMap=new LinkedHashMap<>();

    /**
     * getter方法
     * @param key
     * @return
     */
    public static String getValue(String key){
        return getValue(key,String.class);
    }
    public static <T> T getValue(String key,Class<T> cls){
        if(StrUtil.isEmpty(key)||cls==null){
            throw new RuntimeException("key或类型不能为空..");
        }
        T t;
        synchronized (LocalCacheConstantService.class){
            //1.先从本地缓存中查找
            t = (T)localCacheMap.get(key);
            if(t!=null){//本地缓存中有,则直接返回数据
                return t;
            }
            //2.本地缓存中没有(可能是第一次加载),则去数据库查询
            LocalCacheConstantService service = SpringUtil.getBean("localCacheConstantService",LocalCacheConstantService.class);
            ConfigureSys configureSys = service.configureSysMapper.selectOne(new QueryWrapper<ConfigureSys>().eq("conf_key", key));
            if(configureSys==null){
                throw new RuntimeException("未找到指定key的配置,请重新检查原始配置或先新增对应key的配置项..");
            }
            t= castType(configureSys.getValue(),cls);//类型转换
            //3.存入缓存
            localCacheMap.put(key,t);
        }
        //返回value
        return t;
    }

    /**
     * setter方法
     * @param configureSys
     * @return
     */
    public static String setValue(ConfigureSys configureSys){
        return setValue(configureSys,String.class);
    }
    public static <T> T setValue(ConfigureSys configureSys,Class<T> cls){
        if(configureSys==null||StrUtil.isEmpty(configureSys.getConfKey())||cls==null){
            throw new RuntimeException("数据或类型不能为空..");
        }
        //1.先更新数据库
        LocalCacheConstantService service = SpringUtil.getBean("localCacheConstantService",LocalCacheConstantService.class);

        //字段重复性校验
        List<String> fieldNames=new ArrayList<>();
        fieldNames.add("conf_key");//conf_key字段不可重复
        fieldNames.add("code");//code字段不可重复
        FieldsRepeatCheckResult<ConfigureSys> fieldsRepeatCheckResult = FieldsRepeatCheckUtil.fieldsRepeatCheck(service.configureSysMapper, ServiceConstant.FIELDS_REPEAT_CHECK_METHOD, configureSys, fieldNames, FieldsRepeatCheckUtil.UPDATE);
        //只有一条记录,所以不用考虑分出合格不合格的记录
        if(fieldsRepeatCheckResult.getResult()){//出现字段值重复,禁止操作
            throw new RuntimeException("出现重复字段值的记录..");
        }

        int result = service.configureSysMapper.update(configureSys,new QueryWrapper<ConfigureSys>().
                eq("conf_key",configureSys.getConfKey()));
        if(result==0){//更新失败
            throw new RuntimeException("数据库中不存在当前key对应的配置项,更新失败..");
        }
        T t;
        //2.再更新本地缓存
        t=castType(configureSys.getValue(),cls);//类型转换
        synchronized (LocalCacheConstantService.class){
            localCacheMap.put(configureSys.getConfKey(),t);
        }
        //3.返回最新的值(如果返回null,则表示更新失败)
        return t;
    }

    /**
     * adder方法
     * @param configureSys
     * @return
     */
    public static ConfigureSys addKY(ConfigureSys configureSys){
        return addKY(configureSys,String.class);
    }
    public static <T> ConfigureSys addKY(ConfigureSys configureSys,Class<T> cls){
        if(configureSys==null||StrUtil.isEmpty(configureSys.getConfKey())||cls==null){
            throw new RuntimeException("数据或类型不能为空..");
        }
        //1.先新增数据库的记录
        LocalCacheConstantService service = SpringUtil.getBean("localCacheConstantService",LocalCacheConstantService.class);

        //字段重复性校验
        List<String> fieldNames=new ArrayList<>();
        fieldNames.add("conf_key");//conf_key字段不可重复
        fieldNames.add("code");//code字段不可重复
        FieldsRepeatCheckResult<ConfigureSys> fieldsRepeatCheckResult = FieldsRepeatCheckUtil.fieldsRepeatCheck(service.configureSysMapper, ServiceConstant.FIELDS_REPEAT_CHECK_METHOD, configureSys, fieldNames, FieldsRepeatCheckUtil.INSERT);
        //只有一条记录,所以不用考虑分出合格不合格的记录
        if(fieldsRepeatCheckResult.getResult()){//出现字段值重复,禁止操作
            throw new RuntimeException("出现重复字段值的记录..");
        }

        int result = service.configureSysMapper.insert(configureSys);
        //字段重复性校验放在aop中处理..
        if(result==0){//新增失败
            throw new RuntimeException("数据库出现问题,新增失败..");
        }
        //2.再新增本地缓存的键值对
        T t=castType(configureSys.getValue(),cls);//类型转换
        synchronized (LocalCacheConstantService.class){
            localCacheMap.put(configureSys.getConfKey(),t);
        }
        //3.返回新增的记录
        return configureSys;
    }

    /**
     * remover方法
     * @param configureSys
     * @param
     * @return
     */
    public static ConfigureSys removeKY(ConfigureSys configureSys){
        if(configureSys==null||StrUtil.isEmpty(configureSys.getConfKey())){
            throw new RuntimeException("数据或类型不能为空..");
        }
        //1.先删除数据库的记录
        LocalCacheConstantService service = SpringUtil.getBean("localCacheConstantService",LocalCacheConstantService.class);
        int result = service.configureSysMapper.delete(new QueryWrapper<ConfigureSys>().eq("conf_key",configureSys.getConfKey()));
        if(result==0){//删除失败
            throw new RuntimeException("数据库没有对应的记录,删除失败..");
        }
        synchronized (LocalCacheConstantService.class){
            //2.再删除本地缓存的键值对
            localCacheMap.remove(configureSys.getConfKey());
        }
        //3.返回删除的记录
        return configureSys;
    }

    //************本类的私有方法************

    /**
     * 类型转换的方法
     * @param value
     * @param cls
     * @param <T>
     * @return
     */
    private static <T> T castType(String value,Class<T> cls){
        if(StrUtil.isEmpty(value)||cls==null){
            throw new RuntimeException("value或cls不能为空,数据类型转换失败..");
        }
        if(cls==Integer.class){
            return cls.cast(Integer.valueOf(value));
        }else if(cls==Long.class){
            return cls.cast(Long.valueOf(value));
        }else if(cls==Double.class){
            return cls.cast(Double.valueOf(value));
        }else if(cls==Boolean.class){
            return cls.cast(Boolean.valueOf(value));
        }else{//字符串
            return cls.cast(value);
        }
    }


}
