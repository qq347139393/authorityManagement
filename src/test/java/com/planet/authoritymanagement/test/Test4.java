package com.planet.authoritymanagement.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpMode;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.planet.common.constant.UtilsConstant;
import com.planet.module.authManage.dao.redis.BaseMapper;
import com.planet.module.authManage.entity.mysql.UserInfo;
import com.planet.module.authManage.entity.mysql.UserLog;
import com.planet.module.authManage.service.UserLogService;
import com.planet.util.FTPFileUtil;
import com.planet.util.jdk8.ObjectSerializeUtil;
import com.planet.util.springBoot.SpringBootQuartzManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
//@EnableScheduling // 开启定时任务功能
public class Test4 {
    @Autowired
    private SpringBootQuartzManager springBootQuartzManager;
    @Autowired
    private UserLogService userLogService;
    @Autowired
    private BaseMapper baseMapper;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test1(){
        System.out.println(1);
        springBootQuartzManager.addJob(OrderTimeoutJob.class,"testJob1",
                "testJobGroup1","*/5 * * * * ?");//每5秒执行一次
        System.out.println("开始执行");
    }

    String userInfoFolderPath="D:/home/authorityManagement-fileFolder/userInfo";
    @Test
    public void test2(){
        //1.获取本地的要删除或进行清理的用户文件夹
        File[] ls = FileUtil.ls(userInfoFolderPath);
        if(ls==null||ls.length==0){
            return;//为空不清理
        }
        //2.通过ftp连接文件库
        Charset charset=Charset.forName("GBK");//设置支持中文编码
        Ftp ftp = new Ftp("192.168.1.5", 21, "ftpUser", "123456",charset,"zh",org.apache.commons.net.ftp.FTPClientConfig.SYST_NT);
        ftp.setMode(FtpMode.Passive);//设置被动模式
        //3.对要删除的用户文件夹进行操作
        //1)获取要删除的用户文件夹
        List<File> delFolders = Arrays.stream(ls).sequential().filter(l -> l.getName().endsWith("_del")).collect(Collectors.toList());
        Ftp finalFtp = ftp;
        delFolders.stream().forEach(delFolder->{
            String delFolderName=delFolder.getName();
            File[] delLs = FileUtil.ls("D:/home/authorityManagement-fileFolder/userInfo/" + delFolderName);
            //2)对要删除的文件夹中的文件进行上传ftp指定的文件库,然后再删除
            Arrays.stream(delLs).sequential().forEach(delL->{
                boolean upload = finalFtp.upload("/authorityManagement-fileFolder/userInfo/" + delFolderName,delL.getName() , delL);
                if(upload){//如果执行成功,则再将对应的本地文件删除
                    boolean delete = delL.delete();
                }else{
                    throw new RuntimeException("FTP文件上传失败..取消本地文件的删除");
                }
            });
            //3)如果没有发生异常,说明文件的移动和删除都成功了..所以最后要把本地del空文件夹删除掉
            boolean del = FileUtil.del("D:/home/authorityManagement-fileFolder/userInfo/" + delFolderName);
        });
        //4.对非删除的用户文件夹进行逐一遍历,然后对含有_rep的头像图片文件进行转移
        //1)获取非删除的用户文件夹
        List<File> folders = Arrays.stream(ls).sequential().filter(l -> !l.getName().endsWith("_del")).collect(Collectors.toList());
        folders.stream().forEach(folder->{
            String folderName=folder.getName();
            File[] fileLs = FileUtil.ls("D:/home/authorityManagement-fileFolder/userInfo/" + folderName);
            //2)只对含有_rep的文件进行操作:先上传ftp的指定文件库,然后再删除本地的
            Arrays.stream(fileLs).sequential().filter(fileL->{
                String fileLName = fileL.getName();
                return fileLName.substring(0,fileLName.indexOf(".")).endsWith("_rep");
            }).forEach(repFileL->{
                boolean upload = finalFtp.upload("/authorityManagement-fileFolder/userInfo/" + folderName,repFileL.getName() , repFileL);
                if(upload){//如果执行成功,则再将对应的本地文件删除
                    boolean delete = repFileL.delete();
                }else{
                    throw new RuntimeException("FTP文件上传失败..取消本地文件的删除");
                }
            });
        });




        try {
            ftp.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("关闭ftp对象失败");
        }

    }

    @Test
    public void test5() throws IOException {
        Charset charset=Charset.forName("GBK");
        Ftp ftp = new Ftp("192.168.1.5", 21, "ftpUser", "123456",charset,"zh",org.apache.commons.net.ftp.FTPClientConfig.SYST_NT);
        ftp.setMode(FtpMode.Passive);
        //上传本地文件
        ftp.upload("/test1", FileUtil.file("D:/home/authorityManagement-fileFolder/userInfo/user_33_del/这个歌.txt"));
//        //下载远程文件
//        List<String> ls = ftp.ls("/");
//        ls.stream().forEach(System.out::println);
//        ftp.download("/test1", "托业口语和写作考试.docx", FileUtil.file("D:\\home\\authorityManagement-fileFolder\\userInfo\\user_33_del\\66.docx"));


        ftp.close();

    }

