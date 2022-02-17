package com.planet.module.authManage.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.constant.ServiceConstant;
import com.planet.common.constant.UtilsConstant;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.converter.easyExcelPlus.RoleInfoExcelToPoConverter;
import com.planet.module.authManage.converter.easyExcelPlus.UserInfoExcelToPoConverter;
import com.planet.module.authManage.dao.mysql.mapper.RoleFunctionRsMapper;
import com.planet.module.authManage.dao.mysql.mapper.RoleInfoMapper;
import com.planet.module.authManage.entity.mysql.RoleFunctionRs;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.planet.module.authManage.service.RoleInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.module.authManage.service.UserRoleRsService;
import com.planet.system.sysUserOperationLog.annotation.SysUserOperationMethodLog;
import com.planet.system.sysUserOperationLog.enumeration.MethodType;
import com.planet.system.sysUserOperationLog.enumeration.ParameterType;
import com.planet.util.jdk8.mapAndEntityConvert.MapAndEntityConvertUtil;
import com.planet.util.springBoot.WebUtil;
import com.planetProvide.easyExcelPlus.core.baseDao.BaseDao;
import com.planetProvide.easyExcelPlus.core.baseExcelImportValid.BaseExcelImportValid;
import com.planetProvide.easyExcelPlus.core.baseReadListener.BaseRowReadListener;
import com.planetProvide.easyExcelPlus.core.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色-信息表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
public class RoleInfoServiceImpl extends ServiceImpl<RoleInfoMapper, RoleInfo> implements RoleInfoService {
    @Autowired
    private UserRoleRsService userRoleRsService;
    @Autowired
    private RoleFunctionRsMapper roleFunctionRsMapper;

    @Value("${readBatchCount}") //100
    private long readBatchCount;
    @Autowired
    private BaseExcelImportValid<com.planet.module.authManage.entity.excel.RoleInfo> baseExcelImportValid;
    @Autowired
    private BaseDao<com.planet.module.authManage.entity.excel.RoleInfo> baseDao;
    @Autowired
    private RoleInfoExcelToPoConverter roleInfoExcelToPoConverter;


    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.INSERT,parameterType= ParameterType.List)
    public Integer inserts(List<RoleInfo> list) {
        if(list==null&&list.size()<=0){
            throw new RuntimeException("新增失败:集合不能为空..事务回滚");
        }else if(list.size()==1){
            if(!save(list.get(0))){
                throw new RuntimeException("新增失败,事务回滚");
            }
        }else{
            if(!saveBatch(list)){
                throw new RuntimeException("新增失败,事务回滚");
            }
        }
        return list.size();
    }

    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.UPDATE,parameterType= ParameterType.List)
    public Integer updatesByIds(List<RoleInfo> list) {
        if(list==null&&list.size()<=0){
            throw new RuntimeException("更新失败:集合不能为空..事务回滚");
        }else if(list.size()==1){
            if(!updateById(list.get(0))){
                throw new RuntimeException("更新失败,事务回滚");
            }
        }else{
            if(!updateBatchById(list)){
                throw new RuntimeException("更新失败,事务回滚");
            }
        }
        return list.size();
    }

    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.DELETE,parameterType= ParameterType.List)
    public Integer deletesByIds(List<Long> ids) {
        //0.将要删除的记录先查出来进行判断:看是否已经被删除了,如果已经被删除了,为了保险起见,直接返回错误信息让用户重新确认后再操作
        List<RoleInfo> roleInfos = (List<RoleInfo>)listByIds(ids);
        if(roleInfos==null||roleInfos.size()!=ids.size()){
            throw new RuntimeException("删除用户失败:集合不能为空,事务回滚");
        }
        //1.判断是否有关联的用户记录,如果有则禁止删除
        List<UserRoleRs> authUserRoleRsList = userRoleRsService.list(new QueryWrapper<UserRoleRs>().in("role_id", ids));
        if(authUserRoleRsList==null||authUserRoleRsList.size()==0){//当前所有的用户已经没有了关联的此角色,允许删除
            //2.删除记录
            if(!removeByIds(ids)){
                throw new RuntimeException("删除用户失败,事务回滚");
            }
        }else{//当前某个角色已经有了关联的用户或关联的权限,禁止删除
            throw new RuntimeException("删除用户失败:当前角色有关联的用户,事务回滚");
        }

        //3.删除关联权限记录
        int delete = roleFunctionRsMapper.delete(new QueryWrapper<RoleFunctionRs>().in("role_id", ids));
        return ids.size();
    }

    @Transactional
    @Override
    public RspResult excelImport(MultipartFile excelFile) {
        InputStream in=null;
        try {
            in = excelFile.getInputStream();
            BaseRowReadListener<com.planet.module.authManage.entity.excel.RoleInfo> baseRowReadListener=new BaseRowReadListener<>(
                    readBatchCount,baseExcelImportValid,baseDao,roleInfoExcelToPoConverter
            );
            Class<?>[] parameterTypes=new Class[1];
//            parameterTypes[0]=MultipartFile[].class;
            parameterTypes[0]=List.class;
            baseRowReadListener.setParameterTypes(parameterTypes);
            ExcelReaderBuilder workBook = EasyExcel.read(in, com.planet.module.authManage.entity.excel.RoleInfo.class, baseRowReadListener);
            // 封装工作表
            ExcelReaderSheetBuilder sheet1 = workBook.sheet();
            // 读取
            sheet1.doRead();

            //3.要拿出BaseReadListener对象中的resultCode和unqualifiedRows来获知校验和持久化的结果:这两个值可以直接返回给前端,让前端展示相应的效果给用户
            Result<com.planet.module.authManage.entity.excel.RoleInfo> result = baseRowReadListener.getResult();
            System.out.println(result);
            int resultCode = result.getResultCode();
            if(result.getResultCode()==0){//导入成功
                return RspResult.SUCCESS;
            }else{//将失败结果返回
                return new RspResult(result);
            }
        }catch (IOException e) {
            e.printStackTrace();
            log.error("角色记录导入失败");
            throw new RuntimeException("角色记录导入失败,事务回滚");
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void excelExport(RoleInfo t) {
        HttpServletResponse response = WebUtil.getResponse();
        // 1.模板
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(
                "templates/角色信息模块-模板.xlsx");

        // 2.目标文件
        String targetFile = "角色信息模块-记录.xlsx";

        //3.模型实体类的类对象
        Class roleInfoClass= com.planet.module.authManage.entity.excel.RoleInfo.class;

        // 4.写入workbook对象
        ExcelWriter workBook =null;
        try {
            workBook = EasyExcel.write(response.getOutputStream(),roleInfoClass).withTemplate(templateInputStream).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //5.准备工作表和对应的数据
        WriteSheet sheet = EasyExcel.writerSheet().build();

        //6.获取数据
        QueryWrapper<RoleInfo> tQueryWrapper = new QueryWrapper<>();
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
        List<RoleInfo> list=list(tQueryWrapper.orderByAsc("id"));
        List<com.planet.module.authManage.entity.excel.RoleInfo> roleInfos = roleInfoExcelToPoConverter.convertPoToExcel(list);

        //7. 写入数据

        workBook.fill(roleInfos, sheet);

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
