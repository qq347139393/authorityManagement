package com.planet.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/**
 * Created by Administrator on 2018/8/29 0029.
 * FTP 工具类
 */
@Slf4j
public class FTPFileUtil {

    /** 本地支持中文的字符编码 */
    private static String LOCAL_CHARSET = "GBK";

    /** FTP协议里面，规定文件名编码为iso-8859-1 */
    private static final String SERVER_CHARSET = "ISO-8859-1";
    /**
     * 连接 FTP 服务器
     * @param address     FTP 服务器 IP 地址
     * @param port     FTP 服务器端口号
     * @param username 登录用户名
     * @param password 登录密码
     * @return
     */
    public static FTPClient connectFtpServer(String address, int port, String username, String password) {
        FTPClient ftpClient = new FTPClient();
        try {
            /**连接 FTP 服务器
             * 如果连接失败，则此时抛出异常，如ftp服务器服务关闭时，抛出异常：
             * java.net.ConnectException: Connection refused: connect*/
            ftpClient.connect(address, port);
            /**登录 FTP 服务器
             * 1）如果传入的账号为空，则使用匿名登录，此时账号使用 "Anonymous"，密码为空即可*/
            if (username==null||"".equals(username)) {
                ftpClient.login("Anonymous", "");
            } else {
                ftpClient.login(username, password);
            }

            /**设置文件传输的编码*/
            //这样设置,是为了兼容win和linux的ftp服务器
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                    "OPTS UTF8", "ON"))) {// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
                LOCAL_CHARSET = "UTF-8";
            }
            ftpClient.setControlEncoding(LOCAL_CHARSET);

            /** 设置传输的文件类型
             * BINARY_FILE_TYPE：二进制文件类型
             * ASCII_FILE_TYPE：ASCII传输方式，这是默认的方式
             * ....
             */
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            /**
             * 确认应答状态码是否正确完成响应
             * 凡是 2开头的 isPositiveCompletion 都会返回 true，因为它底层判断是：
             * return (reply >= 200 && reply < 300);
             */
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                /**
                 * 如果 FTP 服务器响应错误 中断传输、断开连接
                 * abort：中断文件正在进行的文件传输，成功时返回 true,否则返回 false
                 * disconnect：断开与服务器的连接，并恢复默认参数值
                 */
                ftpClient.abort();
                ftpClient.disconnect();
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("FTP登陆>>>失败>>>出现io异常");
        }
        log.info("FTP登录>>>成功>>>"+address);
        return ftpClient;
    }

    /**
     * 使用完毕，应该及时关闭连接
     * 终止 ftp 传输
     * 断开 ftp 连接
     * @param ftpClient
     * @return
     */
    public static FTPClient closeFTPConnect(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.abort();
                ftpClient.disconnect();
                log.info("FTP关闭连接>>>成功>>>...");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("FTP关闭连接>>>失败>>>出现io异常");
        }
        return ftpClient;
    }

    /**
     * 文件路径分层全部统一用"/"来表示,这样可以兼容win和linux
     * @param filePath
     * @return
     */
    public static String unifyFilePath(String filePath){
        String[] split = filePath.split("\\\\");
        if(split.length>=0){//说明用的是"\\",我们要转成"/"
            filePath = filePath.replaceAll("\\\\", "/");
        }
        return filePath;
    }

    /**
     * Ftp与本地字符集转换方法
     * @param str
     * @param i 1表示本地转到FTP编码;0表示从FTP转回本地编码
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String changeCharsetDependFtpOrLocal(String str,int i) throws UnsupportedEncodingException {
        if(i==0){//表示将给定字符串从Ftp指定的字符集编码转回本地的字符集编码
            return new String(str.getBytes(SERVER_CHARSET),LOCAL_CHARSET);
        }else if(i==1){
            //表示将给定字符串从本地字符集编码转成Ftp的字符集编码
            return new String(str.getBytes(LOCAL_CHARSET),SERVER_CHARSET);
        }
        log.error(">>>Ftp与本地字符集转换>>>失败>>>无效的转换");
        return str;
    }


    /**
     * 下载 FTP 服务器上指定的单个文件，而且本地存放的文件相对部分路径 会与 FTP 服务器结构保持一致
     * 会覆盖同名的本地文件
     *
     * @param ftpClient              ：连接成功有效的 FTP客户端连接
     * @param localDirectory ：本地存储文件的绝对路径，如 E:\gxg\ftpDownload
     * @param ftpFilePath     ：ftpFile 文件在服务器所在的绝对路径，此方法强制路径使用右斜杠"\"，如 "\video\2018.mp4"
     * @return
     */
    public static void downloadSingleFile(FTPClient ftpClient, String localDirectory, String ftpFilePath) {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            log.error(">>>FTP下载单个文件>>>失败>>>FTP服务器连接已经关闭或者连接无效");
            return;
        }
        if (localDirectory==null||"".equals(localDirectory)||ftpFilePath==null||"".equals(ftpFilePath)) {
            log.error(">>>FTP下载单个文件>>>失败>>>下载时遇到本地存储路径或者ftp服务器文件路径为空，放弃");
            return;
        }
        try {
            //对路径进行统一分层设置
            localDirectory=unifyFilePath(localDirectory);
            ftpFilePath=unifyFilePath(ftpFilePath);
            //对路径进行转码,以防止中文路径出现乱码而无法匹配的情况
            ftpFilePath = changeCharsetDependFtpOrLocal(ftpFilePath,1);
            /**没有对应路径时，FTPFile[] 大小为0，不会为null*/
            FTPFile[] ftpFiles = ftpClient.listFiles(ftpFilePath);
            FTPFile ftpFile = null;
            if (ftpFiles.length >= 1) {
                ftpFile = ftpFiles[0];
            }
            if (ftpFile != null && ftpFile.isFile()) {
                /** ftpFile.getName():获取的是文件名称，如 123.mp4
                 * 必须保证文件存放的父目录必须存在，否则 retrieveFile 保存文件时报错
                 */
                ftpFilePath = changeCharsetDependFtpOrLocal(ftpFilePath,0);
                localDirectory=localDirectory+ftpFilePath.substring(ftpFilePath.lastIndexOf("/"));
                File localFile = new File(localDirectory);
                //如果文件的父级目录不存在,则创建出全部的父级文件夹
                if (!localFile.getParentFile().exists()) {
                    localFile.getParentFile().mkdirs();
                    log.info(">>>FTP下载单个文件>>>特殊情况>>>创建当前文件的父级文件夹成功");
                }
                OutputStream outputStream = new FileOutputStream(localFile);
                int i = ftpFilePath.lastIndexOf("/");
                if(i>0){//替换
                    ftpFilePath = ftpFilePath.substring(0, ftpFilePath.lastIndexOf("/"));
                }else if(i==0){//说明是根目录
                    ftpFilePath = "/";
                }
                /**文件下载前，FTPClient工作目录必须切换到文件所在的目录，否则下载失败
                 * "/" 表示用户根目录*/
                ftpClient.changeWorkingDirectory(ftpFilePath);
                /**下载指定的 FTP 文件 到本地
                 * 1)注意只能是文件，不能直接下载整个目录
                 * 2)如果文件本地已经存在，默认会重新下载
                 * 3)下载文件之前，ftpClient 工作目录必须是下载文件所在的目录
                 * 4)下载成功返回 true，失败返回 false
                 */
                String ftpFileName=ftpFile.getName();
                ftpFileName=changeCharsetDependFtpOrLocal(ftpFileName,1);
                boolean b = ftpClient.retrieveFile(ftpFileName, outputStream);
                outputStream.flush();
                outputStream.close();
                log.info(">>>FTP下载单个文件>>>成功>>>"+ localDirectory);
            }else{
                log.error(">>>FTP下载单个文件>>>失败>>>给定的路径不是单文件");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(">>>FTP下载单个文件>>>失败>>>出现io异常");
        }
    }

    /**
     * 下载Ftp上指定的文件夹到本地指定的文件夹下
     * 会覆盖同名的本地文件
     * @param ftpClient
     * @param localFolderPath
     * @param ftpFolderPath
     */
    public static void downloadFolderFiles(FTPClient ftpClient,String localFolderPath, String ftpFolderPath) {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            log.error(">>>下载Ftp指定的文件夹>>>失败>>>ftp 连接已经关闭或者连接无效......");
            return;
        }
        //统一转成"/"形式表示层级
        localFolderPath=unifyFilePath(localFolderPath);
        ftpFolderPath=unifyFilePath(ftpFolderPath);
        try {
            ftpFolderPath=changeCharsetDependFtpOrLocal(ftpFolderPath,1);//转ftp码
            /**转移到FTP服务器根目录下的指定子目录
             * 1)"/"：表示用户的根目录，为空时表示不变更
             * 2)参数必须是目录，当是文件时改变路径无效
             * */
            ftpClient.changeWorkingDirectory(ftpFolderPath);
            /** listFiles：获取FtpClient连接的当前下的一级文件列表(包括子目录)
             * 1）FTPFile[] ftpFiles = ftpClient.listFiles("/docs/info");
             *      获取服务器指定目录下的子文件列表(包括子目录)，以 FTP 登录用户的根目录为基准，与 FTPClient 当前连接目录无关
             * 2）FTPFile[] ftpFiles = ftpClient.listFiles("/docs/info/springmvc.txt");
             *      获取服务器指定文件，此时如果文件存在时，则 FTPFile[] 大小为 1，就是此文件
             * */
            FTPFile[] ftpFiles = ftpClient.listFiles();
            ftpFolderPath=changeCharsetDependFtpOrLocal(ftpFolderPath,0);//转回本地码
            if (ftpFiles != null && ftpFiles.length > 0) {
                for (FTPFile ftpFile : ftpFiles) {
                    if (ftpFile.isFile()) {
                        String substring = ftpFolderPath.substring(ftpFolderPath.lastIndexOf("/"));
                        String partLocalFolderPath=localFolderPath+substring;
                        downloadSingleFile(ftpClient,partLocalFolderPath,ftpFolderPath + "/" + ftpFile.getName());
                    } else {
                        String substring = ftpFolderPath.substring(ftpFolderPath.lastIndexOf("/"));
                        String partLocalFolderPath=localFolderPath+substring;
                        downloadFolderFiles(ftpClient,partLocalFolderPath,ftpFolderPath + "/" + ftpFile.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(">>>下载Ftp指定的文件夹>>>失败>>>出现io异常");
        }
    }

    /**
     * 上传单个文件的方法
     * 通过文件流来上传
     * @param ftpClient
     * @param fileInputStream
     * @param ftpFilePath
     */
    public static void uploadFile(FTPClient ftpClient,FileInputStream fileInputStream,String ftpFilePath){
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            log.error(">>>上传单个文件>>>失败>>>FTP服务器连接已经关闭或者连接无效*****放弃文件上传****");
            return;
        }
        if (fileInputStream == null) {
            log.error(">>>上传单个文件>>>失败>>>待上传文件为空*****放弃文件上传****");
            return;
        }
        try {
            //统一转成"/"形式表示层级
            ftpFilePath=unifyFilePath(ftpFilePath);
            ftpFilePath=changeCharsetDependFtpOrLocal(ftpFilePath,1);

            String ftpPath=ftpFilePath.substring(0,ftpFilePath.lastIndexOf("/"));
            String fileName=ftpFilePath.substring(ftpFilePath.lastIndexOf("/")+1);

            /**变更 FTPClient 工作目录到新目录
             * 1)不以"/"开头表示相对路径，新目录以当前工作目录为基准，即当前工作目录下不存在此新目录时，变更失败
             * 2)参数必须是目录，当是文件时改变路径无效*/
            ftpClient.changeWorkingDirectory(ftpPath);

            boolean b  = ftpClient.storeFile(fileName, fileInputStream);
            if(b){
                log.info(">>>上传单个文件>>>成功>>>" + changeCharsetDependFtpOrLocal(ftpFilePath,0));
            }else{
                log.error(">>>上传单个文件>>>失败>>>" + changeCharsetDependFtpOrLocal(ftpFilePath,0));
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(">>>上传单个文件>>>失败>>>发生了io异常");
        }finally {
            try {
                if(fileInputStream!=null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 上传单个文件的方法
     * @param ftpClient
     * @param uploadFile
     * @param ftpPath
     */
    public static void uploadFile(FTPClient ftpClient,File uploadFile,String ftpPath) {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            log.error(">>>上传单个文件>>>失败>>>FTP服务器连接已经关闭或者连接无效*****放弃文件上传****");
            return;
        }
        if (uploadFile == null || !uploadFile.exists()) {
            log.error(">>>上传单个文件>>>失败>>>待上传文件为空或者文件不存在*****放弃文件上传****");
            return;
        }
        /**如果是文件，则直接上传*/
        FileInputStream input = null;
        try {
            input=new FileInputStream(uploadFile);
            uploadFile(ftpClient,input,ftpPath+"/"+uploadFile.getName());
//            //统一转成"/"形式表示层级
//            ftpPath=unifyFilePath(ftpPath);
//            ftpPath=changeCharsetDependFtpOrLocal(ftpPath,1);
//            /**变更 FTPClient 工作目录到新目录
//             * 1)不以"/"开头表示相对路径，新目录以当前工作目录为基准，即当前工作目录下不存在此新目录时，变更失败
//             * 2)参数必须是目录，当是文件时改变路径无效*/
//            ftpClient.changeWorkingDirectory(ftpPath);
//            input=new FileInputStream(uploadFile);
//            String fileName=changeCharsetDependFtpOrLocal(uploadFile.getName(),1);
//            boolean b = ftpClient.storeFile(fileName, input);
//            if(b){
//                log.info(">>>上传单个文件>>>成功>>>" + uploadFile.getPath());
//            }else{
//                log.error(">>>上传单个文件>>>失败>>>" + uploadFile.getPath());
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(">>>上传单个文件>>>失败>>>文件找不到");
        }catch (IOException e){
            e.printStackTrace();
            log.error(">>>上传单个文件>>>失败>>>出现io异常");
        }finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 上传本地文件或目录至FTP服务器
     * 保持 FTP 服务器与本地 文件目录结构一致
     * @param ftpClient  连接成功有效的 FTPClinet
     * @param uploadFile 待上传的文件 或 文件夹(此时会遍历逐个上传)
     * @throws Exception
     */
    public static void uploadFolder(FTPClient ftpClient, File uploadFile,String ftpPath) {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            log.error("上传本地文件或目录至FTP服务器>>>失败>>>FTP服务器连接已经关闭或者连接无效*****放弃文件上传****");
            return;
        }
        if (uploadFile == null || !uploadFile.exists()) {
            log.error("上传本地文件或目录至FTP服务器>>>失败>>>待上传文件为空或者文件不存在*****放弃文件上传****");
            return;
        }
        try {
            //统一转成"/"形式表示层级
            ftpPath=unifyFilePath(ftpPath);
            String newFtpFilePath=ftpPath+"/"+uploadFile.getName();
            newFtpFilePath=changeCharsetDependFtpOrLocal(newFtpFilePath,1);
//            ftpPath=changeCharsetDependFtpOrLocal(ftpPath,1);
            if (uploadFile.isDirectory()) {
                /**如果被上传的是目录时
                 * makeDirectory：在 FTP 上创建目录(方法执行完，服务器就会创建好目录，如果目录本身已经存在，则不会再创建)
                 * 1）可以是相对路径，即不以"/"开头，相对的是 FTPClient 当前的工作路径，如 "video"、"视频" 等，会在当前工作目录进行新建目录
                 * 2）可以是绝对路径，即以"/"开头，与 FTPCLient 当前工作目录无关，如 "/images"、"/images/2018"
                 * 3）注意多级目录时，必须确保父目录存在，否则创建失败，
                 *      如 "video/201808"、"/images/2018" ，如果 父目录 video与images不存在，则创建失败
                 * */
                boolean b = ftpClient.makeDirectory(newFtpFilePath);
                if(b){
                    log.info("上传本地文件或目录至FTP服务器>>>成功>>>"+changeCharsetDependFtpOrLocal(newFtpFilePath,0));
                }else{
                    log.error("上传本地文件或目录至FTP服务器>>>失败>>>"+changeCharsetDependFtpOrLocal(newFtpFilePath,0));
                }
//                /**变更 FTPClient 工作目录到新目录
//                 * 1)不以"/"开头表示相对路径，新目录以当前工作目录为基准，即当前工作目录下不存在此新目录时，变更失败
//                 * 2)参数必须是目录，当是文件时改变路径无效*/
//                ftpClient.changeWorkingDirectory(newFtpFilePath);
                File[] listFiles = uploadFile.listFiles();
                for (int i = 0; i < listFiles.length; i++) {
                    File loopFile = listFiles[i];
                    if (loopFile.isDirectory()) {
                        /**如果有子目录，则迭代调用方法进行上传*/
                        String innerFtpPath=changeCharsetDependFtpOrLocal(newFtpFilePath,0);
                        uploadFolder(ftpClient, loopFile,innerFtpPath);
//                        /**changeToParentDirectory：将 FTPClient 工作目录移到上一层
//                         * 这一步细节很关键，子目录上传完成后，必须将工作目录返回上一层，否则容易导致文件上传后，目录不一致
//                         * */
//                        ftpClient.changeToParentDirectory();
                    } else {
                        /**如果是文件，则直接上传*/
                        uploadFile(ftpClient,loopFile,changeCharsetDependFtpOrLocal(newFtpFilePath,0));
                    }
                }
            } else {
                /**如果是文件，则直接上传*/
                uploadFile(ftpClient,uploadFile,changeCharsetDependFtpOrLocal(newFtpFilePath,0));
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("上传本地文件或目录至FTP服务器>>>失败>>>出现了io异常");
        }
    }

    /**
     * 删除Ftp上指定路径的一个文件
     * 不能是文件夹
     * @param ftpClient
     * @param deleteFtpFilePath
     */
    public static void deleteFtpFile(FTPClient ftpClient, String deleteFtpFilePath){
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            log.error(">>>删除Ftp上指定路径的一个文件>>>失败>>>FTP服务器连接已经关闭或者连接无效*****放弃文件上传****");
            return;
        }
        /**deleteFile：删除FTP服务器上的文件
         * 1）只用于删除文件而不是目录，删除成功时，返回 true
         * 2）删除目录时无效,方法返回 false
         * 3）待删除文件不存在时，删除失败，返回 false
         * */
        boolean deleteFlag = false;
        try {
            deleteFtpFilePath=changeCharsetDependFtpOrLocal(deleteFtpFilePath,1);
            boolean changeFlag = ftpClient.changeWorkingDirectory(deleteFtpFilePath);
            deleteFlag = ftpClient.deleteFile(deleteFtpFilePath);
            if (deleteFlag) {
                log.info(">>>删除Ftp上指定路径的一个文件>>>成功>>>"+changeCharsetDependFtpOrLocal(deleteFtpFilePath,0));
            } else {
                log.error(">>>删除Ftp上指定路径的一个文件>>>失败>>>"+ changeCharsetDependFtpOrLocal(deleteFtpFilePath,0));
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(">>>删除Ftp上指定路径的一个文件>>>失败>>>发生了io异常");
        }

    }

    /**
     * 删除服务器的文件夹或文件
     *
     * @param ftpClient   连接成功且有效的 FTP客户端
     * @param deleteFiles 待删除的文件或者目录，为目录时，会逐个删除，
     *                    路径必须是绝对路径，如 "/1.png"、"/video/3.mp4"、"/images/2018"
     *                    "/" 表示用户根目录,则删除所有内容
     */
    public static void deleteFtpFolderOrFile(FTPClient ftpClient, String deleteFiles) {
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            log.error(">>>删除服务器的文件夹或文件>>>失败>>>FTP服务器连接已经关闭或者连接无效*****放弃文件上传****");
            return;
        }
        try {
            /** 尝试改变当前工作目录到 deleteFiles
             * 1）changeWorkingDirectory：变更FTPClient当前工作目录，变更成功返回true，否则失败返回false
             * 2）如果变更工作目录成功，则表示 deleteFiles 为服务器已经存在的目录
             * 3）否则变更失败，则认为 deleteFiles 是文件，是文件时则直接删除
             */
            deleteFiles=changeCharsetDependFtpOrLocal(deleteFiles,1);
            boolean changeFlag = ftpClient.changeWorkingDirectory(deleteFiles);
            if (changeFlag) {
                /**当被删除的是目录时*/
                FTPFile[] ftpFiles = ftpClient.listFiles();
                for (FTPFile ftpFile : ftpFiles) {
                    if(ftpFile.isDirectory()){
                        /**printWorkingDirectory：获取 FTPClient 客户端当前工作目录
                         * 然后开始迭代删除子目录
                         */
                        String workingDirectory = ftpClient.printWorkingDirectory();
                        deleteFtpFolderOrFile(ftpClient, changeCharsetDependFtpOrLocal(workingDirectory,0) + "/" + ftpFile.getName());
                    }else if(ftpFile.isFile()){
                        //单个文件删除
                        deleteFtpFile(ftpClient,changeCharsetDependFtpOrLocal(deleteFiles,0)+"/"+ftpFile.getName());
                    }
                }
                /**printWorkingDirectory：获取 FTPClient 客户端当前工作目录
                 * removeDirectory：删除FTP服务端的空目录，注意如果目录下存在子文件或者子目录，则删除失败
                 * 运行到这里表示目录下的内容已经删除完毕，此时再删除当前的为空的目录，同时将工作目录移动到上移层级
                 * */
                String workingDirectory = ftpClient.printWorkingDirectory();
                ftpClient.removeDirectory(workingDirectory);
                ftpClient.changeToParentDirectory();
                log.info(">>>删除服务器的文件夹或文件>>>成功>>>删除一个空文件夹>>>"+workingDirectory);
            } else {
                //单个文件删除
                deleteFtpFile(ftpClient,changeCharsetDependFtpOrLocal(deleteFiles,0));
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(">>>删除服务器的文件夹或文件>>>失败>>>发生了io异常");
        }
    }


    /**
     * 将本地指定的文件替换到ftp上的指定文件
     * 1)文件->文件
     * 2)文件->文件夹
     * 3)文件夹->文件
     * 4)文件夹->文件夹
     * 先[删除]再[上传]
     * @param ftpClient
     * @param localFilePath
     * @param ftpFilePath
     */
    public static void replaceFileOrFolder(FTPClient ftpClient,String localFilePath,String ftpFilePath){
        /**如果 FTP 连接已经关闭，或者连接无效，则直接返回*/
        if (!ftpClient.isConnected() || !ftpClient.isAvailable()) {
            log.error(">>>将本地指定的文件替换到ftp上的指定文件>>>失败>>>FTP服务器连接已经关闭或者连接无效*****放弃文件上传****");
            return;
        }
        //统一用"/"来表示层级关系
        ftpFilePath=unifyFilePath(ftpFilePath);
        localFilePath=unifyFilePath(localFilePath);

        File localFile=new File(localFilePath);
        if(localFile.isFile()){
            //先[删除]ftp的指定文件或文件夹
            deleteFtpFolderOrFile(ftpClient,ftpFilePath);
            //再[上传]本地文件到ftp的指定位置
            ftpFilePath=ftpFilePath.substring(0,ftpFilePath.lastIndexOf("/"));
            uploadFile(ftpClient,localFile,ftpFilePath);
        }else if(localFile.isDirectory()){
            //先[删除]ftp的指定文件或文件夹
            deleteFtpFolderOrFile(ftpClient,ftpFilePath);
            //再[上传]本地文件到ftp的指定位置
            ftpFilePath=ftpFilePath.substring(0,ftpFilePath.lastIndexOf("/"));
            uploadFolder(ftpClient,localFile,ftpFilePath);
        }

    }




    public static void main(String[] args) throws Exception {
        System.out.println("-----------------------应用启动------------------------");
        //测试ftp连接
        FTPClient ftpClient = FTPFileUtil.connectFtpServer("192.168.1.5", 21, "ftpUser", "123456"/*, "gbk"*/);
//        System.out.println("FTP 连接是否成功：" + ftpClient.isConnected());
//        System.out.println("FTP 连接是否有效：" + ftpClient.isAvailable());
        //测试单个文件下载
//        downloadSingleFile(ftpClient, "D:/test", "/test1/测试空文件夹复制");
//        downloadSingleFile(ftpClient, "D:/test/新建文件夹", "/test1/123213.docx");

//        downloadFolderFiles(ftpClient, "D:/test/3", "/test1/test2");
        File file=new File("D:/test/测试1");
        uploadFolder(ftpClient,file,"/测试");

//        deleteServerFiles(ftpClient,"/测试/新建文件夹");
//        replaceFileOrFolder(ftpClient,"D:\\test\\新建文件夹\\test1\\测试替换文件夹","\\测试\\测试替换123.txt");
//        //测试ftp关闭
//        closeFTPConnect(ftpClient);
        System.out.println("-----------------------应用关闭------------------------");
    }
}
