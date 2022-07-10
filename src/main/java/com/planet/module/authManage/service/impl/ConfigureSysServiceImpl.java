package com.planet.module.authManage.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.common.constant.ComponentConstant;
import com.planet.common.constant.LocalCacheConstantService;
import com.planet.common.constant.ServiceConstant;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.dao.mysql.mapper.ConfigureSysMapper;
import com.planet.module.authManage.entity.mysql.ConfigureSys;
import com.planet.module.authManage.service.ConfigureSysService;
import com.planet.module.authManage.service.springBootQuartzJob.SystemFilesCleanJob;
import com.planet.util.jdk8.mapAndEntityConvert.MapAndEntityConvertUtil;
import com.planet.util.springBoot.SpringBootQuartzManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 配置-系统表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@Service
public class ConfigureSysServiceImpl extends ServiceImpl<ConfigureSysMapper, ConfigureSys> implements ConfigureSysService {
    @Autowired
    private ConfigureSysMapper configureSysMapper;
    @Autowired
    private SpringBootQuartzManager springBootQuartzManager;
    @Override
    public RspResult inserts(List<ConfigureSys> list) {//如果有需要reload的配置项,需要单独写判断和reload代码
        list.stream().forEach(l->{
            ConfigureSys configureSys = LocalCacheConstantService.addKY(l);
            if(configureSys==null){
                throw new RuntimeException("新增配置失败,可能出现重复key的记录..");
            }
        });
        return RspResult.SUCCESS;
    }

    @Override
    public RspResult updatesByIds(List<ConfigureSys> list) {//如果有需要reload的配置项,需要单独写判断和reload代码
        list.stream().forEach(l->{
            ConfigureSys configureSys = configureSysMapper.selectById(l.getId());
            if(configureSys==null){
                throw new RuntimeException("要修改的配置项不存在,修改失败..");
            }
            String valueType = configureSys.getValueType();
            Class cls;
            if("Integer".equals(valueType)){
                cls=Integer.class;
            }else if("Long".equals(valueType)){
                cls=Long.class;
            }else if("Boolean".equals(valueType)){
                cls=Boolean.class;
            }else{//默认按String
                cls=String.class;
            }
            Object value = LocalCacheConstantService.setValue(l, cls);
            if(value==null){
                throw new RuntimeException("更新本地缓存出现异常,修改失败..");
            }
            if("scheduleTask:systemFilesCleanJob".equals(l.getConfKey())){//对systemFilesCleanJob定时任务进行定制化的reload实现
                String storeSystemFilesCleanJob = (String)value;
                JSONObject jsonObject= JSONUtil.parseObj(storeSystemFilesCleanJob);
                String systemFilesCleanJobName = jsonObject.get("systemFilesCleanJobName", String.class);
                String systemFilesCleanJobGroupName = jsonObject.get("systemFilesCleanJobGroupName", String.class);
                String systemFilesCleanJobCron = jsonObject.get("systemFilesCleanJobCron", String.class);
                boolean b = springBootQuartzManager.updateJobCron(systemFilesCleanJobName, systemFilesCleanJobGroupName, systemFilesCleanJobCron);
                if(!b){
                    throw new RuntimeException("定时任务更新失败..");
                }
            }
        });
        return RspResult.SUCCESS;
    }

    @Override
    public RspResult deletesByIds(List<Long> ids) {//如果有需要reload的配置项,需要单独写判断和reload代码
        ids.stream().forEach(id->{
            ConfigureSys configureSys = configureSysMapper.selectById(id);
            if(configureSys==null){
                throw new RuntimeException("要删除的配置项不存在,删除失败..");
            }
            configureSys = LocalCacheConstantService.removeKY(configureSys);
            if(configureSys==null){
                throw new RuntimeException("删除本地缓存的过程中出现异常,删除失败..");
            }
        });
        return RspResult.SUCCESS;
    }

    @Override
    public RspResult selectsByPage(ConfigureSys t) {
        Page<ConfigureSys> page = new Page(t.getCurrent(), t.getSize());
        //这里先做出基本的属性遍历查询,以后要加入模糊、大小等各种可能出现的情况的判断方式
        QueryWrapper<ConfigureSys> tQueryWrapper = new QueryWrapper<>();
        Map<String, Object> stringObjectMap = null;
        try {
            stringObjectMap = MapAndEntityConvertUtil.entityToMap(t, ServiceConstant.CLOSE,true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return RspResult.SYS_ERROR;
        }

        //将需要作为查询条件的字段装配到QueryWrapper上,用于拼装查询sql(目前我们只做等于这种,以后会改进成各种方式的)
        for (String key : stringObjectMap.keySet()) {
            if("size".equals(key)||"current".equals(key)){
                continue;
            }
            if("key".equals(key)){
                //标准模糊查询:这里的key存的是命名空间,采取左侧严格匹配右侧模糊查询的方式
                //tQueryWrapper.like("name", name).or().like("lastname", name)
                tQueryWrapper.likeRight("conf_key", stringObjectMap.get("key"));
                continue;
            }
            tQueryWrapper.eq(key,stringObjectMap.get(key));
        }

        IPage<ConfigureSys> pageData = page(page, tQueryWrapper.orderByAsc("id"));
        return new RspResult(pageData);
    }



}