    @Test
    public void test3(){
        Object accountLogMapper = SpringUtil.getBean("accountLogMapper");
        System.out.println(accountLogMapper);

    }
    @Test
    public void test4() throws NoSuchMethodException {
        Object accountLogServiceImpl = SpringUtil.getBean("accountLogServiceImpl");
        System.out.println(accountLogServiceImpl);
        Method saveBatch = accountLogServiceImpl.getClass().getMethod("saveBatch", Collection.class, int.class);
        System.out.println(saveBatch);

    }
    @Test
    public void test6(){
        List<UserLog> userLogs = (List<UserLog>)userLogService.listByIds(Arrays.asList(3l, 4l));
        for (UserLog userLog : userLogs) {
//            System.out.println(userLog);
            String content = userLog.getContent();
            System.out.println(content);
//            content.

        }

    }
    @Test
    public void test7(){
//        baseMapper.creatCache("测试失效时间","123",10000l);
        baseMapper.creatCache("测试失效2",new Object(), UtilsConstant.TTL_REDIS_DAO_MILLISECOND);
    }

    @Test
    public void test8(){
//        redissonClient.getMapCache()

        RMapCache<String, Integer> map = redissonClient.getMapCache("myMap");

//// 或
//        RLocalCachedMapCache<String, Integer> map = redissonClient.getLocalCachedMapCache("myMap", options);

        int updateListener = map.addListener(new EntryUpdatedListener<String, Integer>() {
            @Override
            public void onUpdated(EntryEvent<String, Integer> event) {
                System.out.println("onUpdated");
                String key = event.getKey();// 字段名
                Integer value = event.getValue();// 值
                Integer oldValue = event.getOldValue();// 旧值
                System.out.println(key+":"+value+"====oldValue:"+oldValue);
// ...
            }
        });

        int createListener = map.addListener(new EntryCreatedListener<String, Integer>() {
            @Override
            public void onCreated(EntryEvent<String, Integer> event) {
                System.out.println("onCreated");
                String key = event.getKey();// 字段名
                Integer value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
            }
        });

        int expireListener = map.addListener(new EntryExpiredListener<String, Integer>() {
            @Override
            public void onExpired(EntryEvent<String, Integer> event) {
                System.out.println("onExpired");
                String key = event.getKey();// 字段名
                Integer value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
//                map.remove("测试失效事件触发");
//                System.out.println("删除:测试失效事件触发");
            }
        });

        int removeListener = map.addListener(new EntryRemovedListener<String, Integer>() {
            @Override
            public void onRemoved(EntryEvent<String, Integer> event) {
                System.out.println("onRemoved");
                String key = event.getKey();// 字段名
                Integer value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
            }
        });

//        map.removeListener(updateListener);
//        map.removeListener(createListener);
//        map.removeListener(expireListener);
//        map.removeListener(removeListener);


        map.put("测试失效事件触发",1000,2, TimeUnit.SECONDS);
//        map.
    }

    @Test
    public void test9(){
//        redissonClient.getMapCache()
        String name="date";
        Date d=new Date();
        long m=2*1000l;

        RMapCache<String, Object> map = redissonClient.getMapCache("myMap");

        List<RBucket<String>> buckets=new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RBucket<String> bucket = redissonClient.getBucket(name+i);
            try {
                long a=m/1000+i;
                System.out.println(a);
                bucket.trySet(ObjectSerializeUtil.objToSerialize(d), a, TimeUnit.SECONDS);
            } catch (IOException e) {
                e.printStackTrace();
            }
            buckets.add(bucket);
        }
//        RBucket<String> bucket10 = redissonClient.getBucket(name+10);
//        try {
//            bucket10.trySet(ObjectSerializeUtil.objToSerialize(d), 1000, TimeUnit.SECONDS);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        buckets.add(bucket10);


        int updateListener = map.addListener(new EntryUpdatedListener<String, Object>() {
            @Override
            public void onUpdated(EntryEvent<String, Object> event) {
                System.out.println("onUpdated");
                String key = event.getKey();// 字段名
                Object value = event.getValue();// 值
                Object oldValue = event.getOldValue();// 旧值
                System.out.println(key+":"+value+"====oldValue:"+oldValue);
// ...
            }
        });

        int createListener = map.addListener(new EntryCreatedListener<String, Object>() {
            @Override
            public void onCreated(EntryEvent<String, Object> event) {
                System.out.println("onCreated");
                String key = event.getKey();// 字段名
                Object value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
            }
        });

