package com.planet.system;

import com.planetProvide.easyExcelPlus.core.baseDao.BaseDao;
import com.planetProvide.easyExcelPlus.core.baseExcelImportValid.BaseExcelImportValid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * 我们的EasyExcelPlus框架的配置类
 */
@Configuration
@PropertySource("classpath:config/easyExcelPlus.yml")
public class EasyExcelPlusConfig {
    @Autowired
    private Environment env;

    @Bean("easyExcelPlusBaseDao")
//    @Scope("prototype")
    public BaseDao baseDao(){
        BaseDao baseDao=new BaseDao();
        baseDao.setServiceClassNameSuffix(env.getProperty("serviceClassNameSuffix"));
        baseDao.setServiceClassFullNamePrefix(env.getProperty("serviceClassFullNamePrefix"));
        baseDao.setModel(Integer.valueOf(env.getProperty("model")));
        baseDao.setInsertsMethod(env.getProperty("insertsMethod"));
        return baseDao;
    }

//    @Bean
//    @Scope("prototype")
//    public BaseReadListener baseReadListener(){
//        BaseReadListener baseReadListener=new BaseReadListener();
//        baseReadListener.setReadBatchCount(Long.valueOf(env.getProperty("readBatchCount")));
//        baseReadListener.setBaseDao(baseDao());
//        baseReadListener.setBaseExcelImportValid(excelImportValid());
//        return baseReadListener;
//    }

    @Bean("excelImportValid")
    public BaseExcelImportValid excelImportValid(){
        BaseExcelImportValid baseExcelImportValid=new BaseExcelImportValid();

        return baseExcelImportValid;
    }

}
