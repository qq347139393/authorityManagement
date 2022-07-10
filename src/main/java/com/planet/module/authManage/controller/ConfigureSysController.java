package com.planet.module.authManage.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.planet.common.base.BaseControllerImpl;
import com.planet.common.constant.ServiceConstant;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.ConfigureSys;
import com.planet.module.authManage.service.ConfigureSysService;
import com.planet.util.jdk8.mapAndEntityConvert.MapAndEntityConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 配置-系统表 前端控制器
 *
 * 系统配置里面的配置表,每个key都对应具体的某个业务逻辑,需要后端先把具体的业务逻辑的代码写好然后让用户在页面上配置key-value去对应上.
 * 所以,一方面创建键值对时key的值不能随便起,要对应具体的业务逻辑的约定;另一方面不能改key,因为改了就会将这个键值对配置切换到了其他的业务逻辑上面了;而value可以按照约定的业务要求来改动;
 * 还有就是,key不可重复,因为重复了会导致获取配置时不知道拿哪个的情况(当然可以做成类似css后面覆盖前面的效果,但是这只是一种优化策略而不是说key值在功能逻辑上是可以重复的)
 * </p>
 *
 * @author Planet
 * @since 2022-01-31
 */
@RestController
@RequestMapping("/authManage/configure-sys")
public class ConfigureSysController extends BaseControllerImpl<ConfigureSysService, ConfigureSys> {
    @Autowired
    private ConfigureSysService configureSysService;

    /**
     * 基础的新增一条或多条记录
     * 要修改对应业务模块中的静态变量的值
     * @param list
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @Override
    public RspResult inserts(@RequestBody List<ConfigureSys> list) {

        if(list==null&&list.size()<=0){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult=configureSysService.inserts(list);
        if(rspResult==null){
            return RspResult.SYS_ERROR;
        }
        return rspResult;
    }

    /**
     * 基础的根据ids修改一条或多条记录
     * 要修改对应业务模块中的静态变量的值
     * @param list
     * @return
     */
    @RequestMapping(value ="/",method = RequestMethod.PUT)
    @Override
    public RspResult updatesByIds(@RequestBody List<ConfigureSys> list) {
        if(list==null&&list.size()<=0){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult=configureSysService.updatesByIds(list);
        if(rspResult==null){
            return RspResult.SYS_ERROR;
        }
        return rspResult;
    }

    /**
     * 基础的根据ids删除一条或多条记录
     * 要修改对应业务模块中的静态变量的值
     * @param ids
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.DELETE)
    @Override
    public RspResult deletesByIds(@RequestParam List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult=configureSysService.deletesByIds(ids);
        if(rspResult==null){
            return RspResult.SYS_ERROR;
        }
        return rspResult;
    }

    /**
     * 基础的根据给定的实体对象来分页查询多条记录
     * 要根据confKey中的":"来拆分模块进行查询(构成模糊查询)
     * @param t
     * @return
     */
    @RequestMapping(value = "/byPage",method = RequestMethod.POST)
    @Override
    public RspResult selectsByPage(@RequestBody ConfigureSys t) {
        if(t!=null){
            RspResult rspResult=configureSysService.selectsByPage(t);
            if(rspResult==null){
                return RspResult.SYS_ERROR;
            }
            return rspResult;
        }else{
            return RspResult.PAPAMETER_ERROR;
        }
    }

}

