package com.planet.module.authManage.service.springBootQuartzJob;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpMode;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统文件定时清理
 * 有些要删除,有些则要作为历史证据通过ftp放到远程文件库中保留
 */
@Slf4j
@PropertySource({"classpath:config/springBootQuartz.yml", "classpath:config/ftp.yml"})
public class SystemFilesCleanJob extends QuartzJobBean {
    @Autowired
    private Environment env;
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("系统文件定时清理开始执行..");

        //1.获取本地的要删除或进行清理的用户文件夹
        File[] ls = FileUtil.ls(env.getProperty("userInfoFolderPath"));
        if(ls==null||ls.length==0){
            return;//为空不清理
        }
        //2.通过ftp连接文件库
        Charset charset=Charset.forName(env.getProperty("charset"));//设置支持中文编码
        Ftp ftp = new Ftp(env.getProperty("host"), Integer.valueOf(env.getProperty("port")), env.getProperty("user"),
                env.getProperty("password"),charset,env.getProperty("language"),org.apache.commons.net.ftp.FTPClientConfig.SYST_NT);
        ftp.setMode(FtpMode.Passive);//设置被动模式
        //3.对要删除的用户文件夹进行操作
        //1)获取要删除的用户文件夹
        List<File> delFolders = Arrays.stream(ls).sequential().filter(l -> l.getName().endsWith("_del")).collect(Collectors.toList());
        Ftp finalFtp = ftp;
        delFolders.stream().forEach(delFolder->{
            String delFolderName=delFolder.getName();
            File[] delLs = FileUtil.ls(env.getProperty("userInfoFolderPath")+"/" + delFolderName);
            //2)对要删除的文件夹中的文件进行上传ftp指定的文件库,然后再删除
            Arrays.stream(delLs).sequential().forEach(delL->{
                boolean upload = finalFtp.upload(env.getProperty("ftpUserInfoFolderPath")+"/" + delFolderName,delL.getName() , delL);
                if(upload){//如果执行成功,则再将对应的本地文件删除
                    boolean delete = delL.delete();
                }else{
                    throw new RuntimeException("FTP文件上传失败..取消本地文件的删除");
                }
            });
            //3)如果没有发生异常,说明文件的移动和删除都成功了..所以最后要把本地del空文件夹删除掉
            boolean del = FileUtil.del(env.getProperty("userInfoFolderPath")+"/" + delFolderName);
            log.info("移动del文件夹成功>>>"+delFolderName);
        });
        //4.对非删除的用户文件夹进行逐一遍历,然后对含有_rep的头像图片文件进行转移
        //1)获取非删除的用户文件夹
        List<File> folders = Arrays.stream(ls).sequential().filter(l -> !l.getName().endsWith(env.getProperty("delFileSuffix"))).collect(Collectors.toList());
        folders.stream().forEach(folder->{
            String folderName=folder.getName();
            File[] fileLs = FileUtil.ls(env.getProperty("userInfoFolderPath")+"/" + folderName);
            //2)只对含有_rep的文件进行操作:先上传ftp的指定文件库,然后再删除本地的
            Arrays.stream(fileLs).sequential().filter(fileL->{
                String fileLName = fileL.getName();
                return fileLName.substring(0,fileLName.indexOf(".")).endsWith(env.getProperty("replaceFileSuffix"));
            }).forEach(repFileL->{
                boolean upload = finalFtp.upload(env.getProperty("ftpUserInfoFolderPath")+"/" + folderName,repFileL.getName() , repFileL);
                if(upload){//如果执行成功,则再将对应的本地文件删除
                    if(repFileL.delete()){
                        log.info("移动rep文件成功>>>"+repFileL.getName());
                    }else{
                        throw new RuntimeException("本地文件的删除失败..");
                    }
                }else{
                    throw new RuntimeException("FTP文件上传失败..取消本地文件的删除");
                }
            });
        });
        try {
            ftp.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("关闭ftp客户端发生了io异常");
        }
        log.info("系统文件定时清理执行完毕..");
    }
}
