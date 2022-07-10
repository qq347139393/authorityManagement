package com.planet.module.authManage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planet.common.constant.LocalCacheConstantService;
import com.planet.common.constant.ServiceConstant;
import com.planet.common.constant.UtilsConstant;
import com.planet.common.util.RspResult;
import com.planet.common.util.RspResultCode;
import com.planet.module.authManage.converter.easyExcelPlus.FunctionInfoExcelToPoConverter;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.system.fieldsRepeatCheck.FieldsRepeatCheckResult;
import com.planet.system.fieldsRepeatCheck.FieldsRepeatCheckUtil;
import com.planet.system.sysUserOperationLog.annotation.SysUserOperationMethodLog;
import com.planet.system.sysUserOperationLog.enumeration.MethodType;
import com.planet.system.sysUserOperationLog.enumeration.ParameterType;
import com.planet.util.jdk8.PageManager;
import com.planet.util.jdk8.TreeStructuresUtil;
import com.planet.module.authManage.dao.mysql.mapper.FunctionInfoMapper;
import com.planet.module.authManage.dao.mysql.mapper.RoleFunctionRsMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserInfoMapper;
import com.planet.module.authManage.dao.redis.BaseMapper;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.entity.redis.UserFunctionRs;
import com.planet.module.authManage.service.FunctionInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.module.authManage.service.authByShiro.ShiroService;
import com.planet.util.jdk8.mapAndEntityConvert.MapAndEntityConvertUtil;
import com.planet.util.springBoot.WebUtil;
import com.planetProvide.easyExcelPlus.core.baseDao.BaseDao;
import com.planetProvide.easyExcelPlus.core.baseExcelImportValid.BaseExcelImportValid;
import com.planetProvide.easyExcelPlus.core.baseReadListener.BaseRowReadListener;
import com.planetProvide.easyExcelPlus.core.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 权限功能-信息表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
@Slf4j
public class FunctionInfoServiceImpl extends ServiceImpl<FunctionInfoMapper, FunctionInfo> implements FunctionInfoService {
    @Autowired
    private ShiroService shiroService;
    @Autowired
    private FunctionInfoMapper functionInfoMapper;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RoleFunctionRsMapper roleFunctionRsMapper;

