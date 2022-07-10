package com.planet.module.authManage.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.planet.common.constant.LocalCacheConstantService;
import com.planet.common.util.RspResultCode;
import com.planet.module.authManage.entity.mysql.RoleInfo;
import com.planet.module.authManage.service.authByShiro.ShiroService;
import com.planet.system.fieldsRepeatCheck.FieldsRepeatCheckResult;
import com.planet.system.fieldsRepeatCheck.FieldsRepeatCheckUtil;
import com.planet.system.sysUserOperationLog.annotation.SysUserOperationMethodLog;
import com.planet.system.sysUserOperationLog.enumeration.MethodType;
import com.planet.system.sysUserOperationLog.enumeration.ParameterType;
import com.planetProvide.easyExcelPlus.core.baseDao.BaseDao;
import com.planetProvide.easyExcelPlus.core.baseExcelImportValid.BaseExcelImportValid;
import com.planetProvide.easyExcelPlus.core.baseReadListener.BaseRowReadListener;
import com.planetProvide.easyExcelPlus.core.entity.Result;
import com.planet.common.constant.ServiceConstant;
import com.planet.common.constant.UtilsConstant;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.converter.easyExcelPlus.UserInfoExcelToPoConverter;
import com.planet.module.authManage.dao.mysql.mapper.ConfigureSysMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserInfoMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserRoleRsMapper;
import com.planet.module.authManage.dao.redis.BaseMapper;
import com.planet.module.authManage.entity.mysql.ConfigureSys;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.entity.mysql.UserRoleRs;
import com.planet.module.authManage.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.util.springBoot.WebUtil;
import com.planet.util.shiro.DigestsUtil;
import com.planet.util.jdk8.mapAndEntityConvert.MapAndEntityConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户-信息表 服务实现类
 * </p>
 *
 * @author Planet
 * @since 2022-01-17
 */
