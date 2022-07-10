package com.planet.system.startRun;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.planet.common.constant.ComponentConstant;
import com.planet.common.constant.LocalCacheConstantService;
import com.planet.common.constant.ServiceConstant;
import com.planet.common.constant.UtilsConstant;
import com.planet.module.authManage.dao.mysql.mapper.ConfigureSysMapper;
import com.planet.module.authManage.entity.mysql.ConfigureSys;
import com.planet.module.authManage.listener.redis.SessionEntryRemovedListener;
import com.planet.module.authManage.listener.redis.SessionExpiredEntryListener;
import com.planet.module.authManage.service.springBootQuartzJob.SystemFilesCleanJob;
import com.planet.util.springBoot.SpringBootQuartzManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * springboot启动后立刻指向此类的run方法中的代码
 */
@Component
@Order(value=2)
@Slf4j
@PropertySource({"classpath:config/springBootQuartz.yml", "classpath:config/ftp.yml"})
public class BaseCommandLineRunner implements CommandLineRunner {
    @Autowired
    private ConfigureSysMapper configureSysMapper;
    @Autowired
    private SpringBootQuartzManager springBootQuartzManager;
    @Autowired
    private Environment env;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private SessionExpiredEntryListener sessionExpiredEntryListener;
    @Autowired
    private SessionEntryRemovedListener sessionEntryRemovedListener;

    @Override
    public void run(String... args) throws Exception {
        log.info("<<<<<<<<<springboot容器启动后立刻执行>>>>>>>>>");
//        初始化需要动态加载的静态变量
//        loadStaticVariables();
        //启动需要立刻开启的定时任务
//        springBootQuartzRun();
        //创建session监听器
        createSessionManagerMap();
    }
//    这些动态加载的静态变量,往往很多地方都会用到..所以直接统一在最初的时候进行赋值的话,代码层面可以节省很多重复性代码(性能和代码复杂度上进行适度调和)
//    private void loadStaticVariables(){
//        log.info("<<<<<<<<<启动后立刻:初始化需要动态加载的静态变量>>>>>>>>>");
//    }

    private void springBootQuartzRun(){
        log.info("<<<<<<<<<启动后立刻:开启springBootQuartzManager定时任务>>>>>>>>>");

        String storeSystemFilesCleanJob = LocalCacheConstantService.getValue("scheduleTask:systemFilesCleanJob");
        JSONObject jsonObject= JSONUtil.parseObj(storeSystemFilesCleanJob);
        String systemFilesCleanJobName = jsonObject.get("systemFilesCleanJobName", String.class);
        String systemFilesCleanJobGroupName = jsonObject.get("systemFilesCleanJobGroupName", String.class);
        String systemFilesCleanJobCron = jsonObject.get("systemFilesCleanJobCron", String.class);
        springBootQuartzManager.addJob(SystemFilesCleanJob.class,systemFilesCleanJobName,
                systemFilesCleanJobGroupName,systemFilesCleanJobCron);
    }

    private void createSessionManagerMap(){
        log.info("<<<<<<<<<启动后立刻:创建或重新设定sessionManagerMap的事件监听器>>>>>>>>>");
        RMapCache<String, String> map = redissonClient.getMapCache("sessionManagerMap");
        map.put("seatElement","seatElement");//创建一个永久有效的占位元素,以防止sessionManagerMap没有元素而被删除
        map.addListener(sessionExpiredEntryListener);//每次重启都要重新添加事件监听器,否则之前的重启后失效
        map.addListener(sessionEntryRemovedListener);//每次重启都要重新添加事件监听器,否则之前的重启后失效
    }

    @PreDestroy
    public void destroy(){
        log.info("<<<<<<<<<springboot容器关闭前立刻执行>>>>>>>>>");
//        removeSessionManagerMap();
    }

//    private void removeSessionManagerMap(){
//        log.info("<<<<<<<<<销毁sessionManagerMap>>>>>>>>>");
//        RMapCache<String, String> map = redissonClient.getMapCache("sessionManagerMap");
//        if(map.delete()){
//            log.info("销毁sessionManagerMap成功");
//        }else{
//            throw new RuntimeException("销毁sessionManagerMap失败");
//        }
//    }



}
