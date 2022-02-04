package com.planet.system.authByShiro.customSettings;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.constant.UtilsConstant;
import com.planet.module.authManage.dao.mapper.UserInfoMapper;
import com.planet.module.authManage.dao.redis.BaseMapper;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.entity.redis.UserFunctionRs;
import com.planet.system.authByShiro.service.UserShiroService;
import com.planet.system.authByShiro.util.ShiroUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过Shiro进行认证和鉴权功能的系统功能类
 */
//@Component("shiroCustomRealm")
public class ShiroCustomRealm extends AuthorizingRealm {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private UserShiroService userShiroService;


    /**
     * @Description 认证
     * @param token 登录Token对象
     * @return
     */
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        System.out.println("doGetAuthenticationInfo");
        //token令牌信息
        CustomUserToken loginToken = (CustomUserToken) token;
        //查询user对象
        UserInfo user = userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("name", loginToken.getUsername()));
        if(user==null){
            throw new UnknownAccountException("账号不存在！");
        }
        com.planet.module.authManage.entity.redis.UserInfo userInfo =new com.planet.module.authManage.entity.redis.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setName(user.getName());
        userInfo.setRealName(user.getRealName());
        userInfo.setNickname(user.getNickname());

        //这个拿到密码比较器中,用于建立密码失败次数的缓存key的后半部分
        loginToken.setUserId(user.getId());

        //构建认证信息对象:1、令牌对象(相当于当前请求的线程的独享数据空间) 2、密文密码  3、加密因子 4、当前realm的名称
        return new SimpleAuthenticationInfo(userInfo, user.getPassword(), ByteSource.Util.bytes(user.getSalt()), getName());
    }

    /**
     * 鉴权
     * 这里是判断当前用户是否有当前请求的权限的方法
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        System.out.println("doGetAuthorizationInfo");
        //1.对当前用户进行判断并授予相应权限
        //1)获得shiro本地缓存中的当前用户对象
        Object principal = ShiroUtil.getPrincipal();
        //2)获取当前用户对象的userId
        String jsonStr = JSONUtil.toJsonPrettyStr(principal);
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
        Long userId = jsonObject.get("id", Long.class);
        //3)通过userId从redis中获取此用户的权限缓存列表
        String userFunctionsKey=UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
        UserFunctionRs userFunctionRs = (UserFunctionRs)baseMapper.getCache(userFunctionsKey);
        if(userFunctionRs==null||userFunctionRs.getFunctionPermits()==null||userFunctionRs.getFunctionPermits().size()==0){
            //如果当前用户没有任何权限,则直接返回错误信息
            return null;
        }
        //2.授予当前用户的权限
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addStringPermissions(userFunctionRs.getFunctionPermits());

        return info;
    }

    /**
     * 清空缓存的方法
     * 用户登出时需要清空用户在缓存中的个人信息
     * @param principals
     */
    @Override
    protected void doClearCache(PrincipalCollection principals) {
        //1.子类的这个方法用来清空在redis自定义的用户的缓存
//        String sessionId = ShiroUtil.getShiroSessionId();
        Object primaryPrincipal = principals.getPrimaryPrincipal();
        String jsonStr = JSONUtil.toJsonPrettyStr(primaryPrincipal);
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
        Long userId = jsonObject.get("id", Long.class);
        //1)清空userInfo
        String userInfoKey= UtilsConstant.REDIS_USER_ID_FOR_USER_INFO+userId;
        baseMapper.removeCache(userInfoKey);
        //2)清空userSession
        userShiroService.deleteUserSessionByUserId(userId);
        //3)清空userRoles
        String userRolesKey=UtilsConstant.REDIS_USER_ID_FOR_ROLES_PERMITS+userId;
        baseMapper.removeCache(userRolesKey);
        //4)清空userFunctions
        String userFunctionsKey=UtilsConstant.REDIS_USER_ID_FOR_FUNCTIONS_PERMITS+userId;
        baseMapper.removeCache(userFunctionsKey);
        //2.让父类的同名方法去清空shiro本地的缓存,比如session
        super.doClearCache(principals);
    }



}
