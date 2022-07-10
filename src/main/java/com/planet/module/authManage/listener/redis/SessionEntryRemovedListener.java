package com.planet.module.authManage.listener.redis;

import com.planet.module.authManage.dao.mysql.mapper.AccountLogMapper;
import com.planet.module.authManage.dao.mysql.mapper.UserInfoMapper;
import com.planet.module.authManage.entity.mysql.AccountLog;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.util.jdk8.ObjectSerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * 对存在redis中的session在销毁时进行监听的Listener
 */
@Component
@Slf4j
public class SessionEntryRemovedListener implements EntryRemovedListener<String, String> {
    @Autowired
    private AccountLogMapper accountLogMapper;

    @Override
    public void onRemoved(EntryEvent<String, String> event) {
        log.info("session销毁,调用onRemoved方法进行用户登出记录..");
        String sessionIdKey=event.getKey();
        Long userId=null;
        Long accountLogId=null;
        try {
            Session session = (Session) ObjectSerializeUtil.serializeToObj(event.getValue());
            Object o = session.getAttribute("userId");
            Object logId= session.getAttribute("accountLogId");
            if(o==null){
                log.error("获取失效session中的userId失败:userId为空");
                return;
            }
            userId=(Long)o;
            if(logId!=null){//拿出登录时新增的那条accountLog记录,然后进行配对后修改其updatime时间..从而与creatime配成登录登出的时间段
                accountLogId=(Long)logId;
            }
        } catch (IOException |ClassNotFoundException e) {
            e.printStackTrace();
            log.error("获取失效session中的userId失败:出现异常");
        }
        //session销毁时不用删除session对应的deque,因为在loguot方法中已经调用了删除session对应的deque的方法了
        //采用配对方式,查出该用户最后一次登录还没有配对的记录,然后将其updatime值设置为登出时间——这样配对的好处，是为了统计时间段方便和高效
        if(accountLogId!=null){
            AccountLog accountLog = accountLogMapper.selectById(accountLogId);
            accountLog.setUpdatime(new Date());
            int update=accountLogMapper.updateById(accountLog);
            if(update>0){
                log.info("session销毁,调用onRemoved方法进行用户登出记录>>>成功");
            }else{
                log.error("session销毁,调用onRemoved方法进行用户登出记录>>>失败");
            }
        }else{
            log.error("session销毁,accountLogId为空,调用onRemoved方法进行用户登出记录>>>失败");
        }

    }
}