    @Value("${readBatchCount}") //100
    private long readBatchCount;
    @Autowired
    private BaseExcelImportValid<com.planet.module.authManage.entity.excel.FunctionInfo> baseExcelImportValid;
    @Autowired
    private BaseDao<com.planet.module.authManage.entity.excel.FunctionInfo> baseDao;
    @Autowired
    private FunctionInfoExcelToPoConverter functionInfoExcelToPoConverter;

    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.INSERT,parameterType= ParameterType.List)
    public RspResult inserts(List<FunctionInfo> list) {
        //字段重复性校验
        List<String> fieldNames=new ArrayList<>();
        fieldNames.add("name");//name字段不可重复
        fieldNames.add("code");//code字段不可重复
        List<FieldsRepeatCheckResult<FunctionInfo>> results = FieldsRepeatCheckUtil.fieldsRepeatChecks(functionInfoMapper, ServiceConstant.FIELDS_REPEAT_CHECK_METHOD, list, fieldNames, FieldsRepeatCheckUtil.INSERT);
        //拿出可以执行的部分进行执行
        List<FunctionInfo> rightList = results.stream().filter(r -> r.getResult().equals(false)).map(r -> r.getData()).collect(Collectors.toList());
        //拿出字段重复的部分返回给前端
        List<FieldsRepeatCheckResult<FunctionInfo>> errorResults = results.stream().filter(r -> r.getResult().equals(true)).collect(Collectors.toList());

        //1.判断新增的权限是否符合系统要求
        //如果新增的是顶级[菜单]或游离[按钮]:则顶级菜单在前端展示的时候直接是放导航栏的最外层;游离按钮在前端展示的时候直接无视掉(但有权限的用户可以通过盗链的方式访问系统)
        //-如果是[菜单],则需要根据其routeOrder的值来进行排序并重改当前同父元素的后续菜单的顺序(比如routeOrder的值为1,则后面1,2,3的菜单都要往后挪1位)
        //-如果是[按钮],则其routeOrder的值前端直接无视掉即可(后续如果类型转成菜单的话,这个值才会起作用);或者设计成前端按钮的插槽排序,我们需要后面做的时候再具体分析和确定
        //并且[菜单]和[按钮]都不能挂载到[按钮]上
        rightList.stream().forEach(l->{
            //进行判断和稳定性设置
            Integer routeOrder = l.getRouteOrder();
            if(routeOrder==null||routeOrder<0){//按0处理
                l.setRouteOrder(0);
            }
            Long parentId = l.getParentId();//按0处理:顶级菜单或游离按钮
            if(parentId==null||parentId<=0){
                l.setParentId(0l);
            }else{//要判断当前元素指定的父级元素是否为[按钮],如果是按钮则禁止本次的新增操作:因为按钮是不能被挂载的
                //判断指定的父级元素是否存在
                FunctionInfo parentFunction = getById(parentId);
                if(parentFunction==null){
                    throw new RuntimeException("指定的父级元素不存在");
                }
                if(parentFunction.getType()==1){
                    throw new RuntimeException("指定的父级元素为按钮");
                }
            }
            Integer type = l.getType();
            if(type==null||(type!=0&&type!=1)){//按0处理:菜单
                l.setType(0);
            }

            //2.如果符合系统要求并且它是[菜单],则计算其routeOrder与跟它同父元素的其他[菜单]元素的routeOrder值的对比:
            //如果routeOrder小于0,则变为0并让其作为同父元素的第一个序列位置,其他的元素的routeOrder值往后挪1位
            //如果routeOrder大于同父元素中的最大的那个元素的routeOrder值超过1,则新增[菜单]会排在最后并且将routeOrder改为最大的那个已有的值+1
            //如果routeOrder处于同父元素中的中间位置,则将跟它routeOrder值相等和后面更大routeOrder值的元素统统往后挪1位
            if(l.getType()==0){//菜单
                List<FunctionInfo> functions = list(new QueryWrapper<FunctionInfo>().eq("parent_id", l.getParentId()).
                        eq("type",0));
                List<FunctionInfo> filterFunctions = functions.stream().filter(f -> f.getRouteOrder()>=l.getRouteOrder()).map(f->{
                    f.setRouteOrder(f.getRouteOrder() + 1);
                    return f;
                }).collect(Collectors.toList());
                if(filterFunctions==null||filterFunctions.size()==0){//此菜单的routOrder值最大:要判断是否大于最大的超过1,如果是就要设置为比之前最大的多1
                    if(functions!=null&&functions.size()>0){
                        Integer max = Collections.max(functions.stream().map(f -> f.getRouteOrder()).collect(Collectors.toList()));
                        if(max+1<l.getRouteOrder()){//说明当前元素的routeOrder的值比最大的值大于1
                            l.setRouteOrder(max+1);
                        }
                    }else{//当前新增的元素是第一个
                        l.setRouteOrder(0);
                    }
                }else{
                    //对同父级的已有权限进行持久化update
                    boolean b = updateBatchById(filterFunctions);
                    if(!b){
                        throw new RuntimeException("对同父级的已有权限进行持久化update失败..");
                    }
                }
            }
        });
        //3.进行批量新增操作
        boolean b = saveBatch(rightList);
        if(!b){
            throw new RuntimeException("批量新增操作失败..");
        }
        //4.如果新增成功,则从中找到有url和permit值的元素,将这些元素批量新增到shiro的权限链中并刷新shro权限链缓存
        List<FunctionInfo> shiroFunctions = rightList.stream().filter(f -> !StrUtil.isEmpty(f.getPermit())).collect(Collectors.toList());

        if(shiroFunctions==null||shiroFunctions.size()==0){//没有需要更新后端url链路的权限
            if(errorResults.size()>0){//有字段值重复的记录,返回给前端
                return new RspResult(RspResultCode.FIELDS_REPEAT_ERROR,errorResults);
            }
            return RspResult.SUCCESS;
        }

        boolean b1 = shiroService.updateShiroPermissions();
        if(b1){
            if(errorResults.size()>0){//有字段值重复的记录,返回给前端
                return new RspResult(RspResultCode.FIELDS_REPEAT_ERROR,errorResults);
            }
            return RspResult.SUCCESS;
        }
        return RspResult.UPDATE_SHIRO_PERMISSIONS_FAILED;
    }

    //这里的修改跟常规的修改略有不同:就是说很多关键性字段即使不做修改,也必须要求前端返回——因为这些关键性字段为空时可以表示用户不想进行某些关联，
    // 所以这里的关键性字段，如果用户不想改动，也要将查询的原值作为请求参数传过来：routeOrder、parentId、type
    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.UPDATE,parameterType= ParameterType.List)
    public RspResult updatesByIds(List<FunctionInfo> list) {
        //字段重复性校验
        List<String> fieldNames=new ArrayList<>();
        fieldNames.add("name");//name字段不可重复
        fieldNames.add("code");//code字段不可重复
        List<FieldsRepeatCheckResult<FunctionInfo>> results = FieldsRepeatCheckUtil.fieldsRepeatChecks(functionInfoMapper, ServiceConstant.FIELDS_REPEAT_CHECK_METHOD, list, fieldNames, FieldsRepeatCheckUtil.UPDATE);
        //拿出可以执行的部分进行执行
        List<FunctionInfo> rightList = results.stream().filter(r -> r.getResult().equals(false)).map(r -> r.getData()).collect(Collectors.toList());
        //拿出字段重复的部分返回给前端
        List<FieldsRepeatCheckResult<FunctionInfo>> errorResults = results.stream().filter(r -> r.getResult().equals(true)).collect(Collectors.toList());


        List<FunctionInfo> oldList=new ArrayList<>();
        rightList.stream().forEach(l->{
            //进行判断和稳定性设置
            Integer routeOrder = l.getRouteOrder();
            if(routeOrder==null||routeOrder<0){//按0处理
                l.setRouteOrder(0);
            }
            Long parentId = l.getParentId();//按0处理:顶级菜单或游离按钮
            if(parentId==null||parentId<=0){
                l.setParentId(0l);
            }else{//要判断当前元素指定的父级元素是否为[按钮],如果是按钮则禁止本次的修改操作:因为按钮是不能被挂载的
                //判断指定的父级元素是否存在
                FunctionInfo parentFunction = getById(parentId);
                if(parentFunction==null){
                    throw new RuntimeException("指定的父级元素不存在");
                }
                if(parentFunction.getType()==1){
                    throw new RuntimeException("指定的父级元素为按钮");
                }
            }
            Integer type = l.getType();
            if(type==null||(type!=0&&type!=1)){//按0处理:菜单
                l.setType(0);
            }
            //2.如果符合系统要求并且它是[菜单],则计算其routeOrder与跟它同父元素的其他[菜单]元素的routeOrder值的对比:
            //如果routeOrder小于0,则变为0并让其作为同父元素的第一个序列位置
            //如果routeOrder大于同父元素中的最大的那个元素的routeOrder值超过1,则修改的[菜单]会排在最后并且将routeOrder改为最大的那个已有的值+1
            //如果routeOrder处于同父元素中的中间位置,则还要判断它现在的值跟原来的值的大小:
            // 如果大,则让插入的那个位置以及之前的元素向前移1位;如果小,则将跟它routeOrder值相等和后面更大routeOrder值的元素统统往后挪1位
            FunctionInfo oldFunction = getById(l.getId());
            oldList.add(oldFunction);
            if(oldFunction==null){
                throw new RuntimeException("要修改的权限已经不存在,修改失败..");
            }
            if(l.getType()==0){//菜单
                List<FunctionInfo> functions = list(new QueryWrapper<FunctionInfo>().eq("parent_id", l.getParentId()).
                        eq("type",0));
                List<FunctionInfo> filterFunctions=null;
                if(l.getRouteOrder()>oldFunction.getRouteOrder()){//当前大
                    filterFunctions = functions.stream().filter(f -> f.getRouteOrder()<=l.getRouteOrder()&&f.getRouteOrder()>oldFunction.getRouteOrder()).map(f->{
                        f.setRouteOrder(f.getRouteOrder() - 1);
                        return f;
                    }).collect(Collectors.toList());
                    Integer max = Collections.max(functions.stream().map(f -> f.getRouteOrder()).collect(Collectors.toList()));
                    if(max<l.getRouteOrder()){//说明当前元素的routeOrder的值是最大的,则当前元素就应该是最大的那个
                        l.setRouteOrder(max);
                    }
                }else if(l.getRouteOrder()<oldFunction.getRouteOrder()){//当前小
                    filterFunctions = functions.stream().filter(f -> f.getRouteOrder()>=l.getRouteOrder()&&f.getRouteOrder()<oldFunction.getRouteOrder()).map(f->{
                        f.setRouteOrder(f.getRouteOrder() + 1);
                        return f;
                    }).collect(Collectors.toList());
                }else{
                    //如果相等,则直接过
                }

                //对同父的已有权限进行持久化update
                if(filterFunctions!=null){
                    if(!updateBatchById(filterFunctions)){
                        throw new RuntimeException("对同父级的已有权限进行持久化update失败..");
                    }
                }
            }
        });
        //3.进行批量更新操作
        boolean b = updateBatchById(rightList);
        if(!b){
            throw new RuntimeException("批量更新操作失败..");
        }
        //4.如果更新成功,则要将全部元素并跟原始值进行比对,如果修改了:则将这些元素批量更新到shiro的权限链中并刷新shro权限链缓存
        //1)查询全部元素在数据库中的url和permit的值,然后对比看是否有更改
        //2)如果没有更改,则直接过;如果有更改,则要更新shiro权限链缓存
        List<FunctionInfo> filterFunctions = rightList.stream().filter(l ->
                oldList.stream().anyMatch(o -> o.getId().equals(l.getId()) && (!o.getUrl().equals(l.getUrl()) || !o.getPermit().equals(l.getPermit())))
        ).collect(Collectors.toList());

        if(filterFunctions==null||filterFunctions.size()==0){//没有需要更新后端url链路的权限
            if(errorResults.size()>0){//有字段值重复的记录,返回给前端
                return new RspResult(RspResultCode.FIELDS_REPEAT_ERROR,errorResults);
            }
            return RspResult.SUCCESS;
        }
        boolean b2 = shiroService.updateShiroPermissions();
        if(!b2){//更新失败,返回错误信息
            throw new RuntimeException("更新系统的权限链缓存失败..");
        }
        //5.最后还要更新redis现有的用户的权限缓存信息
        //1)通过functionIds去数据库查到关联的用户集合
        //这里我们只需要拿取改变了permit的那部分权限,因为用户的权限缓存中只是存了permit字段的值
        List<FunctionInfo> filterFunctions2 = list.stream().filter(l ->
                oldList.stream().anyMatch(o -> o.getId().equals(l.getId()) && (!o.getPermit().equals(l.getPermit())))
        ).collect(Collectors.toList());
        List<UserInfo> userInfos=null;
        if(filterFunctions2!=null&&filterFunctions2.size()>0){
            userInfos = functionInfoMapper.selectUsersByFunctionIds(filterFunctions2.stream().map(f -> f.getId()).collect(Collectors.toList()));
        }
        //2)通过用户集合对比redis缓存中的用户集合,如果有对应的,就要更新他们的权限缓存数据
        if(userInfos==null||userInfos.size()==0){//没有受影响的用户,直接返回成功信息
            if(errorResults.size()>0){//有字段值重复的记录,返回给前端
                return new RspResult(RspResultCode.FIELDS_REPEAT_ERROR,errorResults);
            }
            return RspResult.SUCCESS;
        }
        userInfos.stream().forEach(u->{
            Long userId = u.getId();
            String userFunctionsKey= UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
            Object userFunctionRs = baseMapper.getCache(userFunctionsKey);
            if(userFunctionRs!=null){//更新用户的权限缓存信息
                List<FunctionInfo> functionInfos = userInfoMapper.selectFunctionsByUserIds(Arrays.asList(userId));
                //只存permit不为空的属性,因为这种才是给后端shiro用的
                List<String> functionPermits = functionInfos.stream().filter(functionInfo -> !StrUtil.isEmpty(functionInfo.getPermit())).map(
                        functionInfo -> functionInfo.getPermit()
                ).collect(Collectors.toList());
                UserFunctionRs redisUserFunctionRs=new UserFunctionRs();
                redisUserFunctionRs.setFunctionPermits(functionPermits);
                redisUserFunctionRs.setUserId(userId);
                baseMapper.updateCache(userFunctionsKey,redisUserFunctionRs, LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }
        });

        if(errorResults.size()>0){//有字段值重复的记录,返回给前端
            return new RspResult(RspResultCode.FIELDS_REPEAT_ERROR,errorResults);
        }
        return RspResult.SUCCESS;
    }

    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.DELETE,parameterType= ParameterType.List)
    public RspResult deletesByIds(List<Long> ids) {
        List<FunctionInfo> functionInfos=new ArrayList<>();
        ids.stream().forEach(id->{
            //1.查询出对应的权限
            FunctionInfo function = getById(id);
            if(function==null){//已经不存在,返回错误信息
                throw new RuntimeException("要删除的权限不存在,删除失败..");
            }
            //2.判断是否有关联性以及是否有子权限,如果有则禁止删除
            //1)判断是否与角色有关联
            List<RoleFunctionRs> roleFunctions = roleFunctionRsMapper.selectList(new QueryWrapper<RoleFunctionRs>().eq("function_id", id));
            if(roleFunctions!=null&&roleFunctions.size()>0){//有角色关联
                throw new RuntimeException("当前要删除的权限有关联的角色,禁止删除..");
            }
            //2)判断是否有子权限
            List<FunctionInfo> childrenFunctions = functionInfoMapper.selectList(new QueryWrapper<FunctionInfo>().eq("parent_id", id));
            if(childrenFunctions!=null&&childrenFunctions.size()>0){//有子权限
                throw new RuntimeException("当前要删除的权限有子权限,禁止删除..");
            }

            //3.如果存在并且它是[菜单],则计算其routeOrder与跟它同父元素的其他[菜单]元素的routeOrder值的对比:
            if(function.getType()==0){//菜单
                List<FunctionInfo> functions = list(new QueryWrapper<FunctionInfo>().eq("parent_id", function.getParentId()).
                        eq("type",0).gt("route_order",function.getRouteOrder()));
                //将它同父的后面的元素往前挪1位
                if(functions!=null&&functions.size()>0){
                    functions.stream().forEach(f->{
                        f.setRouteOrder(f.getRouteOrder()-1);
                    });
                    boolean b = updateBatchById(functions);
                    if(!b){
                        throw new RuntimeException("同父元素的其他[菜单]元素的移位失败,事务回滚..");
                    }
                }
            }
            functionInfos.add(function);
        });
        //4.进行删除操作
        boolean b = removeByIds(ids);
        if(!b){
            throw new RuntimeException("批量删除操作失败..");
        }
        //5.如果删除成功,则从旧的记录中看是否有permit值不为空的元素,如果有就刷新shiro权限链缓存
        boolean b1 = functionInfos.stream().anyMatch(f -> !StrUtil.isEmpty(f.getPermit()));

        if(!b1){//没有需要更新后端url链路的权限
            return RspResult.SUCCESS;
        }

        boolean b2 = shiroService.updateShiroPermissions();//能在同一个事务下更新,说明是[允许脏读]才能实现的
        if(b2){
            return RspResult.SUCCESS;
        }
        return RspResult.FAILED;
    }

    @Override
    public RspResult selectsByTreeByPage(FunctionInfo t) {
        //1.无论是否有条件或模糊查询,都肯定要拿出全部权限用于后面的判断比对
        List<FunctionInfo> all = list();
        List<FunctionInfo> filterFunctions = all.stream().filter(a -> {
            //2.使用反射的动态属性功能
            //*如果想忽略字段的匹配,则字段值要为null;如果字段值为"",则这里视为匹配为null或为""的值
            try {
                Class<? extends FunctionInfo> aClass = t.getClass();
                //1)进行字段遍历匹配
                Field[] declaredFields = aClass.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    declaredField.setAccessible(true);
                    if(Modifier.isStatic(declaredField.getModifiers())){//静态属性过滤掉
                        continue;
                    }
                    TableField annotation = declaredField.getAnnotation(TableField.class);
                    if (annotation == null || annotation.exist() == true) {//不能忽略此字段
                        Object parameterValue = declaredField.get(t);
                        if (parameterValue == null) {//null表示过,匹配下一个字段
                            continue;
                        }
                        //程序能走到这里,说明用户在一些非忽略的字段上填值了..所以用户是想进行选择性匹配的
                        a.setFlag(true);
                        Object dataValue = declaredField.get(a);
                        if (parameterValue.equals(dataValue)) {//值相等,匹配上
                            return true;
                        }
                    }
                }
                //2)进行key的模糊匹配:name||code||describ||url||permit
                if (t.getKey() != null) {
                    //程序能走到这里,说明用户在一些非忽略的字段上填值了..所以用户是想进行选择性匹配的
                    a.setFlag(true);
                    String key = t.getKey().trim();//去空
                    if("".equals(key)){//如果是空,则必须按照非模糊查询来;否则用contains方法会将所有为空的都匹配上
                        return key.equals(a.getName()==null?"":a.getName())|| key.equals(a.getCode()==null?"":a.getCode())
                                || key.equals(a.getDescrib()==null?"":a.getDescrib()) || key.equals(a.getUrl()==null?"":a.getUrl())
                                || key.equals(a.getPermit()==null?"":a.getPermit());
                    }
                    //到这里,相当于key一定不为"",所以下面是对这种情况的处理
                    if((a.getName()!=null&&a.getName().contains(key))||(a.getCode()!=null&&a.getCode().contains(key))
                    ||(a.getDescrib()!=null&&a.getDescrib().contains(key))||(a.getUrl()!=null&&a.getUrl().contains(key))
                    ||(a.getPermit()!=null&&a.getPermit().contains(key))){
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("属性获取和比对失败..");
            }
        }).collect(Collectors.toList());
        List<FunctionInfo> treeFunctions;
        if((filterFunctions==null||filterFunctions.size()==0)&&t.getFlag()==true){//说明用户是填入选择性的值,但是没有匹配到数据
            return new RspResult("没有匹配到符合条件的数据");
        }else if(filterFunctions==null||filterFunctions.size()==0){//用户压根没有填入选择性的值,而是想查询全部权限
            //直接对all进行树状处理,然后返回前端
            treeFunctions=TreeStructuresUtil.buildTree(all);
        }else{
            //下面这种情况,就是用户输入了匹配性的字段值,并且也确实匹配上了部分数据->所以这里要进行处理
            List<FunctionInfo> finalFunctions=new ArrayList<>();
            filterFunctions.stream().forEach(f->{
                List<FunctionInfo> parents = TreeStructuresUtil.getParents(f.getParentId(), filterFunctions);
                if(parents!=null&&parents.size()>0){
                    finalFunctions.addAll(parents);
                }
            });
            finalFunctions.addAll(filterFunctions);
            //进行去重处理
            HashSet<FunctionInfo> set=new HashSet<>(finalFunctions);
            finalFunctions.clear();
            finalFunctions.addAll(set);
            treeFunctions = TreeStructuresUtil.buildTree(finalFunctions);
        }

        //3.伪分页返回前端
        PageManager<FunctionInfo> pageManager=new PageManager<>(t.getCurrent(),t.getSize(),treeFunctions);
        IPage<FunctionInfo> pageData=new Page<>();
        pageData.setCurrent(pageManager.getCurrent());
        pageData.setSize(pageManager.getSize());
        pageData.setRecords(pageManager.getRecords());
        pageData.setTotal(pageManager.getTotal());
        pageData.setPages(pageManager.getPages());

        return new RspResult(pageData);
    }

    @Override
    public RspResult selectsByIds(List<Long> ids) {
        //1.查出指定的全部权限
        List<FunctionInfo> functionInfos = functionInfoMapper.selectBatchIds(ids);
        if(functionInfos==null||functionInfos.size()==0){//查询失败
            return RspResult.SELECT_NULL;
        }

        //2.查出除了按钮的全部权限:type=0表示菜单
        List<FunctionInfo> menuFunctions = list(new QueryWrapper<FunctionInfo>().eq("type", 0));
        //3.进行返回数据的构建
        if(functionInfos.size()>1){//如果不是一个,就得使用克隆复制出多份menuFunctions供不同的权限独享
            List<FunctionInfo> finalFunctions = functionInfos.stream().map(f -> {
                //给menuFunctions进行深度克隆
                List<FunctionInfo> exclusiveMenuFunctions = menuFunctions.stream().map(mf -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    FunctionInfo functionInfo;
                    try {
                        functionInfo = objectMapper.readValue(objectMapper.writeValueAsString(mf), FunctionInfo.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        throw new RuntimeException("给menuFunctions进行深度克隆出现异常..");
                    }
//                    FunctionInfo functionInfo = ObjectUtil.cloneByStream(mf);
                    if (functionInfo.getId().equals(f.getParentId())) {//是当前权限的父权限
                        functionInfo.setFlag(true);
                    }
                    return functionInfo;
                }).collect(Collectors.toList());
                //进行树状排序
                f.setFunctionInfos(TreeStructuresUtil.buildTree(exclusiveMenuFunctions));
                return f;
            }).collect(Collectors.toList());
            return new RspResult(finalFunctions);
        }
        //如果是1个,则不需要克隆复制多份menuFunctions
        FunctionInfo functionInfo = functionInfos.get(0);
        menuFunctions.stream().forEach(mf->{
            if(mf.getId().equals(functionInfo.getParentId())){//是当前权限的父权限
                mf.setFlag(true);
            }
        });
        //进行树状排序
        functionInfo.setFunctionInfos(TreeStructuresUtil.buildTree(menuFunctions));
        return new RspResult(functionInfo);
    }

    @Transactional
    @Override
    public RspResult excelImport(MultipartFile excelFile) {
        InputStream in=null;
        try {
            in = excelFile.getInputStream();
            BaseRowReadListener<com.planet.module.authManage.entity.excel.FunctionInfo> baseRowReadListener=new BaseRowReadListener<>(
                    readBatchCount,baseExcelImportValid,baseDao,functionInfoExcelToPoConverter
            );
            Class<?>[] parameterTypes=new Class[1];
            parameterTypes[0]=List.class;
            baseRowReadListener.setParameterTypes(parameterTypes);
            ExcelReaderBuilder workBook = EasyExcel.read(in, com.planet.module.authManage.entity.excel.FunctionInfo.class, baseRowReadListener);
            // 封装工作表
            ExcelReaderSheetBuilder sheet1 = workBook.sheet();
            // 读取
            sheet1.doRead();

            //3.要拿出BaseReadListener对象中的resultCode和unqualifiedRows来获知校验和持久化的结果:这两个值可以直接返回给前端,让前端展示相应的效果给用户
            Result<com.planet.module.authManage.entity.excel.FunctionInfo> result = baseRowReadListener.getResult();
            System.out.println(result);
            int resultCode = result.getResultCode();
            if(result.getResultCode()==0){//导入成功
                return RspResult.SUCCESS;
            }else{//将失败结果返回
                return new RspResult(result);
            }
        }catch (IOException e) {
            e.printStackTrace();
            log.error("权限记录导入失败");
            throw new RuntimeException("权限记录导入失败,事务回滚");
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void excelExport(FunctionInfo t) {
        HttpServletResponse response = WebUtil.getResponse();
        // 1.模板
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(
                "templates/excel/modular/权限信息模块-模板.xlsx");

        // 2.目标文件
        String targetFile = "权限信息模块-记录.xlsx";

        //3.模型实体类的类对象
        Class functionInfoClass= com.planet.module.authManage.entity.excel.FunctionInfo.class;

        // 4.写入workbook对象
        ExcelWriter workBook =null;
        try {
            workBook = EasyExcel.write(response.getOutputStream(),functionInfoClass).withTemplate(templateInputStream).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //5.准备工作表和对应的数据
        WriteSheet sheet = EasyExcel.writerSheet().build();

        //6.获取数据
        QueryWrapper<FunctionInfo> tQueryWrapper = new QueryWrapper<>();
        Map<String, Object> stringObjectMap = null;
        try {
            stringObjectMap = MapAndEntityConvertUtil.entityToMap(t, ServiceConstant.CLOSE,true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        //将需要作为查询条件的字段装配到QueryWrapper上,用于拼装查询sql(目前我们只做等于这种,以后会改进成各种方式的)
        for (String key : stringObjectMap.keySet()) {
            if("size".equals(key)||"current".equals(key)){
                continue;
            }
            tQueryWrapper.eq(key,stringObjectMap.get(key));
        }
//            IPage<T> pageData = iService.page(page, tQueryWrapper.orderByAsc("id"));
        List<FunctionInfo> list=list(tQueryWrapper.orderByAsc("id"));
        List<com.planet.module.authManage.entity.excel.FunctionInfo> functionInfos = functionInfoExcelToPoConverter.convertPoToExcel(list);

        //7. 写入数据

        workBook.fill(functionInfos, sheet);

        //8.构建响应对象
        response.setContentType("application/vnd.ms-excel");
//        response.setContentType("application/octet-stream;charset=ISO8859-1");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = null;
        try {
            fileName = URLEncoder.encode(targetFile, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        workBook.finish();

    }


}
