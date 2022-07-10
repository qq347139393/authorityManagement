package com.planet.module.authManage.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.planet.common.base.BaseControllerImpl;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.service.FunctionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 权限-权限功能-信息表 前端控制器
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@RestController
@RequestMapping("/authManage/function-info")
public class FunctionInfoController extends BaseControllerImpl<FunctionInfoService, FunctionInfo> {
    @Autowired
    private FunctionInfoService functionInfoService;

    /**
     * 基础的新增一条或多条记录
     * @param list
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @Override
    public RspResult inserts(@RequestBody List<FunctionInfo> list) {
        if(list==null&&list.size()<=0){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult = functionInfoService.inserts(list);
        if(rspResult!=null){
            return rspResult;
        }
        return RspResult.SYS_ERROR;
    }

    /**
     * 基础的根据ids修改一条或多条记录
     * @param list
     * @return
     */
    @RequestMapping(value ="/",method = RequestMethod.PUT)
    @Override
    public RspResult updatesByIds(@RequestBody List<FunctionInfo> list) {
        if(list==null&&list.size()<=0){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult = functionInfoService.updatesByIds(list);
        if(rspResult!=null){
            return rspResult;
        }
        return RspResult.SYS_ERROR;
    }

    /**
     * 基础的根据ids删除一条或多条记录
     * @param ids
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.DELETE)
    @Override
    public RspResult deletesByIds(@RequestParam List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult = functionInfoService.deletesByIds(ids);
        if(rspResult!=null){
            return rspResult;
        }
        return RspResult.SYS_ERROR;
    }


    @RequestMapping(value ="/byPageOfTree",method = RequestMethod.POST)
    public RspResult selectsByTreeByPage(@RequestBody FunctionInfo t){
        if(t.getCurrent()==null||t.getCurrent()<0||t.getSize()==null||t.getSize()<=0){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult = functionInfoService.selectsByTreeByPage(t);
        if(rspResult!=null){
            return rspResult;
        }
        return RspResult.SYS_ERROR;
    }

    /**
     * 基础的根据ids查询一条或多条记录
     * @param ids
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
    @Override
    public RspResult selectsByIds(@RequestParam List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.PAPAMETER_ERROR;
        }

        RspResult rspResult = functionInfoService.selectsByIds(ids);
        if(rspResult!=null){
            return rspResult;
        }
        return RspResult.SYS_ERROR;
    }

    /**
     * excel导入记录
     * @param excelFile
     * @return
     */
    @RequestMapping(value = "/excelImport",method = RequestMethod.POST)
    public RspResult excelImport(@RequestParam("excelFile") MultipartFile excelFile){
        if(excelFile==null){
            return RspResult.PAPAMETER_ERROR;
        }
        RspResult rspResult = functionInfoService.excelImport(excelFile);
        if(rspResult==null){//发生了异常
            return RspResult.SYS_ERROR;
        }
        return rspResult;
    }

    /**
     * excel导出文件
     //     * @param t
     * @return
     */
    @RequestMapping(value = "/excelExport",method = RequestMethod.POST)
    public void excelExport(@RequestBody FunctionInfo t){
        if(t==null){
            return;
        }

        functionInfoService.excelExport(t);
    }

}