        int expireListener = map.addListener(new EntryExpiredListener<String, Object>() {
            @Override
            public void onExpired(EntryEvent<String, Object> event) {
                System.out.println("onExpired==========================");
                String key = event.getKey();// 字段名
                Object value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
//                map.remove("测试失效事件触发");
//                System.out.println("删除:测试失效事件触发");
            }
        });

        int removeListener = map.addListener(new EntryRemovedListener<String, Object>() {
            @Override
            public void onRemoved(EntryEvent<String, Object> event) {
                System.out.println("onRemoved");
                String key = event.getKey();// 字段名
                Object value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
            }
        });

//        map.removeListener(updateListener);
//        map.removeListener(createListener);
//        map.removeListener(expireListener);
//        map.removeListener(removeListener);
//        for (RBucket<String> bucket : buckets) {
//            map.put(bucket.getName(),bucket);
//            bucket.
//        }
//        map.put(bucket10.getName(),bucket10);
//        map.put("测试失效事件触发",1000,2, TimeUnit.SECONDS);
//        map.
    }

    @Test
    public void test10(){
//        redissonClient.getMapCache()

        RMapCache<String, Integer> map = redissonClient.getMapCache("myMap");

//// 或
//        RLocalCachedMapCache<String, Integer> map = redissonClient.getLocalCachedMapCache("myMap", options);

        int updateListener = map.addListener(new EntryUpdatedListener<String, Integer>() {
            @Override
            public void onUpdated(EntryEvent<String, Integer> event) {
                System.out.println("onUpdated");
                String key = event.getKey();// 字段名
                Integer value = event.getValue();// 值
                Integer oldValue = event.getOldValue();// 旧值
                System.out.println(key+":"+value+"====oldValue:"+oldValue);
// ...
            }
        });

        int createListener = map.addListener(new EntryCreatedListener<String, Integer>() {
            @Override
            public void onCreated(EntryEvent<String, Integer> event) {
                System.out.println("onCreated");
                String key = event.getKey();// 字段名
                Integer value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
            }
        });

        int expireListener = map.addListener(new EntryExpiredListener<String, Integer>() {
            @Override
            public void onExpired(EntryEvent<String, Integer> event) {
                System.out.println("onExpired");
                String key = event.getKey();// 字段名
                Integer value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
//                map.remove("测试失效事件触发");
//                System.out.println("删除:测试失效事件触发");
            }
        });

        int removeListener = map.addListener(new EntryRemovedListener<String, Integer>() {
            @Override
            public void onRemoved(EntryEvent<String, Integer> event) {
                System.out.println("onRemoved");
                String key = event.getKey();// 字段名
                Integer value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
            }
        });

//        map.removeListener(updateListener);
//        map.removeListener(createListener);
//        map.removeListener(expireListener);
//        map.removeListener(removeListener);


        map.put("测试失效事件触发",1000,2, TimeUnit.SECONDS);
//        map.put("测试失效事件触发2",1000,200, TimeUnit.SECONDS);
//        map.
//        map.put("测试失效事件触发",100,3, TimeUnit.SECONDS);
//        map.put("测试失效事件触发2",1000,200, TimeUnit.SECONDS);
    }
    @Test
    public void test11(){
        RMapCache<String, String> map = redissonClient.getMapCache("myMap");
        System.out.println(123);
        String a = map.get("a");
        System.out.println(a);
        map.put("a","a");
        System.out.println(123);


        int updateListener = map.addListener(new EntryUpdatedListener<String, String>() {
            @Override
            public void onUpdated(EntryEvent<String, String> event) {
                System.out.println("onUpdated");
                String key = event.getKey();// 字段名
                String value = event.getValue();// 值
                String oldValue = event.getOldValue();// 旧值
                System.out.println(key+":"+value+"====oldValue:"+oldValue);
// ...
            }
        });

        int createListener = map.addListener(new EntryCreatedListener<String, String>() {
            @Override
            public void onCreated(EntryEvent<String, String> event) {
                System.out.println("onCreated");
                String key = event.getKey();// 字段名
                String value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
            }
        });

        int expireListener = map.addListener(new EntryExpiredListener<String, String>() {
            @Override
            public void onExpired(EntryEvent<String, String> event) {
                System.out.println("onExpired");
                String key = event.getKey();// 字段名
                String value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
//                map.remove("测试失效事件触发");
//                System.out.println("删除:测试失效事件触发");
            }
        });

        int removeListener = map.addListener(new EntryRemovedListener<String, String>() {
            @Override
            public void onRemoved(EntryEvent<String, String> event) {
                System.out.println("onRemoved");
                String key = event.getKey();// 字段名
                String value = event.getValue();// 值
                System.out.println(key+":"+value);
                // ...
            }
        });


        map.put("b","b",1,TimeUnit.SECONDS);
        System.out.println(1232132);
    }

}
