package com.planet.module.authManage.dao.mysql.sysUserOperationLogMapper;

import java.util.List;
import java.util.Map;

/**
 * 对用户操作记录进行持久化的Dao
 */
public interface SysUserOperationLogDao {
    /**
     * 根据传入的id集合和log表名,获取对应的主体名
     * @param ids 主体id
     * @param infoDaoBean 是执行Dao操作的对象:由于我们项目用的是springboot,所以这里最好使用spring管理的那个单例的Dao对象而不要用反射新建一个
     * @return
     */
    List selectNamesByIds(List<Long> ids, Object infoDaoBean);

    /**
     * 向指定的log表中新增log记录
     * @param logs
     * @param logDaoBean
     * @param batchSize
     * @return
     */
    boolean saveBatch(List logs,Object logDaoBean,int batchSize);

}
