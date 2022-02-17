package com.planet.module.authManage.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.planet.common.constant.UtilsConstant;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.dao.mysql.mapper.AccountLogMapper;
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
import com.planet.system.authByShiro.customSettings.CustomUserToken;
import com.planet.module.authManage.service.authByShiro.UserShiroService;
import com.planet.util.jdk8.VerificationCodeByPictureUtil;
import com.planet.util.shiro.DigestsUtil;
import com.planet.util.shiro.ShiroUtil;
import com.planet.util.JwtUtil;
import com.planet.util.springBoot.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
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
    private UserShiroService userShiroService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private AccountLogMapper accountLogMapper;

    @Override
    public RspResult login(String name, String password,String verificationCode) {
        //0.获取验证码,进行判断,如果错误直接返回错误信息
        String sessionIdVerificationCode="sessionIdVerificationCode:"+WebUtil.getSession().getId();
        Object verificationCodeCache = baseMapper.getCache(sessionIdVerificationCode);
        if(verificationCode==null||verificationCodeCache==null||!verificationCode.equalsIgnoreCase(verificationCodeCache.toString())){
            return RspResult.FAILED;
        }
        //如果验证码合法,则要将此验证码立刻作废掉
        baseMapper.removeCache(sessionIdVerificationCode);

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
            jwtToken = JwtUtil.createJwt(UtilsConstant.JWT_ISS, UtilsConstant.TTL_JWT_MILLISECOND,
                    sessionId, claims);
            //3.重载当前登录成功的用户的Redis缓存 --更新键值对的时间的技巧:就是查出来后再update原样放回,就能以简单的方式刷新失效时间了
            //1)userId:sessionId的
            userShiroService.userSessionManage(userId,sessionId);
            //2)userId:userInfo的
            String userInfoKey= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
            Object user = baseMapper.getCache(userInfoKey);
            if (user!=null&&!"".equals(user)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userInfoKey,(UserInfo)user,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
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
                    baseMapper.creatCache(userInfoKey,redisUser,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
                }
            }
            //3)userId:userRoles的
            //1.先从redis缓存中查询用户信息
            String userRolesKey= UtilsConstant.REDIS_USER_ID_FOR_ROLES_PERMITS+userId;
            Object userRoleRs = baseMapper.getCache(userRolesKey);
            if (userRoleRs!=null&&!"".equals(userRoleRs)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userRolesKey,(UserRoleRs)userRoleRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
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
                    baseMapper.creatCache(userRolesKey,redisUserRoleRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
                }
            }
            //4)userId:userFunctions的
            //1.先从redis缓存中查询用户信息
            String userFunctionsKey= UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
            Object userFunctionRs = baseMapper.getCache(userFunctionsKey);
            if (userFunctionRs!=null&&!"".equals(userFunctionRs)){
                //a1:如果缓存中有,则要update下以便更新存活时间:只有每次登录成功后都要更新存活时间
                baseMapper.updateCache(userFunctionsKey,userFunctionRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
            }else{
                //a2:缓存中没有,则去数据库中查询用户信息
                List<Long> userIds=new ArrayList<>();
                userIds.add(userId);
                List<FunctionInfo> functionInfos = userInfoMapper.selectFunctionsByUserIds(userIds);
                UserFunctionRs redisUserFunctionRs=null;
                if(functionInfos!=null &&functionInfos.size()>0 ){
                    //b1:将查询的mysql的functionInfos对象转成redis的UserFunctionRs对象
                    redisUserFunctionRs=new UserFunctionRs();
                    List<String> functionPermits = functionInfos.stream().map(functionInfo -> functionInfo.getPermit()).collect(Collectors.toList());
                    redisUserFunctionRs.setFunctionPermits(functionPermits);
                    redisUserFunctionRs.setUserId(userId);
                    //b2:将数据库的用户信息更新到缓存中
                    baseMapper.creatCache(userFunctionsKey,redisUserFunctionRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
                }
            }
        }catch (Exception e){
            //登陆失败
            e.printStackTrace();
            return RspResult.FAILED;
        }

        //插入登录后的accountLog记录
        AccountLog accountLog=new AccountLog();
        accountLog.setUserId(userInfo.getId());
        accountLog.setName(userInfo.getName());
        accountLog.setMethod("login");
        accountLog.setContent("("+userInfo.getId()+")("+userInfo.getName()+")("+verificationCodeCache+")");
        accountLog.setCreator(userInfo.getName());
        accountLogMapper.insert(accountLog);

        //登陆成功,将生成的jwtToken放入响应数据中返回
        return new RspResult(jwtToken);
    }

    @Override
    public void getVeriCodeByPic() {
        HttpServletResponse response = WebUtil.getResponse();
        ServletOutputStream outputStream=null;
        try {
            outputStream = WebUtil.getResponse().getOutputStream();
            //1.获取验证码的图片和信息
            Object[] verificationCodes = VerificationCodeByPictureUtil.createVerificationCodeAndPicture(0, 0, 0, 0, 0);
            //2.将信息配对sessionId存入redis缓存中
            String sessionId = WebUtil.getSession().getId();
            String sessionIdVerificationCode="sessionIdVerificationCode:"+sessionId;
            //先清除之前的验证码信息
            baseMapper.removeCache(sessionIdVerificationCode);
            //再创建新的验证码信息
            baseMapper.creatCache(sessionIdVerificationCode,verificationCodes[0].toString(),UtilsConstant.TTL_REDIS_DAO_MILLISECOND);

            //3.设置浏览器接收时的类型
            response.setHeader("Pragma","No-cache");
            response.setHeader("Cache-Control","no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/jpeg");//设置类型
            //4.返回给前端浏览器验证码图片
            ImageIO.write((BufferedImage) verificationCodes[1], "jpeg", outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("获取验证码图片失败..");
        }finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            return RspResult.FAILED;
        }
        //密码和盐隐藏掉
        userInfo.setPassword(null);
        userInfo.setSalt(null);
        return new RspResult(userInfo);
    }

    @Override
    public boolean updateByMyIds(MultipartFile multipartFile, String usersJson) {
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
        Integer update = userInfoService.updatesByIds(new MultipartFile[]{multipartFile}, userInfos);

        if(update==null||update<=0){
            return false;
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

        return true;
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
                baseMapper.creatCache(key,redisUser,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
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
                baseMapper.creatCache(key,redisUserRoleRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
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
                List<String> functionPermits = functionInfos.stream().map(functionInfo -> functionInfo.getPermit()).collect(Collectors.toList());
                redisUserFunctionRs.setFunctionPermits(functionPermits);
                redisUserFunctionRs.setUserId(userId);
                //2)将数据库的用户信息更新到缓存中
                baseMapper.creatCache(key,redisUserFunctionRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
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
                baseMapper.updateCache(userInfoKey,redisUserInfo,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
            }else if(mysqlUserInfo!=null&&userInfo==null){//a2:mysql有,redis无->新增
                UserInfo redisUserInfo=new UserInfo();
                redisUserInfo.setId(mysqlUserInfo.getId());
                redisUserInfo.setName(mysqlUserInfo.getName());
                redisUserInfo.setCode(mysqlUserInfo.getCode());
                redisUserInfo.setRealName(mysqlUserInfo.getRealName());
                redisUserInfo.setNickname(mysqlUserInfo.getNickname());
                baseMapper.creatCache(userInfoKey,redisUserInfo,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
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
                baseMapper.updateCache(userRolesKey,redisUserRoleRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
            }else if((mysqlUserRoleRs!=null &&mysqlUserRoleRs.size()>0)&&userRoleRs==null){//a2:mysql有,redis无->新增
                com.planet.module.authManage.entity.redis.UserRoleRs redisUserRoleRs=new com.planet.module.authManage.entity.redis.UserRoleRs();
                List<String> rolePermits = mysqlUserRoleRs.stream().map(mysqlUserRole -> mysqlUserRole.getRoleInfo().getPermit()).collect(Collectors.toList());
                redisUserRoleRs.setRolePermits(rolePermits);
                redisUserRoleRs.setUserId(userId);
                baseMapper.creatCache(userRolesKey,redisUserRoleRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
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
                List<String> functionPermits = functionInfos.stream().map(functionInfo -> functionInfo.getPermit()).collect(Collectors.toList());
                redisUserFunctionRs.setFunctionPermits(functionPermits);
                redisUserFunctionRs.setUserId(userId);
                baseMapper.updateCache(userFunctionsKey,redisUserFunctionRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
            }else if((functionInfos!=null &&functionInfos.size()>0)&&userFunctionRs==null){//a2:mysql有,redis无->新增
                UserFunctionRs redisUserFunctionRs=new UserFunctionRs();
                List<String> functionPermits = functionInfos.stream().map(functionInfo -> functionInfo.getPermit()).collect(Collectors.toList());
                redisUserFunctionRs.setFunctionPermits(functionPermits);
                redisUserFunctionRs.setUserId(userId);
                baseMapper.creatCache(userFunctionsKey,redisUserFunctionRs,UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
            }else if((functionInfos==null ||functionInfos.size()==0)&&userFunctionRs!=null){//a3:mysql无,redis有->删除
                baseMapper.removeCache(userFunctionsKey);
            }else{//a4:mysql无,redis无->不操作
                log.info("mysql无,redis无->[用户-权限]缓存不操作");
            }
        });
        return true;
    }


}
