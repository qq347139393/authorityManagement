package com.planet.module.authManage.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.constant.LocalCacheConstantService;
import com.planet.common.constant.ServiceConstant;
import com.planet.common.constant.UtilsConstant;
import com.planet.common.util.RspResult;
import com.planet.common.util.RspResultCode;
import com.planet.module.authManage.dao.mysql.mapper.AccountLogMapper;
import com.planet.module.authManage.dao.mysql.mapper.ConfigureSysMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserInfoMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserRoleRsMapper;
import com.planet.module.authManage.dao.redis.BaseMapper;
import com.planet.module.authManage.entity.mysql.AccountLog;
import com.planet.module.authManage.entity.mysql.FunctionInfo;
import com.planet.module.authManage.entity.redis.UserFunctionRs;
import com.planet.module.authManage.entity.redis.UserInfo;
import com.planet.module.authManage.entity.redis.UserRoleRs;
import com.planet.module.authManage.service.AccountModuleService;
import com.planet.module.authManage.service.UserInfoService;
import com.planet.module.authManage.service.authByShiro.ShiroService;
import com.planet.system.authByShiro.customSettings.CustomUserToken;
import com.planet.system.fieldsRepeatCheck.FieldsRepeatCheckResult;
import com.planet.system.fieldsRepeatCheck.FieldsRepeatCheckUtil;
import com.planet.util.jdk8.Base64Util;
import com.planet.util.jdk8.VerificationCodeByPictureUtil;
import com.planet.util.shiro.DigestsUtil;
import com.planet.util.shiro.ShiroUtil;
import com.planet.util.JwtUtil;
import com.planet.util.springBoot.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountModuleServiceImpl implements AccountModuleService {
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserRoleRsMapper userRoleRsMapper;
    @Autowired
    private ShiroService shiroService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private AccountLogMapper accountLogMapper;
    @Autowired
    private ConfigureSysMapper configureSysMapper;

    @Override
    public RspResult login(String name, String password,String verificationCode) {
        //0.获取验证码,进行判断,如果错误直接返回错误信息
        Boolean flag = LocalCacheConstantService.getValue("account:verificationCodeFlag", Boolean.class);
        Object verificationCodeCache=null;
        if(flag){//为true表示启用了验证码
            String sessionIdVerificationCode="sessionIdVerificationCode:"+WebUtil.getSession().getId();
            verificationCodeCache= baseMapper.getCache(sessionIdVerificationCode);
            if(verificationCode==null||verificationCodeCache==null||!verificationCode.equalsIgnoreCase(verificationCodeCache.toString())){
                return RspResult.VERIFICATION_CODE_ERROR;
            }
            //如果验证码合法,则要将此验证码立刻作废掉
            baseMapper.removeCache(sessionIdVerificationCode);
        }

        String jwtToken = null;
        UserInfo userInfo =null;
        try {
            //1.进行登录判断,由Shiro的约定方法进行登录判断
            CustomUserToken userToken=new CustomUserToken(name,password);
            Subject subject = SecurityUtils.getSubject();
            subject.login(userToken);
            //2.登录完成之后需要颁发令牌
            String sessionId = ShiroUtil.getShiroSessionId();
            userInfo = (UserInfo)ShiroUtil.getPrincipal();
            Long userId=userInfo.getId();
            Map<String,Object> claims = new HashMap<>();
            claims.put("userId",userId);
            claims.put("name",userInfo.getName());
            jwtToken = JwtUtil.createJwt(UtilsConstant.JWT_ISS, LocalCacheConstantService.getValue("redis:ttlJwtMillisecond",Long.class),
                    sessionId, claims);
            //3.重载当前登录成功的用户的Redis缓存 --更新键值对的时间的技巧:就是查出来后再update原样放回,就能以简单的方式刷新失效时间了
            //1)userId:sessionId的
            shiroService.userSessionManage(userId,sessionId);
            //2)userId:userInfo的
            String userInfoKey= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
            Object user = baseMapper.getCache(userInfoKey);
            if (user!=null&&!"".equals(user)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userInfoKey,(UserInfo)user,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else{
                //a2:缓存中没有,则去数据库中查询用户信息
                com.planet.module.authManage.entity.mysql.UserInfo mysqlUserInfo = userInfoMapper.selectById(userId);
                UserInfo redisUser=null;
                if(mysqlUserInfo!=null){
                    //b1:将查询的mysql的user对象转成redis的user对象
                    redisUser=new UserInfo();
                    redisUser.setId(mysqlUserInfo.getId());
                    redisUser.setName(mysqlUserInfo.getName());
                    redisUser.setRealName(mysqlUserInfo.getRealName());
                    redisUser.setNickname(mysqlUserInfo.getNickname());
                    //b2:将数据库的用户信息更新到缓存中
                    baseMapper.creatCache(userInfoKey,redisUser,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
                }
            }
            //3)userId:userRoles的
            //1.先从redis缓存中查询用户信息
            String userRolesKey= UtilsConstant.REDIS_USER_ID_FOR_ROLES_PERMITS+userId;
            Object userRoleRs = baseMapper.getCache(userRolesKey);
            if (userRoleRs!=null&&!"".equals(userRoleRs)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userRolesKey,(UserRoleRs)userRoleRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else{
                //a2:缓存中没有,则去数据库中查询用户信息
                List<Long> userIds=new ArrayList<>();
                userIds.add(userId);
                List<com.planet.module.authManage.entity.mysql.UserRoleRs> mysqlUserRoleRs = userRoleRsMapper.selectsByUserIdsGroupUserId(userIds);
                UserRoleRs redisUserRoleRs=null;
                if(mysqlUserRoleRs!=null &&mysqlUserRoleRs.size()>0 ){
                    //b1:将查询的mysql的UserRoleRs对象转成redis的UserRoleRs对象
                    redisUserRoleRs=new UserRoleRs();
                    List<String> rolePermits = mysqlUserRoleRs.stream().map(mysqlUserRole -> mysqlUserRole.getRoleInfo().getPermit()).collect(Collectors.toList());
                    redisUserRoleRs.setRolePermits(rolePermits);
                    redisUserRoleRs.setUserId(userId);
                    //b2:将数据库的用户信息更新到缓存中
                    baseMapper.creatCache(userRolesKey,redisUserRoleRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
                }
            }
            //4)userId:userFunctions的
            //1.先从redis缓存中查询用户信息
            String userFunctionsKey= UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
            Object userFunctionRs = baseMapper.getCache(userFunctionsKey);
            if (userFunctionRs!=null&&!"".equals(userFunctionRs)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userFunctionsKey,userFunctionRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else{
                //a2:缓存中没有,则去数据库中查询用户信息
                List<Long> userIds=new ArrayList<>();
                userIds.add(userId);
                List<FunctionInfo> functionInfos = userInfoMapper.selectFunctionsByUserIds(userIds);
                UserFunctionRs redisUserFunctionRs=null;
                if(functionInfos!=null &&functionInfos.size()>0 ){
                    //b1:将查询的mysql的functionInfos对象转成redis的UserFunctionRs对象
                    redisUserFunctionRs=new UserFunctionRs();
                    //只存permit不为空的属性,因为这种才是给后端shiro用的
                    List<String> functionPermits = functionInfos.stream().filter(functionInfo -> !StrUtil.isEmpty(functionInfo.getPermit())).map(
                            functionInfo -> functionInfo.getPermit()
                    ).collect(Collectors.toList());

                    redisUserFunctionRs.setFunctionPermits(functionPermits);
                    redisUserFunctionRs.setUserId(userId);
                    //b2:将数据库的用户信息更新到缓存中
                    baseMapper.creatCache(userFunctionsKey,redisUserFunctionRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
                }
            }
        }catch (Exception e){
            //登陆失败
            e.printStackTrace();
            if(e instanceof UnknownAccountException){
                log.error("账号不存在");
                return RspResult.ACCOUNT_NON_EXISTENT;
            }else if(e instanceof IncorrectCredentialsException){
                log.error("账号的密码错误");
                return RspResult.PASSWORD_WRONG;
            }
            return RspResult.SYS_ERROR;
        }

        //插入登录后的accountLog记录
        AccountLog accountLog=new AccountLog();
        accountLog.setUserId(userInfo.getId());
        accountLog.setName(userInfo.getName());
        accountLog.setMethod("login");
        accountLog.setContent("("+userInfo.getId()+")("+userInfo.getName()+")("+verificationCodeCache+")");
        accountLog.setCreator(userInfo.getName());
        accountLogMapper.insert(accountLog);
        //拿出来放session中,等后面用户登出时(session失效或删除时)可以从session中拿到accountLogId然后匹配log记录后修改其updatime值作为登出时间与登录时间配对
        Long accountLogId=accountLog.getId();
        ShiroUtil.getShiroSession().setAttribute("accountLogId",accountLogId);

        //登陆成功,将生成的jwtToken放入响应数据中返回
        //新的数据格式,除了jwtToken之外还要有

        //构建动态路由列表(这个其实是在数据库查出来并构建树形结构的,这里我们只是做个基本的演示)
        //这里我们先写死,等后面完成整个页面的效果后,再计算权限并添加全部权限列表

        //由于这个项目中后端负责权限数据的处理构建,所以本着[单边全权处理原则]:后端还要将路由排序直接处理好,让前端拿到数据后直接放到v-model指定的对象中展示即可(这个等后面真正从数据库拿权限时我们会处理)
        List<FunctionInfo> functionInfos = new ArrayList<>();
        FunctionInfo functionInfo=new FunctionInfo();
        functionInfo.setIcon("el-icon-setting");
        functionInfo.setName("首页展示");
        functionInfo.setPath("/home");
        functionInfos.add(functionInfo);

        FunctionInfo functionInfo1=new FunctionInfo();
        functionInfo1.setIcon("el-icon-menu");
        functionInfo1.setName("用户管理");
        functionInfo1.setPath("/users");
        functionInfos.add(functionInfo1);

        FunctionInfo functionInfo2=new FunctionInfo();
        functionInfo2.setIcon("el-icon-setting");
        functionInfo2.setName("角色管理");
        functionInfo2.setPath("/roles");
        functionInfos.add(functionInfo2);

        FunctionInfo functionInfo3=new FunctionInfo();
        functionInfo3.setIcon("el-icon-menu");
        functionInfo3.setName("权限管理");
        functionInfo3.setPath("/functions");
        functionInfos.add(functionInfo3);

        FunctionInfo functionInfo4=new FunctionInfo();
        functionInfo4.setIcon("el-icon-setting");
        functionInfo4.setName("报表统计");
        functionInfo4.setPath("/statisticses");
        functionInfos.add(functionInfo4);

        FunctionInfo functionInfo5_1=new FunctionInfo();
        functionInfo5_1.setIcon("el-icon-setting");
        functionInfo5_1.setName("账号历史");
        functionInfo5_1.setPath("/accountLogs");

        FunctionInfo functionInfo5_2=new FunctionInfo();
        functionInfo5_2.setIcon("el-icon-setting");
        functionInfo5_2.setName("用户历史");
        functionInfo5_2.setPath("/userLogs");

        FunctionInfo functionInfo5_3=new FunctionInfo();
        functionInfo5_3.setIcon("el-icon-setting");
        functionInfo5_3.setName("角色历史");
        functionInfo5_3.setPath("/roleLogs");

        FunctionInfo functionInfo5_4=new FunctionInfo();
        functionInfo5_4.setIcon("el-icon-setting");
        functionInfo5_4.setName("权限历史");
        functionInfo5_4.setPath("/functionLogs");

        FunctionInfo functionInfo5=new FunctionInfo();
        functionInfo5.setIcon("el-icon-menu");
        functionInfo5.setName("历史记录");
        functionInfo5.setPath("1");//这里虽然没有路由跳转,但是这个值少不了:这个值可以用来让前端区分用户点击的是哪个导航项
        functionInfo5.setChildren(Arrays.asList(functionInfo5_1,functionInfo5_2,functionInfo5_3,functionInfo5_4));
        functionInfos.add(functionInfo5);

        FunctionInfo functionInfo6_1=new FunctionInfo();
        functionInfo6_1.setIcon("el-icon-setting");
        functionInfo6_1.setName("账号设置");
        functionInfo6_1.setPath("/accountSet");

        FunctionInfo functionInfo6_2=new FunctionInfo();
        functionInfo6_2.setIcon("el-icon-setting");
        functionInfo6_2.setName("dao设置");
        functionInfo6_2.setPath("/daoSet");

        FunctionInfo functionInfo6_3=new FunctionInfo();
        functionInfo6_3.setIcon("el-icon-setting");
        functionInfo6_3.setName("统计设置");
        functionInfo6_3.setPath("/statisticsSet");

        FunctionInfo functionInfo6_4=new FunctionInfo();
        functionInfo6_4.setIcon("el-icon-setting");
        functionInfo6_4.setName("redis设置");
        functionInfo6_4.setPath("/redisSet");

        FunctionInfo functionInfo6_5=new FunctionInfo();
        functionInfo6_5.setIcon("el-icon-setting");
        functionInfo6_5.setName("定时任务设置");
        functionInfo6_5.setPath("/scheduleTaskSet");

        FunctionInfo functionInfo6=new FunctionInfo();
        functionInfo6.setIcon("el-icon-setting");
        functionInfo6.setName("系统配置");
        functionInfo6.setPath("2");
        functionInfo6.setChildren(Arrays.asList(functionInfo6_1,functionInfo6_2,functionInfo6_3,functionInfo6_4,functionInfo6_5));
        functionInfos.add(functionInfo6);

        FunctionInfo functionInfo7=new FunctionInfo();
        functionInfo7.setIcon("el-icon-menu");
        functionInfo7.setName("个人账号");
        functionInfo7.setPath("/account");
        functionInfos.add(functionInfo7);

        //将jwtToken和动态路由列表都返回给前端
        Map<String,Object> data=new HashMap<>();
        data.put("jwtToken",jwtToken);
        data.put("functions",functionInfos);

        return new RspResult(data);//登陆成功,返回jwtToken和动态路由列表
    }

    @Override
    public RspResult getVeriCodeByPic() {
        try {
            //1.获取验证码的图片和信息
            Object[] verificationCodes = VerificationCodeByPictureUtil.createVerificationCodeAndPicture(0, 0, 0, 0, 0);
            //2.将信息配对sessionId存入redis缓存中
            String sessionId = WebUtil.getSession().getId();
            String sessionIdVerificationCode="sessionIdVerificationCode:"+sessionId;
            //先清除之前的验证码信息
            baseMapper.removeCache(sessionIdVerificationCode);
            //再创建新的验证码信息
            baseMapper.creatCache(sessionIdVerificationCode,verificationCodes[0].toString(),LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));

            // 转换流信息写出
            FastByteArrayOutputStream os = new FastByteArrayOutputStream();
            ImageIO.write((BufferedImage) verificationCodes[1], "jpeg", os);
            Map<String,Object> imgMap=new HashMap<>();
            imgMap.put("imgBase64", Base64Util.encode(os.toByteArray()));
            return new RspResult(imgMap);
        } catch (IOException e) {
            e.printStackTrace();
            return RspResult.SYS_ERROR;
        }
    }


    @Override
    public RspResult selectUserByMyId() {
        //a1:获得shiro本地缓存中的当前用户对象
        Object principal = ShiroUtil.getPrincipal();
        //a2:获取当前用户对象的userId
        Long userId = JSONUtil.parseObj(principal).get("id",Long.class);
        com.planet.module.authManage.entity.mysql.UserInfo userInfo = userInfoMapper.selectById(userId);
        if(userInfo==null){
            return RspResult.USER_NULL;
        }
        //密码和盐隐藏掉
        userInfo.setPassword(null);
        userInfo.setSalt(null);
        return new RspResult(userInfo);
    }

    @Override
    public RspResult isGetVeriCodeByPic() {
        return new RspResult(LocalCacheConstantService.getValue("account:verificationCodeFlag",Boolean.class));
    }

    @Override
    public RspResult checkToken(com.planet.module.authManage.entity.mysql.UserInfo user) {
        //先判断jwtToken是否合法
        boolean b = JwtUtil.checkJwt(user.getJwtToken());
        if(b){
            //再判断用户的name是否真实存在..以防用户盗取他人的jwtToken放到自己的浏览器上冒充
            user = userInfoMapper.selectOne(new QueryWrapper<com.planet.module.authManage.entity.mysql.UserInfo>().eq("name", user.getName()));
            if(user!=null){
                return RspResult.SUCCESS;
            }
        }
        return RspResult.JWT_TOKEN_EMBEZZLE;
    }

    @Override
    public RspResult updateByMyIds(MultipartFile multipartFile, String usersJson) {
        JSONObject jsonObject = JSONUtil.parseObj(usersJson);
        com.planet.module.authManage.entity.mysql.UserInfo userInfo = JSONUtil.toBean(jsonObject, com.planet.module.authManage.entity.mysql.UserInfo.class);
        //密码和盐不能改
        userInfo.setPassword(null);
        userInfo.setSalt(null);
        //a1:获得shiro本地缓存中的当前用户对象
        Object principal = ShiroUtil.getPrincipal();
        //a2:获取当前用户对象的userId
        Long userId = JSONUtil.parseObj(principal).get("id",Long.class);
        userInfo.setId(userId);//只能修改自己的,所以这里的id应该是从缓存中读取
        //这里会调用反射的方法,所以这里不要用Arrays.asList方法创建ArrayList对象,以免创建的集合对象的类型不匹配

        List<com.planet.module.authManage.entity.mysql.UserInfo> userInfos=new ArrayList<>();
        userInfos.add(userInfo);
        List<String> fieldNames=new ArrayList<>();
        fieldNames.add("name");//name字段不可重复
        fieldNames.add("code");//code字段不可重复
        FieldsRepeatCheckResult<com.planet.module.authManage.entity.mysql.UserInfo> result = FieldsRepeatCheckUtil.fieldsRepeatCheck(userInfoMapper, ServiceConstant.FIELDS_REPEAT_CHECK_METHOD, userInfo, fieldNames, FieldsRepeatCheckUtil.UPDATE);
        //只有一条记录,所以不用考虑分出合格不合格的记录
        if(result.getResult()){//出现字段值重复,禁止操作
            return new RspResult(RspResultCode.FIELDS_REPEAT_ERROR,result);
        }
        RspResult rspResult = userInfoService.updatesByIds(new MultipartFile[]{multipartFile}, userInfos);

        if(rspResult==null||!rspResult.getCode().equals("000000")){
            return RspResult.FAILED;
        }

        //插入修改个人信息成功后的accountLog记录
        List<com.planet.module.authManage.entity.mysql.UserInfo> newUserInfos = userInfoService.selectsByIds(Arrays.asList(userId));
        AccountLog accountLog=new AccountLog();
        accountLog.setUserId(userId);
        accountLog.setName(newUserInfos.get(0).getName());
        accountLog.setMethod("updateByMyIds");
        accountLog.setContent("("+multipartFile+")("+usersJson+")");
        accountLog.setCreator(newUserInfos.get(0).getName());
        accountLogMapper.insert(accountLog);

        return RspResult.SUCCESS;
    }

    @Override
    public boolean updateMyPassword(com.planet.module.authManage.entity.mysql.UserInfo t) {
        if(t.getPassword().equals(t.getNewPassword())){
            return false;
        }
        //a1:获得shiro本地缓存中的当前用户对象
        Object principal = ShiroUtil.getPrincipal();
        //a2:获取当前用户对象的userId
        Long userId = JSONUtil.parseObj(principal).get("id",Long.class);
        com.planet.module.authManage.entity.mysql.UserInfo userInfo = userInfoMapper.selectById(userId);

        boolean b = DigestsUtil.checkPassword(userInfo.getPassword(), t.getPassword(), userInfo.getSalt());
        if(!b){//对比失败
            return false;
        }
        Map<String, String> map = DigestsUtil.encryptPassword(t.getNewPassword());
        t.setPassword(map.get("password"));
        t.setSalt(map.get("salt"));
        t.setId(userId);
        int i = userInfoMapper.updateById(t);

        //插入修改个人信息成功后的accountLog记录
        AccountLog accountLog=new AccountLog();
        accountLog.setUserId(userId);
        accountLog.setName(userInfo.getName());
        accountLog.setMethod("updateMyPassword");
        accountLog.setContent("("+(i==1?true:false)+")");
        accountLog.setCreator(userInfo.getName());
        accountLogMapper.insert(accountLog);

        if(i<=0){
            return false;
        }
        return true;

    }

    //=======下面方法,会查询或更新redis缓存========

    @Override
    public List<UserInfo> selectOrUpdateRedisUserByUserIds(List<Long> userIds) {
        if(userIds==null||userIds.size()==0){
            log.error("根据用户id从redis缓存中获取对应的用户信息失败:id不能为空");
            return null;
        }
        return userIds.stream().map(userId->{
            //1.先从redis缓存中查询用户信息
            String key= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
            Object user = baseMapper.getCache(key);
            if (user!=null&&!"".equals(user)){
                return (UserInfo) user;
            }
            //2.缓存中没有,则去数据库中查询用户信息
            com.planet.module.authManage.entity.mysql.UserInfo userInfo = userInfoMapper.selectById(userId);
            UserInfo redisUser=null;
            if(userInfo!=null){
                //1)将查询的mysql的user对象转成redis的user对象
                redisUser=new UserInfo();
                redisUser.setId(userInfo.getId());
                redisUser.setName(userInfo.getName());
                redisUser.setRealName(userInfo.getRealName());
                redisUser.setNickname(userInfo.getNickname());
                //2)将数据库的用户信息更新到缓存中
                baseMapper.creatCache(key,redisUser,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }

            //3)返回redis的user对象
            return redisUser;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserRoleRs> selectOrUpdateRedisRolesByUserIds(List<Long> userIds) {
        if(userIds==null||userIds.size()==0){
            log.error("根据用户id从redis缓存中获取对应的全部角色数据失败:id不能为空");
            return null;
        }
        return userIds.stream().map(userId->{
            //1.先从redis缓存中查询用户信息
            String key= UtilsConstant.REDIS_USER_ID_FOR_ROLES_PERMITS+userId;
            Object userRoleRs = baseMapper.getCache(key);
            if (userRoleRs!=null&&!"".equals(userRoleRs)){
                return (UserRoleRs) userRoleRs;
            }
            //2.缓存中没有,则去数据库中查询用户信息
            List<com.planet.module.authManage.entity.mysql.UserRoleRs> mysqlUserRoleRs = userRoleRsMapper.selectsByUserIdsGroupUserId(Arrays.asList(userId));
            UserRoleRs redisUserRoleRs=null;
            if(mysqlUserRoleRs!=null &&mysqlUserRoleRs.size()>0 ){
                //1)将查询的mysql的UserRoleRs对象转成redis的UserRoleRs对象
                redisUserRoleRs=new UserRoleRs();
                List<String> rolePermits = mysqlUserRoleRs.stream().map(mysqlUserRole -> mysqlUserRole.getRoleInfo().getPermit()).collect(Collectors.toList());
                redisUserRoleRs.setRolePermits(rolePermits);
                redisUserRoleRs.setUserId(userId);
                //2)将数据库的用户信息更新到缓存中
                baseMapper.creatCache(key,redisUserRoleRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }

            //3)返回redis的user对象
            return redisUserRoleRs;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserFunctionRs> selectOrUpdateRedisFunctionsByUserIds(List<Long> userIds) {
        if(userIds==null||userIds.size()==0){
            log.error("根据用户id从redis缓存中获取对应的全部权限数据失败:id不能为空");
            return null;
        }
        return userIds.stream().map(userId->{
            //1.先从redis缓存中查询用户信息
            String key= UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
            Object userFunctionRs = baseMapper.getCache(key);
            if (userFunctionRs!=null&&!"".equals(userFunctionRs)){
                return (UserFunctionRs) userFunctionRs;
            }
            //2.缓存中没有,则去数据库中查询用户信息
            List<FunctionInfo> functionInfos = userInfoMapper.selectFunctionsByUserIds(Arrays.asList(userId));
            UserFunctionRs redisUserFunctionRs=null;
            if(functionInfos!=null &&functionInfos.size()>0 ){
                //1)将查询的mysql的functionInfos对象转成redis的UserFunctionRs对象
                redisUserFunctionRs=new UserFunctionRs();
                //只存permit不为空的属性,因为这种才是给后端shiro用的
                List<String> functionPermits = functionInfos.stream().filter(functionInfo -> !StrUtil.isEmpty(functionInfo.getPermit())).map(
                        functionInfo -> functionInfo.getPermit()
                ).collect(Collectors.toList());
                redisUserFunctionRs.setFunctionPermits(functionPermits);
                redisUserFunctionRs.setUserId(userId);
                //2)将数据库的用户信息更新到缓存中
                baseMapper.creatCache(key,redisUserFunctionRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }

            //3)返回redis的user对象
            return redisUserFunctionRs;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean updateRedisUserByUserIds(List<Long> userIds) {
        if(userIds==null||userIds.size()==0){
            log.error("根据用户id更新用户的用户缓存信息失败:id不能为空");
            return false;
        }
        userIds.stream().forEach(userId->{
            //1)获取当前用户的个人信息
            com.planet.module.authManage.entity.mysql.UserInfo mysqlUserInfo = userInfoMapper.selectById(userId);
            //2)更新用户的用户缓存:mysql是新的,redis是旧的
            String userInfoKey= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
            Object userInfo = baseMapper.getCache(userInfoKey);
            //a1:mysql有,redis有->更新
            if(mysqlUserInfo!=null&&userInfo!=null){
                UserInfo redisUserInfo=new UserInfo();
                redisUserInfo.setId(mysqlUserInfo.getId());
                redisUserInfo.setName(mysqlUserInfo.getName());
                redisUserInfo.setCode(mysqlUserInfo.getCode());
                redisUserInfo.setRealName(mysqlUserInfo.getRealName());
                redisUserInfo.setNickname(mysqlUserInfo.getNickname());
                baseMapper.updateCache(userInfoKey,redisUserInfo,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else if(mysqlUserInfo!=null&&userInfo==null){//a2:mysql有,redis无->新增
                UserInfo redisUserInfo=new UserInfo();
                redisUserInfo.setId(mysqlUserInfo.getId());
                redisUserInfo.setName(mysqlUserInfo.getName());
                redisUserInfo.setCode(mysqlUserInfo.getCode());
                redisUserInfo.setRealName(mysqlUserInfo.getRealName());
                redisUserInfo.setNickname(mysqlUserInfo.getNickname());
                baseMapper.creatCache(userInfoKey,redisUserInfo,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else if(mysqlUserInfo==null&&userInfo!=null){//a3:mysql无,redis有->删除
                baseMapper.removeCache(userInfoKey);
            }else{//a4:mysql无,redis无->不操作
                log.info("mysql无,redis无->[用户-角色]缓存不操作");
            }
        });
        return true;
    }

    @Override
    public boolean updateRedisRolesByUserIds(List<Long> userIds) {
        if(userIds==null||userIds.size()==0){
            log.error("根据用户id更新用户的角色列表缓存信息失败:id不能为空");
            return false;
        }
        userIds.stream().forEach(userId->{
            //1)获取当前用户的全部角色列表
            List<com.planet.module.authManage.entity.mysql.UserRoleRs> mysqlUserRoleRs = userRoleRsMapper.selectsByUserIdsGroupUserId(Arrays.asList(userId));
            //2)更新用户的角色缓存:mysql是新的,redis是旧的
            String userRolesKey= UtilsConstant.REDIS_USER_ID_FOR_ROLES_PERMITS+userId;
            Object userRoleRs = baseMapper.getCache(userRolesKey);
            //a1:mysql有,redis有->更新
            if((mysqlUserRoleRs!=null &&mysqlUserRoleRs.size()>0)&&userRoleRs!=null){
                com.planet.module.authManage.entity.redis.UserRoleRs redisUserRoleRs=new com.planet.module.authManage.entity.redis.UserRoleRs();
                List<String> rolePermits = mysqlUserRoleRs.stream().map(mysqlUserRole -> mysqlUserRole.getRoleInfo().getPermit()).collect(Collectors.toList());
                redisUserRoleRs.setRolePermits(rolePermits);
                redisUserRoleRs.setUserId(userId);
                baseMapper.updateCache(userRolesKey,redisUserRoleRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else if((mysqlUserRoleRs!=null &&mysqlUserRoleRs.size()>0)&&userRoleRs==null){//a2:mysql有,redis无->新增
                com.planet.module.authManage.entity.redis.UserRoleRs redisUserRoleRs=new com.planet.module.authManage.entity.redis.UserRoleRs();
                List<String> rolePermits = mysqlUserRoleRs.stream().map(mysqlUserRole -> mysqlUserRole.getRoleInfo().getPermit()).collect(Collectors.toList());
                redisUserRoleRs.setRolePermits(rolePermits);
                redisUserRoleRs.setUserId(userId);
                baseMapper.creatCache(userRolesKey,redisUserRoleRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else if((mysqlUserRoleRs==null ||mysqlUserRoleRs.size()==0)&&userRoleRs!=null){//a3:mysql无,redis有->删除
                baseMapper.removeCache(userRolesKey);
            }else{//a4:mysql无,redis无->不操作
                log.info("mysql无,redis无->[用户-角色]缓存不操作");
            }
        });
        return true;
    }

    @Override
    public boolean updateRedisFunctionsByUserIds(List<Long> userIds) {
        if(userIds==null||userIds.size()==0){
            log.error("根据用户id更新用户的权限列表缓存信息失败:id不能为空");
            return false;
        }
        userIds.stream().forEach(userId->{
            //1)获取当前用户的全部权限列表
            List<FunctionInfo> functionInfos = userInfoMapper.selectFunctionsByUserIds(Arrays.asList(userId));
            //2)更新用户的权限缓存:mysql是新的,redis是旧的
            String userFunctionsKey= UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
            Object userFunctionRs = baseMapper.getCache(userFunctionsKey);
            //a1:mysql有,redis有->更新
            if((functionInfos!=null &&functionInfos.size()>0)&&userFunctionRs!=null){
                UserFunctionRs redisUserFunctionRs=new UserFunctionRs();
                //只存permit不为空的属性,因为这种才是给后端shiro用的
                List<String> functionPermits = functionInfos.stream().filter(functionInfo -> !StrUtil.isEmpty(functionInfo.getPermit())).map(
                        functionInfo -> functionInfo.getPermit()
                ).collect(Collectors.toList());
                redisUserFunctionRs.setFunctionPermits(functionPermits);
                redisUserFunctionRs.setUserId(userId);
                baseMapper.updateCache(userFunctionsKey,redisUserFunctionRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else if((functionInfos!=null &&functionInfos.size()>0)&&userFunctionRs==null){//a2:mysql有,redis无->新增
                UserFunctionRs redisUserFunctionRs=new UserFunctionRs();
                List<String> functionPermits = functionInfos.stream().map(functionInfo -> functionInfo.getPermit()).collect(Collectors.toList());
                redisUserFunctionRs.setFunctionPermits(functionPermits);
                redisUserFunctionRs.setUserId(userId);
                baseMapper.creatCache(userFunctionsKey,redisUserFunctionRs,LocalCacheConstantService.getValue("redis:ttlRedisDaoMillisecond",Long.class));
            }else if((functionInfos==null ||functionInfos.size()==0)&&userFunctionRs!=null){//a3:mysql无,redis有->删除
                baseMapper.removeCache(userFunctionsKey);
            }else{//a4:mysql无,redis无->不操作
                log.info("mysql无,redis无->[用户-权限]缓存不操作");
            }
        });
        return true;
    }


}
