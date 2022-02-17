package com.planet.module.authManage.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.common.constant.ServiceConstant;
import com.planet.module.authManage.converter.easyExcelPlus.AccountLogExcelToPoConverter;
import com.planet.module.authManage.converter.easyExcelPlus.RoleLogExcelToPoConverter;
import com.planet.module.authManage.dao.mysql.mapper.AccountLogMapper;
import com.planet.module.authManage.entity.mysql.AccountLog;
import com.planet.module.authManage.entity.mysql.RoleLog;
import com.planet.module.authManage.service.AccountLogService;
import com.planet.util.jdk8.mapAndEntityConvert.MapAndEntityConvertUtil;
import com.planet.util.springBoot.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 个人账号-历史表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-02-06
 */
@Service
public class AccountLogServiceImpl extends ServiceImpl<AccountLogMapper, AccountLog> implements AccountLogService {
    @Autowired
    private AccountLogExcelToPoConverter accountLogExcelToPoConverter;
    @Override
    public IPage<AccountLog> selectsByPage(AccountLog t) {
        Page<AccountLog> page = new Page(t.getCurrent(), t.getSize());
        //这里先做出基本的属性遍历查询,以后要加入模糊、大小等各种可能出现的情况的判断方式
        QueryWrapper<AccountLog> tQueryWrapper = new QueryWrapper<>();
        Map<String, Object> stringObjectMap = null;
        try {
            stringObjectMap = MapAndEntityConvertUtil.entityToMap(t, ServiceConstant.CLOSE,true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

//        //这种操作是不允许的,所以说mybatisplus并没有完全实现动态sql的构建执行:因为即使是查询,也仅仅是要先确定表名对应的实体类才能往下构建,
//        //想实现完全动态sql的构建并执行,需要用java的反射并配合spring的自动获取dao的bean对象来选定可以执行的dao对象然后再执行对应表的sql语句
//        Class<? extends UserInfo> aClass = t.getClass();
//        QueryWrapper<aClass> aClassQueryWrapper=new QueryWrapper<aClass>();

        //将需要作为查询条件的字段装配到QueryWrapper上,用于拼装查询sql(目前我们只做等于这种,以后会改进成各种方式的)
        for (String key : stringObjectMap.keySet()) {
            if("size".equals(key)||"current".equals(key)){
                continue;
            }
            tQueryWrapper.eq(key,stringObjectMap.get(key));
        }

        IPage<AccountLog> pageData = page(page, tQueryWrapper.orderByAsc("id"));
        return pageData;
    }

    @Override
    public void excelExport(AccountLog t) {
        HttpServletResponse response = WebUtil.getResponse();
        // 1.模板
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(
                "templates/账号操作记录模块-模板.xlsx");

        // 2.目标文件
        String targetFile = "账号操作记录模块-记录.xlsx";

        //3.模型实体类的类对象
        Class accountLogClass= com.planet.module.authManage.entity.excel.AccountLog.class;

        // 4.写入workbook对象
        ExcelWriter workBook =null;
        try {
            workBook = EasyExcel.write(response.getOutputStream(),accountLogClass).withTemplate(templateInputStream).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //5.准备工作表和对应的数据
        WriteSheet sheet = EasyExcel.writerSheet().build();

        //6.获取数据
        QueryWrapper<com.planet.module.authManage.entity.mysql.AccountLog> tQueryWrapper = new QueryWrapper<>();
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
        List<AccountLog> list=list(tQueryWrapper.orderByAsc("id"));
        List<com.planet.module.authManage.entity.excel.AccountLog> accountLogs = accountLogExcelToPoConverter.convertPoToExcel(list);

        //7. 写入数据

        workBook.fill(accountLogs, sheet);

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
