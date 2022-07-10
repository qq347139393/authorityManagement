package com.planet.system;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.planet.module.authManage.entity.redis.UserInfo;
import com.planet.util.shiro.ShiroUtil;
import com.planet.util.springBoot.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Date;

@Configuration
@EnableTransactionManagement
@MapperScan("com.planet.module.authManage.dao.mysql.mapper")
@Slf4j
public class MybatisPlusConfig {

    /**
     * 分页插件的配置
     * @return
     */
    @Bean("paginationInterceptor")
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 字段值的自动补充配置
     * @return
     */
    @Bean("metaObjectHandler")
    public MetaObjectHandler metaObjectHandler(){
        return new MetaObjectHandler() {
            // 插入时的填充策略
            @Override
            public void insertFill(MetaObject metaObject) {
                log.info("start insert fill.....");
                // setFieldValByName(String fieldName, Object fieldVal, MetaObject metaObject
                this.setFieldValByName("creatime",new Date(),metaObject);
                this.setFieldValByName("updatime",new Date(),metaObject);
                this.setFieldValByName("status",0,metaObject);
                this.setFieldValByName("deleted",0,metaObject);
                this.setFieldValByName("version",1l,metaObject);
                //creator由用户关理缓存中获取
                Object principal = ShiroUtil.getPrincipal();
                String name="";
                if(principal!=null){
                    name = JSONUtil.parseObj(JSONUtil.toJsonPrettyStr(principal)).get("name", String.class);
                }
                this.setFieldValByName("creator",name,metaObject);
                this.setFieldValByName("updator",name,metaObject);
            }
            // 更新时的填充策略
            @Override
            public void updateFill(MetaObject metaObject) {
                log.info("start update fill.....");
                this.setFieldValByName("updatime",new Date(),metaObject);
                //creator由用户关理缓存中获取
                Object principal = ShiroUtil.getPrincipal();
                String name="";
                if(principal!=null){
                    name = JSONUtil.parseObj(JSONUtil.toJsonPrettyStr(principal)).get("name", String.class);
                }
//                this.setFieldValByName("creator",name,metaObject);
                this.setFieldValByName("updator",name,metaObject);
            }
        };
    }

    // 注册乐观锁插件
    @Bean("optimisticLockerInterceptor")
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }


}