@Service
@Slf4j
@PropertySource({"classpath:config/application.yml", "classpath:config/easyExcelPlus.yml"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Value("${web.upload-path}") // D:/home/authorityManagement-fileFolder
    private String webUploadPath;
    @Value(("${spring.mvc.static-path-pattern}")) // /file/**
    private String mvcStaticPathPattern;
    @Autowired
    private ConfigureSysMapper configureSysMapper;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private UserRoleRsMapper userRoleRsMapper;
    @Autowired
    private ShiroService shiroService;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Value("${readBatchCount}") //100
    private long readBatchCount;
    @Autowired
    private BaseExcelImportValid<com.planet.module.authManage.entity.excel.UserInfo> baseExcelImportValid;
    @Autowired
    private BaseDao<com.planet.module.authManage.entity.excel.UserInfo> baseDao;
    @Autowired
    private UserInfoExcelToPoConverter userInfoExcelToPoConverter;

    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.INSERT,parameterType= ParameterType.List)
    public RspResult inserts(MultipartFile[] multipartFiles, List<UserInfo> list) {
        //字段重复性校验
        List<String> fieldNames=new ArrayList<>();
        fieldNames.add("name");//name字段不可重复
        fieldNames.add("code");//code字段不可重复
        List<FieldsRepeatCheckResult<UserInfo>> results = FieldsRepeatCheckUtil.fieldsRepeatChecks(userInfoMapper, ServiceConstant.FIELDS_REPEAT_CHECK_METHOD, list, fieldNames, FieldsRepeatCheckUtil.INSERT);
        //拿出可以执行的部分进行执行
        List<UserInfo> rightList = results.stream().filter(r -> r.getResult().equals(false)).map(r -> r.getData()).collect(Collectors.toList());
        //拿出字段重复的部分返回给前端
        List<FieldsRepeatCheckResult<UserInfo>> errorResults = results.stream().filter(r -> r.getResult().equals(true)).collect(Collectors.toList());

        //1.所有用户的密码都要进行盐值加密
        rightList.stream().forEach(user ->{
            Map<String, String> map = DigestsUtil.encryptPassword(user.getPassword());
            user.setPassword(map.get("password"));
            user.setSalt(map.get("salt"));

        });
        //2.然后进行存入:此时qrCode和portrait字段是空的,如果新增成功才会将这两个字段的内容生成(两个图片会接下去生成出来)
        boolean b = saveBatch(rightList);
        if(b){
            //3.新增成功后,进行
            rightList.stream().forEach(user ->{
                //查找对应的MultipartFile文件,更新新增用户的头像图片
                String pathPrefix=mvcStaticPathPattern.substring(0,mvcStaticPathPattern.lastIndexOf("/"));
                boolean flag=false;
                String portraitUrl="/userInfo/user_"+user.getId()+"/id="+user.getId()+"_portrait"+System.currentTimeMillis()+".jpg";
                if(multipartFiles!=null&&multipartFiles.length>0){
                    for (MultipartFile multipartFile : multipartFiles) {
                        if(multipartFile!=null&&multipartFile.getOriginalFilename()!=null&&multipartFile.getOriginalFilename().equals(user.getOriginalFilename())){
                            //找到匹配的头像图片,存入文件库中;并更新此user对象的portrait属性值为头像图片的url
                            InputStream in=null;
                            BufferedOutputStream out=null;
                            try {
                                in = multipartFile.getInputStream();
//                                multipartFile.transferTo(new File(webUploadPath+portraitUrl));
                                out = FileUtil.getOutputStream(new File(webUploadPath + portraitUrl));
                                long copySize = IoUtil.copy(in, out, IoUtil.DEFAULT_BUFFER_SIZE);
                            } catch (IOException e) {
                                e.printStackTrace();
                                log.error("头像图片转换失败");
                                throw new RuntimeException("头像图片转换失败,事务回滚");
                            }finally {
                                try {
                                    in.close();
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                            user.setPortrait(pathPrefix+portraitUrl);
                            flag=true;
                            break;
                        }
                    }
                }
                if(!flag){//未找到匹配的头像图片,则使用系统设定的默认的头像图片
                    //先看当前类的对应静态变量是否已经存值了,如果有则直接取出来
                    FileUtil.copy(webUploadPath+JSONUtil.parseObj(LocalCacheConstantService.getValue("account:userPortraitDefaultUrl")).get("userPortraitDefaultUrl",String.class),webUploadPath+portraitUrl,true);
                    user.setPortrait(pathPrefix+portraitUrl);
                }

                //构建只有四个字段的json对象,用于生成二维码
                JSONObject jsonUser = new JSONObject();
                jsonUser.set("id",user.getId());
                jsonUser.set("name",user.getName());
                jsonUser.set("realName",user.getRealName());
                jsonUser.set("nickname",user.getNickname());
                // 生成指定url对应的二维码到文件，宽和高都是300像素
                String qrCodeFileUrl="/userInfo/user_"+user.getId()+"/id="+user.getId()+"_qrCode"+System.currentTimeMillis()+".jpg";
                QrCodeUtil.generate(jsonUser.toString(),ServiceConstant.USER_QRCODE_WIDTH,
                        ServiceConstant.USER_QRCODE_HEIGHT,FileUtil.file(webUploadPath+qrCodeFileUrl));
                user.setQrCode(pathPrefix+qrCodeFileUrl);
            });
        }else{
            //回滚
            throw new RuntimeException("新增用户失败,事务回滚");
        }
        //进行更新操作
        boolean b1 = updateBatchById(rightList);
        if(b1){
            if(errorResults.size()>0){//存在字段重复性记录
                return new RspResult(RspResultCode.FIELDS_REPEAT_ERROR,errorResults);
            }
            return RspResult.SUCCESS;
        }else{
            //回滚
            throw new RuntimeException("新增用户失败,事务回滚");
        }
    }

    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.UPDATE,parameterType= ParameterType.List)
    public RspResult updatesByIds(MultipartFile[] multipartFiles, List<UserInfo> list) {
        //字段重复性校验
        List<String> fieldNames=new ArrayList<>();
        fieldNames.add("name");//name字段不可重复
        fieldNames.add("code");//code字段不可重复
        List<FieldsRepeatCheckResult<UserInfo>> results = FieldsRepeatCheckUtil.fieldsRepeatChecks(userInfoMapper, ServiceConstant.FIELDS_REPEAT_CHECK_METHOD, list, fieldNames, FieldsRepeatCheckUtil.UPDATE);
        //拿出可以执行的部分进行执行
        List<UserInfo> rightList = results.stream().filter(r -> r.getResult().equals(false)).map(r -> r.getData()).collect(Collectors.toList());
        //拿出字段重复的部分返回给前端
        List<FieldsRepeatCheckResult<UserInfo>> errorResults = results.stream().filter(r -> r.getResult().equals(true)).collect(Collectors.toList());

        //1.判断密码是否修改了,如果修改了就要进行盐值加密处理后修改密码和salt
        rightList.stream().forEach(user ->{
            if(user.getSalt()!=null&&user.getSalt().equals("1")){//说明要修改当前用户的密码
                Map<String, String> map = DigestsUtil.encryptPassword(user.getPassword());
                user.setPassword(map.get("password"));
                user.setSalt(map.get("salt"));
            }else{//不修改当前密码:为了保险起见,要将password和salt置空,以免updates时更新这两个字段的值
                user.setPassword(null);
                user.setSalt(null);
            }
            //让下面两个字段先不进行更新,等待后续判断后再进行更新
            user.setPortrait(null);
            user.setQrCode(null);
        });
        //2.然后进行更新:此时qrCode和portrait字段是还未更新,如果更新成功才会立刻更新这两个字段以及判断是否需要更新redis缓存中的用户信息
        //从而降低多个管理员用户在并发操作用户更新时出现互相干扰的概率
        boolean b = updateBatchById(rightList);
        if(b){
            //3.更新成功后,进行
            rightList.stream().forEach(user ->{
                //获取旧的头像图片和二维码图片的url,以便后面进行del标识
                UserInfo oldUser = getById(user.getId());
                String oldPortrait=oldUser.getPortrait();
                String oldQrCode=oldUser.getQrCode();
                //查找对应的MultipartFile文件,更新新增用户的头像图片
                String pathPrefix=mvcStaticPathPattern.substring(0,mvcStaticPathPattern.lastIndexOf("/"));
                boolean flag=false;
                String portraitUrl="/userInfo/user_"+user.getId()+"/id="+user.getId()+"_portrait"+System.currentTimeMillis()+".jpg";
                if(multipartFiles!=null&&multipartFiles.length>0){
                    for (MultipartFile multipartFile : multipartFiles) {
                        if(multipartFile!=null&&multipartFile.getOriginalFilename()!=null&&multipartFile.getOriginalFilename().equals(user.getOriginalFilename())){
                            //找到匹配的头像图片,存入文件库中;并更新此user对象的portrait属性值为头像图片的url
                            InputStream in =null;
                            BufferedOutputStream out=null;
                            try {
                                in = multipartFile.getInputStream();
//                                multipartFile.transferTo(new File(webUploadPath+portraitUrl));
                                out = FileUtil.getOutputStream(new File(webUploadPath + portraitUrl));
                                long copySize = IoUtil.copy(in, out, IoUtil.DEFAULT_BUFFER_SIZE);
                            } catch (IOException e) {
                                e.printStackTrace();
                                log.error("头像图片转换失败");
                                throw new RuntimeException("头像图片转换失败,事务回滚");
                            }finally {
                                try {
                                    in.close();
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            user.setPortrait(pathPrefix+portraitUrl);
                            flag=true;
                            break;
                        }
                    }
                }
                if(!flag){//未找到匹配的头像图片,则使用系统设定的默认的头像图片
                    FileUtil.copy(webUploadPath+JSONUtil.parseObj(LocalCacheConstantService.getValue("account:userPortraitDefaultUrl")).get("userPortraitDefaultUrl",String.class),webUploadPath+portraitUrl,true);
                    user.setPortrait(pathPrefix+portraitUrl);
                }
                //将替换掉的头像图片文件进行del标识,后续会让定时任务将这些del的头像图片文件转移到ftp指定的仓库中保留
                String oldPortraitFilename=webUploadPath+oldPortrait.replace(pathPrefix,"");
                File oldPortraitFile=new File(oldPortraitFilename);
                StringBuilder sb=new StringBuilder(oldPortraitFilename);
                boolean delPortrait = oldPortraitFile.renameTo(new File(sb.insert(oldPortraitFilename.lastIndexOf("."), "_rep").toString()));

                //判断是否修改了name\realName\nickname这三个字段的任一个,如果改了就要:
                if(!StrUtil.isEmpty(user.getName())||!StrUtil.isEmpty(user.getRealName())||!StrUtil.isEmpty(user.getNickname())){
                    //1)构建只有四个字段的json对象,用于生成二维码
                    JSONObject jsonUser = new JSONObject();
                    jsonUser.set("id",user.getId());
                    jsonUser.set("name",user.getName());
                    jsonUser.set("realName",user.getRealName());
                    jsonUser.set("nickname",user.getNickname());
                    //生成指定url对应的二维码到文件，宽和高都是300像素
                    String qrCodeFileUrl="/userInfo/user_"+user.getId()+"/id="+user.getId()+"_qrCode"+System.currentTimeMillis()+".jpg";
                    QrCodeUtil.generate(jsonUser.toString(),ServiceConstant.USER_QRCODE_WIDTH,
                            ServiceConstant.USER_QRCODE_HEIGHT,FileUtil.file(webUploadPath+qrCodeFileUrl));
                    user.setQrCode(pathPrefix+qrCodeFileUrl);
                    //删除当前用户的旧的二维码图片
                    boolean delQrCode = FileUtil.del(webUploadPath+oldQrCode.replace(pathPrefix,""));
                    //2)查看redis缓存中是否有当前用户,如果有就要更新redis缓存的数据
                    String userInfoKey= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+user.getId();
                    Object redisObj =baseMapper.getCache(userInfoKey);
                    if (redisObj!=null&&!"".equals(redisObj)){
                        com.planet.module.authManage.entity.redis.UserInfo redisUser=(com.planet.module.authManage.entity.redis.UserInfo)redisObj;
                        //a1:如果缓存中有,则要update此用户的userId:userInfo信息
                        redisUser.setName(user.getName());
                        redisUser.setRealName(user.getRealName());
                        redisUser.setNickname(user.getNickname());
                        baseMapper.updateCache(userInfoKey,redisUser,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
                    }
//                    Object r =baseMapper.getCache(userInfoKey);
                }
            });
        }else{
            //回滚
            throw new RuntimeException("新增用户失败,事务回滚");
        }
        //进行更新操作
        boolean b1 = updateBatchById(rightList);
        if(b1){
            if(errorResults.size()>0){//存在字段重复性记录
                return new RspResult(RspResultCode.FIELDS_REPEAT_ERROR,errorResults);
            }
            return RspResult.SUCCESS;
        }else{
            //回滚
            throw new RuntimeException("新增用户失败,事务回滚");
        }
    }

    @Transactional
    @Override
    @SysUserOperationMethodLog(MethodType= MethodType.DELETE,parameterType= ParameterType.List)
    public Integer deletesByIds(List<Long> ids) {
        //0.将要删除的记录先查出来进行判断:看是否已经被删除了,如果已经被删除了,为了保险起见,直接返回错误信息让用户重新确认后再操作
        List<UserInfo> userInfos = (List<UserInfo>)listByIds(ids);
        if(userInfos==null||userInfos.size()!=ids.size()){
            return null;
        }
        //1.删除记录
        boolean b = removeByIds(ids);
        if(!b){
            throw new RuntimeException("删除用户失败,事务回滚");
        }
        //2.删除关联记录
        int userRoleRsDelete = userRoleRsMapper.delete(new QueryWrapper<UserRoleRs>().in("user_id", ids));
        //3.删除二维码文件,修改用户文件夹的名称(删除标识的文件夹后面会被定时任务定时移动到ftp管理的文件库中)
        userInfos.stream().forEach(userInfo->{
            //1)删除二维码文件
            String qrCode=userInfo.getQrCode();
            //删除当前用户的旧的二维码图片
            String pathPrefix=mvcStaticPathPattern.substring(0,mvcStaticPathPattern.lastIndexOf("/"));
            boolean delQrCode = FileUtil.del(webUploadPath+qrCode.replace(pathPrefix,""));
            //2)将用户文件夹改名:_del后缀
            Long userId=userInfo.getId();
            String folderUrl=webUploadPath+"/userInfo/user_"+userId;
            boolean del = new File(folderUrl).renameTo(new File(folderUrl + "_del"));
            //4.查看redis缓存中是否有当前用户,如果有就要更新redis缓存的数据
            //1)清空userInfo
            String userInfoKey= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
            baseMapper.removeCache(userInfoKey);
            //2)清空userSession
            shiroService.deleteUserSessionByUserId(userId);
            //3)清空userRoles
            String userRolesKey=UtilsConstant.REDIS_USER_ID_FOR_ROLES_PERMITS+userId;
            baseMapper.removeCache(userRolesKey);
            //4)清空userFunctions
            String userFunctionsKey=UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
            baseMapper.removeCache(userFunctionsKey);
        });

        return ids.size();
    }

    @Override
    public List<UserInfo> selectsByIds(List<Long> ids) {
        List<UserInfo> userInfos = (List<UserInfo>)listByIds(ids);
        if(userInfos!=null&&userInfos.size()>0){
            userInfos.stream().forEach(userInfo -> {
                //密码和盐不返回
                userInfo.setPassword(null);
                userInfo.setSalt(null);
            });
        }
        return userInfos;
    }

    @Override
    public IPage<UserInfo> selectsByPage(UserInfo t) {
        Page<UserInfo> page = new Page(t.getCurrent(), t.getSize());
        //这里先做出基本的属性遍历查询,以后要加入模糊、大小等各种可能出现的情况的判断方式
        QueryWrapper<UserInfo> tQueryWrapper = new QueryWrapper<>();
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
            if("key".equals(key)){
                //标准模糊查询
                //tQueryWrapper.like("name", name).or().like("lastname", name)
                Map<String, Object> finalStringObjectMap1 = stringObjectMap;
                tQueryWrapper.nested(i->{
                    i.like("name", finalStringObjectMap1.get(key)).or().
                            like("code", finalStringObjectMap1.get(key)).or().
                            like("real_name", finalStringObjectMap1.get(key)).or().
                            like("nickname", finalStringObjectMap1.get(key));
                });
                continue;
            }
            tQueryWrapper.eq(key,stringObjectMap.get(key));
        }

        IPage<UserInfo> pageData = page(page, tQueryWrapper.orderByDesc("updatime"));
        pageData.getRecords().stream().forEach(userInfo -> {
            userInfo.setPassword(null);
            userInfo.setSalt(null);
        });
        return pageData;
    }

    @Transactional
    @Override
    public RspResult excelImport(MultipartFile excelFile) {
        InputStream in=null;
        try {
            in = excelFile.getInputStream();
            BaseRowReadListener<com.planet.module.authManage.entity.excel.UserInfo> baseRowReadListener=new BaseRowReadListener<>(
                    readBatchCount,baseExcelImportValid,baseDao,userInfoExcelToPoConverter
            );
            Class<?>[] parameterTypes=new Class[2];
            parameterTypes[0]=MultipartFile[].class;
            parameterTypes[1]=List.class;
            baseRowReadListener.setParameterTypes(parameterTypes);
            ExcelReaderBuilder workBook = EasyExcel.read(in, com.planet.module.authManage.entity.excel.UserInfo.class, baseRowReadListener);
            // 封装工作表
            ExcelReaderSheetBuilder sheet1 = workBook.sheet();
            // 读取
            sheet1.doRead();

            //3.要拿出BaseReadListener对象中的resultCode和unqualifiedRows来获知校验和持久化的结果:这两个值可以直接返回给前端,让前端展示相应的效果给用户
            Result<com.planet.module.authManage.entity.excel.UserInfo> result = baseRowReadListener.getResult();
            System.out.println(result);
            int resultCode = result.getResultCode();
            if(result.getResultCode()==0){//导入成功
                return RspResult.SUCCESS;
            }else{//将失败结果返回
                return new RspResult(result);
            }
        }catch (IOException e) {
            e.printStackTrace();
            log.error("用户记录导入失败");
            throw new RuntimeException("用户记录导入失败,事务回滚");
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void excelExport(UserInfo t) {
        HttpServletResponse response = WebUtil.getResponse();
        // 1.模板
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(
                "templates/excel/modular/用户信息模块-模板.xlsx");

        // 2.目标文件
        String targetFile = "用户信息模块-记录.xlsx";

        //3.模型实体类的类对象
        Class userInfoClass= com.planet.module.authManage.entity.excel.UserInfo.class;

        // 4.写入workbook对象
        ExcelWriter workBook =null;
        try {
            workBook = EasyExcel.write(response.getOutputStream(),userInfoClass).withTemplate(templateInputStream).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //5.准备工作表和对应的数据
        WriteSheet sheet = EasyExcel.writerSheet().build();

        //6.获取数据
        QueryWrapper<UserInfo> tQueryWrapper = new QueryWrapper<>();
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
        List<UserInfo> list=list(tQueryWrapper.orderByAsc("id"));
        List<com.planet.module.authManage.entity.excel.UserInfo> userInfos = userInfoExcelToPoConverter.convertPoToExcel(list);

        //7. 写入数据

        workBook.fill(userInfos, sheet);

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
