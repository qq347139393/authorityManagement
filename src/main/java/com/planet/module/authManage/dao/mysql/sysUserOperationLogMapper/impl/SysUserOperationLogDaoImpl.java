package com.planet.module.authManage.dao.mysql.sysUserOperationLogMapper.impl;

import com.planet.module.authManage.dao.mysql.sysUserOperationLogMapper.SysUserOperationLogDao;
import org.springframework.stereotype.Repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public class SysUserOperationLogDaoImpl implements SysUserOperationLogDao {


    @Override
    public List selectNamesByIds(List<Long> ids, Object infoDaoBean) {
        try {
            //已经mybatisplus的根据多个id查询记录的通用方法名为:"selectBatchIds",入参为List<实体类id字段的类型>,返回值为List<T>
            Class<?> aClass = infoDaoBean.getClass();
            Method selectBatchIds = aClass.getMethod("selectBatchIds", Collection.class);
            List list = (List)selectBatchIds.invoke(infoDaoBean, ids);

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("根据ids获取info表的记录失败..");
        }
    }

    @Override
    public boolean saveBatch(List logs, Object logDaoBean, int batchSize) {
        try {
            Method saveBatchMethod = logDaoBean.getClass().getMethod("saveBatch", Collection.class, int.class);
            Object result = saveBatchMethod.invoke(logDaoBean, logs, batchSize);
            if(result!=null){
                return (boolean)result;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("向指定的log表中新增log记录失败..");
        }

    }


}
