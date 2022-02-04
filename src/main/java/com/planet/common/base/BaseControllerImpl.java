package com.planet.common.base;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.planet.common.util.RspResult;
import com.planet.util.jdk8.mapAndEntityConvert.MapAndEntityConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

/**
 * 基础Controller类
 */
public class BaseControllerImpl<M extends IService<T>,T extends BaseEntity> implements BaseController<T>{
    @Autowired
    protected M iService;

    /**
     * 基础的新增一条或多条记录
     * @param list
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @Override
    public RspResult inserts(@RequestBody List<T> list) {

        if(list==null&&list.size()<=0){
            return RspResult.FAILED;
        }else if(list.size()==1){
            boolean save = iService.save(list.get(0));
            return save==true?RspResult.SUCCESS:RspResult.FAILED;
        }else{
            boolean save = iService.saveBatch(list);
            return save==true?RspResult.SUCCESS:RspResult.FAILED;
        }
    }

    /**
     * 基础的根据ids修改一条或多条记录
     * @param list
     * @return
     */
    @RequestMapping(value ="/",method = RequestMethod.PUT)
    @Override
    public RspResult updatesByIds(@RequestBody List<T> list) {
        if(list==null&&list.size()<=0){
            return RspResult.FAILED;
        }else if(list.size()==1){
            boolean b = iService.updateById(list.get(0));
            return b==true?RspResult.SUCCESS:RspResult.FAILED;
        }else{
            boolean b = iService.updateBatchById(list);
            return b==true?RspResult.SUCCESS:RspResult.FAILED;
        }
    }

    /**
     * 基础的根据ids删除一条或多条记录
     * @param ids
     * @return
     */
    @RequestMapping(value = "/{ids}",method = RequestMethod.DELETE)
    @Override
    public RspResult deletesByIds(@PathVariable List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.FAILED;
        }else if(ids.size()==1){
            boolean b = iService.removeById((Long) ids.get(0));
            return b==true?RspResult.SUCCESS:RspResult.FAILED;
        }else{
            boolean b = iService.removeByIds(ids);
            return b==true?RspResult.SUCCESS:RspResult.FAILED;
        }
    }

    /**
     * 基础的根据ids查询一条或多条记录
     * @param ids
     * @return
     */
    @RequestMapping(value = "/{ids}",method = RequestMethod.GET)
    @Override
    public RspResult selectsByIds(@PathVariable List<Long> ids) {
        if(ids==null&&ids.size()<=0){
            return RspResult.FAILED;
        }else if(ids.size()==1){
            T byId = iService.getById(ids.get(0));
            return byId!=null?new RspResult(byId):RspResult.FAILED;
        }else{
            List<T> ts = (List<T>)iService.listByIds(ids);
            if(ts!=null&&ts.size()>0){
                return new RspResult(ts);
            }else{
                return RspResult.FAILED;
            }
        }
    }

    /**
     * 基础的根据给定的实体类的属性来查询一条或多条记录
     * @param t
     * @return
     */
    @RequestMapping(value = "/byList",method = RequestMethod.POST)
    @Override
    public RspResult selects(@RequestBody T t){
        if(t!=null){
            JSONObject json = JSONUtil.parseObj(t, true);

//            Page<T> page = new Page(t.getCurrent(), t.getSize());
            //这里先做出基本的属性遍历查询,以后要加入模糊、大小等各种可能出现的情况的判断方式
            QueryWrapper<T> tQueryWrapper = new QueryWrapper<>();
            Map<String, Object> stringObjectMap = null;
            try {
                stringObjectMap = MapAndEntityConvertUtil.entityToMap(t, "0",true);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return RspResult.SYS_ERROR;
            }

            //将需要作为查询条件的字段装配到QueryWrapper上,用于拼装查询sql(目前我们只做等于这种,以后会改进成各种方式的)
            for (String key : stringObjectMap.keySet()) {
                if("size".equals(key)||"current".equals(key)){
                    continue;
                }
                tQueryWrapper.eq(key,stringObjectMap.get(key));
            }

//            IPage<T> pageData = iService.page(page, tQueryWrapper.orderByAsc("id"));
            List<T> list=iService.list(tQueryWrapper.orderByAsc("id"));
            return new RspResult(list);
        }else{
            return RspResult.FAILED;
        }
    }

    /**
     * 基础的根据给定的实体对象来分页查询多条记录
     * @param t
     * @return
     */
    @RequestMapping(value = "/byPage",method = RequestMethod.POST)
    @Override
    public RspResult selectByPage(@RequestBody T t) {
        if(t!=null){
            JSONObject json = JSONUtil.parseObj(t, true);

            Page<T> page = new Page(t.getCurrent(), t.getSize());
            //这里先做出基本的属性遍历查询,以后要加入模糊、大小等各种可能出现的情况的判断方式
            QueryWrapper<T> tQueryWrapper = new QueryWrapper<>();
            Map<String, Object> stringObjectMap = null;
            try {
                stringObjectMap = MapAndEntityConvertUtil.entityToMap(t, "0",true);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return RspResult.SYS_ERROR;
            }

            //将需要作为查询条件的字段装配到QueryWrapper上,用于拼装查询sql(目前我们只做等于这种,以后会改进成各种方式的)
            for (String key : stringObjectMap.keySet()) {
                if("size".equals(key)||"current".equals(key)){
                    continue;
                }
                tQueryWrapper.eq(key,stringObjectMap.get(key));
            }

            IPage<T> pageData = iService.page(page, tQueryWrapper.orderByAsc("id"));
            return new RspResult(pageData);
        }else{
            return RspResult.FAILED;
        }
    }

}
