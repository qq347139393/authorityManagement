package com.planet.module.authManage.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.ImageData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.util.FileUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.planet.common.constant.LocalCacheConstantService;
import com.planet.common.constant.ServiceConstant;
import com.planet.common.util.RspResult;
import com.planet.module.authManage.dao.mysql.mapper.AccountLogMapper;
import com.planet.module.authManage.entity.excel.UsersOnlineDurationFill;
import com.planet.module.authManage.entity.mysql.AccountLog;
import com.planet.module.authManage.entity.redis.UserInfo;
import com.planet.module.authManage.service.StatisticsModuleService;
import com.planet.util.JFreeChartUtil.BarChart2DOr3DUtil;
import com.planet.util.JFreeChartUtil.ChartToPictureFileUtil;
import com.planet.util.JFreeChartUtil.LineChartUtil;
import com.planet.util.JFreeChartUtil.PieChart2DOr3DUtil;
import com.planet.util.shiro.ShiroUtil;
import com.planet.util.springBoot.WebUtil;
import com.planetProvide.easyExcelPlus.utils.ExcelAppendPictureUtil;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

@Service
@Slf4j
public class StatisticsModuleServiceImpl implements StatisticsModuleService {
    @Value("${web.upload-path}") // D:/home/authorityManagement-fileFolder
    private String webUploadPath;
    @Value(("${web.modular.statistics}")) // /statisticsTemporary
    private String statisticsPath;
    @Autowired
    private AccountLogMapper accountLogMapper;


    @Override
    public RspResult usersOnlineDurationForData(String usersJson) {
        //1.通过内部方法获取、处理并构建需要的统计数据
        Map<String, Object> usersOnlineDurationMap = usersOnlineDuration(usersJson);
        if(usersOnlineDurationMap!=null){
            return new RspResult(usersOnlineDurationMap);
        }
        return RspResult.FAILED;
    }

    @Override
    public void usersOnlineDurationForFile(String usersJson) {
        //1.通过内部方法获取、处理并构建需要的统计数据
        Map<String, Object> usersOnlineDurationMap = usersOnlineDuration(usersJson);

        //2.使用jfreechart工具来生成统计图表文件流
        Map<String, Map<String, Object>> statisticsMap=(Map<String, Map<String, Object>>)usersOnlineDurationMap.get("statisticsMap");
        //1)构建jfreechart的对象
        JFreeChart groupLineChart = LineChartUtil.createGroupLineChart(statisticsMap, "用户在线时长统计折线图", "时间轴", "在线小时数");
        //2)将映射[折线统计图]的JFreeChart对象转成指定格式的图片文件,文件先放入指定的位置保存,后面会用定时任务定时清理
        String filePath= null;
        try {
            filePath = ChartToPictureFileUtil.chartToPictureFile(groupLineChart, null, null,
                    webUploadPath+statisticsPath+"/"+ UUID.randomUUID()+".jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(filePath);

        //3.进行excel填充
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(
                "templates/excel/statistical/指定用户在线时长统计模板.xlsx");
        // 写入workbook对象
        HttpServletResponse response = WebUtil.getResponse();
        ExcelWriter workBook = null;
        try {
            workBook = EasyExcel.write(response.getOutputStream()).withTemplate(templateInputStream).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WriteSheet sheet = EasyExcel.writerSheet().build();
        // ****** 准备数据 *******
        HashMap<String, Object> dateMap1 = new HashMap<String, Object>();
        dateMap1.put("start", (String)usersOnlineDurationMap.get("start"));
        dateMap1.put("end",(String)usersOnlineDurationMap.get("end"));
        dateMap1.put("section",(Integer)usersOnlineDurationMap.get("section"));
        //之所以分开,是因为要逐行执行,而模板中中间部分时其他地方的数据..所以这里要分开然后按照模板的排序来分别插入
        HashMap<String, Object> dateMap2 = new HashMap<String, Object>();
        String nowDate= DateUtil.today();
        dateMap2.put("nowDate",nowDate);
        UserInfo userInfo = (UserInfo)ShiroUtil.getPrincipal();
        String name="未登录用户";
        if(userInfo!=null){
            name=userInfo.getName();
        }
        dateMap2.put("operatorName",name);
//        插入统计图表图片临时文件的路径

        //插入统计图表图片临时文件到excel的指定位置并调整其大小
        WriteCellData<Void> writeCellData = new WriteCellData<>();
        // 这里可以设置为 EMPTY 则代表不需要其他数据了
        writeCellData.setType(CellDataTypeEnum.STRING);
        writeCellData.setStringValue("放置统计图片区域");

        // 可以放入多个图片
        List<ImageData> imageDataList = new ArrayList<>();
        //放入第一个图片
//        ImageData imageData = new ImageData();
////        writeCellData.setImageDataList(imageDataList);
//        // 放入二进制图片
//        try {
//            imageData.setImage(FileUtils.readFileToByteArray(new File(filePath)));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // 图片类型
//        imageData.setImageType(ImageData.ImageType.PICTURE_TYPE_JPEG);
//        // 上 右 下 左 需要留空
//        // 这个类似于 css 的 margin
//        // 这里实测 不能设置太大 超过单元格原始大小后 打开会提示修复。暂时未找到很好的解法。
//        imageData.setTop(5);
//        imageData.setRight(40);
//        imageData.setBottom(5);
//        imageData.setLeft(5);
//        imageDataList.add(imageData);
        // 放入第二个图片  -- 这里只需要这一种
        ImageData imageData = new ImageData();
        try {
            imageData.setImage(FileUtils.readFileToByteArray(new File(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageData.setImageType(ImageData.ImageType.PICTURE_TYPE_JPEG);
        imageData.setTop(5);
        imageData.setRight(5);
        imageData.setBottom(5);
        imageData.setLeft(5);
        // 设置图片的位置 假设 现在目标 是 覆盖 当前单元格 和当前单元格右边的单元格
        // 起点相对于当前单元格为0 当然可以不写
        imageData.setRelativeFirstRowIndex(0);
        imageData.setRelativeFirstColumnIndex(0);
        imageData.setRelativeLastRowIndex(0);
        // 前面3个可以不写  下面这个需要写 也就是 结尾 需要相对当前单元格 往右移动一格
        // 也就是说 这个图片会覆盖当前单元格和 后面的那一格
        imageData.setRelativeLastColumnIndex(4);
        imageDataList.add(imageData);
        writeCellData.setImageDataList(imageDataList);
        // 写入数据
//        EasyExcel.write(targetFile).sheet().doWrite(list);
        dateMap2.put("lineChart",writeCellData);

        List<UsersOnlineDurationFill> usersFill=(List<UsersOnlineDurationFill>)usersOnlineDurationMap.get("users");
        // 写入统计数据
        workBook.fill(dateMap1, sheet);
        FillConfig fillConfig = FillConfig.builder().forceNewRow(true).build();
        // 填充并换行
        workBook.fill(usersFill, fillConfig, sheet);
        workBook.fill(dateMap2, sheet);
//        workBook.finish();

        //4.将excel统计图表文件流返回给浏览器
        response.setContentType("application/vnd.ms-excel");
//        response.setContentType("application/octet-stream;charset=ISO8859-1");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        // 目标文件名
        String targetFile = "指定用户在线时长统计报表.xlsx";
        String fileName = null;

        try {
            fileName = URLEncoder.encode(targetFile, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        workBook.finish();
    }

    @Override
    public RspResult activeUsersForData(String usersJson) {
        //1.通过内部方法获取、处理并构建需要的统计数据
        Map<String, Object> activeUsersMap = activeUsers(usersJson);
        if(activeUsersMap!=null){
            return new RspResult(activeUsersMap);
        }
        return RspResult.FAILED;
    }

    @Override
    public void activeUsersForFile(String usersJson) {
        //1.通过内部方法获取、处理并构建需要的统计数据
        Map<String, Object> activeUsersMap = activeUsers(usersJson);

        //2.使用jfreechart工具来生成统计图表文件流
        Map<String, Map<String, Object>> statisticsMap=(Map<String, Map<String, Object>>)activeUsersMap.get("statisticsMap");
        //1)构建jfreechart的对象
        //a1:构建条形图
        JFreeChart groupBarChart = BarChart2DOr3DUtil.createBarChartWithGroup2DOr3D(statisticsMap, null,"指定时间段活跃用户统计条形图", "时间轴", "活跃用户数");
        //将映射[条形统计图]的JFreeChart对象转成指定格式的图片文件,文件先放入指定的位置保存,后面会用定时任务定时清理
        String filePath= null;
        try {
            filePath = ChartToPictureFileUtil.chartToPictureFile(groupBarChart, null, null,
                    webUploadPath+statisticsPath+"/"+ UUID.randomUUID()+".jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(filePath);
        //a2:构建饼状图
        //处理下statisticsMap中的数据格式,以供PieChart2DOr3DUtil可以使用
        Map<String,Object> pieMap=new LinkedHashMap<>();
        for (String key : statisticsMap.keySet()) {
            pieMap.put(key,statisticsMap.get(key).get("活跃用户"));
        }
        JFreeChart pieChart = PieChart2DOr3DUtil.createPieChart2DOr3D(pieMap, null,"指定时间段活跃用户统计饼状图",null,null,null);
        //将映射[条形统计图]的JFreeChart对象转成指定格式的图片文件,文件先放入指定的位置保存,后面会用定时任务定时清理
        String pieFilePath= null;
        try {
            pieFilePath = ChartToPictureFileUtil.chartToPictureFile(pieChart, null, null,
                    webUploadPath+statisticsPath+"/"+ UUID.randomUUID()+".jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(pieFilePath);

        //3.进行excel填充
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(
                "templates/excel/statistical/指定时间段活跃用户统计模板.xlsx");
        // 写入workbook对象
        HttpServletResponse response = WebUtil.getResponse();
        ExcelWriter workBook = null;
        try {
            workBook = EasyExcel.write(response.getOutputStream()).withTemplate(templateInputStream).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WriteSheet sheet = EasyExcel.writerSheet().build();
        // ****** 准备数据 *******
        //这个里面没有多行数据,所以一个dataMap就够了
        HashMap<String, Object> dateMap1 = new HashMap<String, Object>();
        dateMap1.put("start", (String)activeUsersMap.get("start"));
        dateMap1.put("end",(String)activeUsersMap.get("end"));
        dateMap1.put("section",(Integer)activeUsersMap.get("section"));
        dateMap1.put("threshold",(Long)activeUsersMap.get("threshold"));
        dateMap1.put("total",(Long)activeUsersMap.get("total"));
        dateMap1.put("average",(Long)activeUsersMap.get("average"));
        dateMap1.put("maxDate",(String)activeUsersMap.get("maxDate"));
        dateMap1.put("max",(Long)activeUsersMap.get("max"));
        dateMap1.put("minDate",(String)activeUsersMap.get("minDate"));
        dateMap1.put("min",(Long)activeUsersMap.get("min"));
        String nowDate= DateUtil.today();
        dateMap1.put("nowDate",nowDate);
        UserInfo userInfo = (UserInfo)ShiroUtil.getPrincipal();
        String name="未登录用户";
        if(userInfo!=null){
            name=userInfo.getName();
        }
        dateMap1.put("operatorName",name);
//        插入统计图表图片临时文件的路径

        //1)放入条形图统计图片
        //插入统计图表图片临时文件到excel的指定位置并调整其大小
        WriteCellData<Void> writeCellData = new WriteCellData<>();
        // 这里可以设置为 EMPTY 则代表不需要其他数据了
        writeCellData.setType(CellDataTypeEnum.STRING);
        writeCellData.setStringValue("放置统计图片区域");

        // 可以放入多个图片
        List<ImageData> imageDataList = new ArrayList<>();
        //放入第一个图片
//        ImageData imageData = new ImageData();
////        writeCellData.setImageDataList(imageDataList);
//        // 放入二进制图片
//        try {
//            imageData.setImage(FileUtils.readFileToByteArray(new File(filePath)));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // 图片类型
//        imageData.setImageType(ImageData.ImageType.PICTURE_TYPE_JPEG);
//        // 上 右 下 左 需要留空
//        // 这个类似于 css 的 margin
//        // 这里实测 不能设置太大 超过单元格原始大小后 打开会提示修复。暂时未找到很好的解法。
//        imageData.setTop(5);
//        imageData.setRight(40);
//        imageData.setBottom(5);
//        imageData.setLeft(5);
//        imageDataList.add(imageData);
        // 放入第二个图片  -- 这里只需要这一种
        ImageData imageData = new ImageData();
        try {
            imageData.setImage(FileUtils.readFileToByteArray(new File(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageData.setImageType(ImageData.ImageType.PICTURE_TYPE_JPEG);
        imageData.setTop(5);
        imageData.setRight(5);
        imageData.setBottom(5);
        imageData.setLeft(5);
        // 设置图片的位置 假设 现在目标 是 覆盖 当前单元格 和当前单元格右边的单元格
        // 起点相对于当前单元格为0 当然可以不写
        imageData.setRelativeFirstRowIndex(0);
        imageData.setRelativeFirstColumnIndex(0);
        imageData.setRelativeLastRowIndex(0);
        // 前面3个可以不写  下面这个需要写 也就是 结尾 需要相对当前单元格 往右移动一格
        // 也就是说 这个图片会覆盖当前单元格和 后面的那一格
        imageData.setRelativeLastColumnIndex(3);
        imageDataList.add(imageData);
        writeCellData.setImageDataList(imageDataList);
        // 写入数据
//        EasyExcel.write(targetFile).sheet().doWrite(list);
        dateMap1.put("barChart",writeCellData);
        //2)放入饼状图统计图片
        //插入统计图表图片临时文件到excel的指定位置并调整其大小
        WriteCellData<Void> pieWriteCellData = new WriteCellData<>();
        // 这里可以设置为 EMPTY 则代表不需要其他数据了
        pieWriteCellData.setType(CellDataTypeEnum.STRING);
        pieWriteCellData.setStringValue("放置统计图片区域");

        // 可以放入多个图片
        List<ImageData> pieImageDataList = new ArrayList<>();
        ImageData pieImageData = new ImageData();
        try {
            pieImageData.setImage(FileUtils.readFileToByteArray(new File(pieFilePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pieImageData.setImageType(ImageData.ImageType.PICTURE_TYPE_JPEG);
        pieImageData.setTop(5);
        pieImageData.setRight(5);
        pieImageData.setBottom(5);
        pieImageData.setLeft(5);
        // 设置图片的位置 假设 现在目标 是 覆盖 当前单元格 和当前单元格右边的单元格
        // 起点相对于当前单元格为0 当然可以不写
        pieImageData.setRelativeFirstRowIndex(0);
        pieImageData.setRelativeFirstColumnIndex(0);
        pieImageData.setRelativeLastRowIndex(0);
        // 前面3个可以不写  下面这个需要写 也就是 结尾 需要相对当前单元格 往右移动一格
        // 也就是说 这个图片会覆盖当前单元格和 后面的那一格
        pieImageData.setRelativeLastColumnIndex(3);
        pieImageDataList.add(pieImageData);
        pieWriteCellData.setImageDataList(pieImageDataList);
        // 写入数据
//        EasyExcel.write(targetFile).sheet().doWrite(list);
        dateMap1.put("pieChart",pieWriteCellData);

//        List<UsersOnlineDurationFill> usersFill=(List<UsersOnlineDurationFill>)usersOnlineDurationMap.get("users");
        // 写入统计数据
        workBook.fill(dateMap1, sheet);
        FillConfig fillConfig = FillConfig.builder().forceNewRow(true).build();
//        // 填充并换行
//        workBook.fill(usersFill, fillConfig, sheet);
//        workBook.fill(dateMap2, sheet);
//        workBook.finish();

        //4.将excel统计图表文件流返回给浏览器
        response.setContentType("application/vnd.ms-excel");
//        response.setContentType("application/octet-stream;charset=ISO8859-1");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        // 目标文件名
        String targetFile = "指定时间段活跃用户统计报表.xlsx";
        String fileName = null;

        try {
            fileName = URLEncoder.encode(targetFile, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        workBook.finish();
    }
    //    @Override
    private Map<String,Object> usersOnlineDuration(String usersJson) {
        JSONObject jsonObject = JSONUtil.parseObj(usersJson);
        String start=jsonObject.getStr("start");
        String end = jsonObject.getStr("end");
        //增加统计时间段的分段值:比如,section=2时,表示要以每2个为单位来记为一组
        Integer section=jsonObject.getInt("section",1);
        Map users = (Map)jsonObject.get("users");
        List<Long> ids=new ArrayList<>();
        users.forEach((key,value)->{
            ids.add(Long.valueOf((String) key));
        });

        //1.统计sql构建并获取相应的数据
        //1)判断是以年\月\日为计的:yyyy,年;yyyy-m,月;yyyy-m-d,日
        QueryWrapper<AccountLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", ids).eq("method", "login");
        int model;
        if (start.length() == 4) {//按年
            model = 0;
            //eg:2022-01-01
            start = start + "-01-01";
            end = end + "-01-01";
        } else if (start.length() == 7) {//按月
            model = 1;
            //eg:2022-06-01
            start = start + "-01";
            end = end + "-01";
        } else {//按日
            model = 2;
            //eg:2022-03-12
        }
//        select * from account_log where user_id=1 and method='login' and
//                (creatime<'2021-01-01' and updatime>='2021-01-01' and updatime<'2021-05-01') or
//                (creatime>='2021-01-01' and updatime<'2021-05-01') or
//                (creatime>='2021-01-01' and creatime<'2021-05-01' and updatime>='2021-05-01') or
//                (creatime<'2021-01-01' and updatime>='2021-05-01') or
//                (creatime>='2021-01-01' and creatime<'2021-05-01' and updatime is null) or
//                (creatime<'2021-01-01' and updatime is null)
        queryWrapper.lt("creatime", start).ge("updatime", start).lt("updatime",end).or().
                ge("creatime",start).lt("updatime",end).or().
                ge("creatime",start).lt("creatime",end).ge("updatime",end).or().
                lt("creatime",start).ge("updatime",end).or().
                ge("creatime",start).lt("creatime",end).isNull("updatime").or().//updatime为null表示用户在统计时段内还未退出系统
                lt("creatime",start).isNull("updatime");

        //默认按创建时间的升序排列,后面要用来进行配对
        List<AccountLog> accountLogs = accountLogMapper.selectList(queryWrapper.orderByAsc("creatime"));
        //2)构建数据
        Map<String, Map<String, Object>> statisticsMap= new HashMap<>();
        //根据用户id将集合分组
        String finalStart = start;
        String finalEnd = end;

        List<UsersOnlineDurationFill> userFills=new ArrayList<>();

        users.forEach((id, name)->{
            Long total=0l;
            Long max=0l;
            String maxDate=null;
            Long min=Long.MAX_VALUE;//这里设置成最大的,这样第一个数据一定会将其替换掉..从而进行正常的循环对比的流程
            String minDate=null;

            long parts=1;//求平均值的参数,至少是1

            Date startDate = DateUtil.parse(finalStart);
            Date endDate = DateUtil.parse(finalEnd);
            Map<String, Object> userMap = new LinkedHashMap<>();
            if (model == 0) {//按年
                long betweenYear = DateUtil.betweenYear(startDate, endDate, true);
                parts=betweenYear%section==0?betweenYear/section:betweenYear/section+1;//分段后的段数
                for (long i = 0; i < parts; i++) {
                    int year = DateUtil.year(startDate) + (int) i*section;
                    //增加对应的时间分量
                    Date currentStartDate = DateUtil.parse(year + "01-01", "yyyy-MM-dd");
                    Date currentEndDate = DateUtil.parse(year + section + "01-01", "yyyy-MM-dd");

                    Long onlineHours = calculateOnlineHours(accountLogs, Long.valueOf((String) id), currentStartDate, currentEndDate,endDate);
                    if(onlineHours>=max){//如果当前分段的时段长度比最大的还大,则它就是最大的
                        max=onlineHours;
                        maxDate=year + "年";
                    }
                    if(onlineHours<min){//如果当前分段的时间长度比最小的还小,则它就是最小的
                        min=onlineHours;
                        minDate=year + "年";
                    }
                    total+=onlineHours;//计入总时长
                    //在这里对每个用户在指定时段内进行统计,值存入当前userMap的value中
                    userMap.put(year + "年", onlineHours);
                }
            } else if (model == 1) {//按月
                long betweenMonth = DateUtil.betweenMonth(startDate, endDate, true);
                parts=betweenMonth%section==0?betweenMonth/section:betweenMonth/section+1;//分段后的段数
                for (long i = 0; i < parts; i++) {
                    Date currentStartDate = DateUtil.offsetMonth(startDate, (int) i*section);
                    int year = DateUtil.year(currentStartDate);
                    int month = DateUtil.month(currentStartDate) + 1;
                    //增加对应的时间分量
                    Date currentEndDate = DateUtil.offsetMonth(currentStartDate, section);

                    Long onlineHours = calculateOnlineHours(accountLogs, Long.valueOf((String) id), currentStartDate, currentEndDate,endDate);
                    if(onlineHours>=max){//如果当前分段的时段长度比最大的还大,则它就是最大的
                        max=onlineHours;
                        maxDate=year + "年" + month + "月";
                    }
                    if(onlineHours<min){//如果当前分段的时间长度比最小的还小,则它就是最小的
                        min=onlineHours;
                        minDate=year + "年" + month + "月";
                    }
                    total+=onlineHours;//计入总时长
                    //在这里对每个用户在指定时段内进行统计,值存入当前userMap的value中
                    userMap.put(year + "年" + month + "月",onlineHours);
                }
            } else {//按日
                long betweenDay = DateUtil.betweenDay(startDate, endDate, true);
                parts=betweenDay%section==0?betweenDay/section:betweenDay/section+1;//分段后的段数
                for (long i = 0; i < parts; i++) {
                    Date currentStartDate = DateUtil.offsetDay(startDate, (int) i*section);
                    int year = DateUtil.year(currentStartDate);
                    int month = DateUtil.month(currentStartDate) + 1;
                    int day = DateUtil.dayOfMonth(currentStartDate);
                    //增加对应的时间分量
                    Date currentEndDate = DateUtil.offsetDay(currentStartDate, section);

                    Long onlineHours = calculateOnlineHours(accountLogs, Long.valueOf((String) id), currentStartDate, currentEndDate,endDate);
                    if(onlineHours>=max){//如果当前分段的时段长度比最大的还大,则它就是最大的
                        max=onlineHours;
                        maxDate=year + "年" + month + "月" + day + "日";
                    }
                    if(onlineHours<min){//如果当前分段的时间长度比最小的还小,则它就是最小的
                        min=onlineHours;
                        minDate=year + "年" + month + "月" + day + "日";
                    }
                    total+=onlineHours;//计入总时长
                    //在这里对每个用户在指定时段内进行统计,值存入当前userMap的value中
                    userMap.put(year + "年" + month + "月" + day + "日", onlineHours);
                }
            }

            Long average=total/parts;

            UsersOnlineDurationFill userFill=new UsersOnlineDurationFill();
            userFill.setId(Long.valueOf((String)id));
            userFill.setName((String)name);
            userFill.setTotal(total);
            userFill.setAverage(average);
            userFill.setMax(max);
            userFill.setMaxDate(maxDate);
            userFill.setMin(min);
            userFill.setMinDate(minDate);
            userFills.add(userFill);

            statisticsMap.put((String)name, userMap);
        });

        Map<String,Object> usersOnlineDurationMap=new HashMap<>();
        usersOnlineDurationMap.put("start",jsonObject.getStr("start"));
        usersOnlineDurationMap.put("end",jsonObject.getStr("end"));
        usersOnlineDurationMap.put("users",userFills);
        usersOnlineDurationMap.put("section",section);
        usersOnlineDurationMap.put("statisticsMap",statisticsMap);

        return usersOnlineDurationMap;
    }

    private Map<String,Object> activeUsers(String activeUsersJson) {
        JSONObject jsonObject = JSONUtil.parseObj(activeUsersJson);
        String start=jsonObject.getStr("start");
        String end = jsonObject.getStr("end");
        //增加统计时间段的分段值:比如,section=2时,表示要以每2个为单位来记为一组
        Integer section=jsonObject.getInt("section",1);
        //门槛阈值
        Long threshold=jsonObject.getLong("threshold", LocalCacheConstantService.getValue("statistics:activeUserThreshold",Long.class));

        //1.统计sql构建并获取相应的数据
        //1)判断是以年\月\日为计的:yyyy,年;yyyy-m,月;yyyy-m-d,日
        QueryWrapper<AccountLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("method", "login");
        int model;
        if (start.length() == 4) {//按年
            model = 0;
            //eg:2022-01-01
            start = start + "-01-01";
            end = end + "-01-01";
        } else if (start.length() == 7) {//按月
            model = 1;
            //eg:2022-06-01
            start = start + "-01";
            end = end + "-01";
        } else {//按日
            model = 2;
            //eg:2022-03-12
        }
//        select * from account_log where method='login' and
//                (creatime<'2021-01-01' and updatime>='2021-01-01' and updatime<'2021-05-01') or
//                (creatime>='2021-01-01' and updatime<'2021-05-01') or
//                (creatime>='2021-01-01' and creatime<'2021-05-01' and updatime>='2021-05-01') or
//                (creatime<'2021-01-01' and updatime>='2021-05-01') or
//                (creatime>='2021-01-01' and creatime<'2021-05-01' and updatime is null) or
//                (creatime<'2021-01-01' and updatime is null)
        queryWrapper.lt("creatime", start).ge("updatime", start).lt("updatime",end).or().
                ge("creatime",start).lt("updatime",end).or().
                ge("creatime",start).lt("creatime",end).ge("updatime",end).or().
                lt("creatime",start).ge("updatime",end).or().
                ge("creatime",start).lt("creatime",end).isNull("updatime").or().//updatime为null表示用户在统计时段内还未退出系统
                lt("creatime",start).isNull("updatime");

        //默认按创建时间的升序排列,后面要用来进行配对
        List<AccountLog> accountLogs = accountLogMapper.selectList(queryWrapper.orderByAsc("creatime"));
        //2)构建数据
        //根据用户id将集合分组
        String finalStart = start;
        String finalEnd = end;

        //根据用户id分组
        Map<Long, List<AccountLog>> accountLogGroups = accountLogs.stream().collect(groupingBy(item -> item.getUserId()));
        //由于下面代码的时间从小往大遍历的,所以这里用有序map的话一定能保证元素顺序按照时间来排序的
        Map<String,Long> dateSectionUsersNumMap=new LinkedHashMap<>();
        long parts=1;//求平均值的参数,至少是1
        for (Long id : accountLogGroups.keySet()) {
            Date startDate = DateUtil.parse(finalStart);
            Date endDate = DateUtil.parse(finalEnd);
            if (model == 0) {//按年
                long betweenYear = DateUtil.betweenYear(startDate, endDate, true);
                parts=betweenYear%section==0?betweenYear/section:betweenYear/section+1;//分段后的段数
                for (long i = 0; i < parts; i++) {
                    int year = DateUtil.year(startDate) + (int) i*section;

                    //为了防止出现某个时间段内完全没有用户从而使得dateSectionUsersNumMap集合缺少这个时间段的元素的情况,要每次进行判断
                    Long value = dateSectionUsersNumMap.get(year + "年");
                    if(value==null){//说明还未创建对应的元素,这里要创建出来
                        value=0l;
                        dateSectionUsersNumMap.put(year + "年",value);
                    }

                    //增加对应的时间分量
                    Date currentStartDate = DateUtil.parse(year + "01-01", "yyyy-MM-dd");
                    Date currentEndDate = DateUtil.parse(year + section + "01-01", "yyyy-MM-dd");
                    //表示某个用户在某个时间段内的在线时间
                    Long onlineHours = calculateOnlineHours(accountLogs, id, currentStartDate, currentEndDate,endDate);
                    if(onlineHours>=threshold){//如果在线时间大于门槛值
                        value=+1l;
                        dateSectionUsersNumMap.put(year + "年",value);
                    }
                }
            } else if (model == 1) {//按月
                long betweenMonth = DateUtil.betweenMonth(startDate, endDate, true);
                parts=betweenMonth%section==0?betweenMonth/section:betweenMonth/section+1;//分段后的段数
                for (long i = 0; i < parts; i++) {
                    Date currentStartDate = DateUtil.offsetMonth(startDate, (int) i*section);
                    int year = DateUtil.year(currentStartDate);
                    int month = DateUtil.month(currentStartDate) + 1;

                    //为了防止出现某个时间段内完全没有用户从而使得dateSectionUsersNumMap集合缺少这个时间段的元素的情况,要每次进行判断
                    Long value = dateSectionUsersNumMap.get(year + "年" + month + "月");
                    if(value==null){//说明还未创建对应的元素,这里要创建出来
                        value=0l;
                        dateSectionUsersNumMap.put(year + "年" + month + "月",value);
                    }

                    //增加对应的时间分量
                    Date currentEndDate = DateUtil.offsetMonth(currentStartDate, section);
                    //表示某个用户在某个时间段内的在线时间
                    Long onlineHours = calculateOnlineHours(accountLogs, id, currentStartDate, currentEndDate,endDate);
                    if(onlineHours>=threshold){//如果在线时间大于门槛值
                        value+=1l;
                        dateSectionUsersNumMap.put(year + "年" + month + "月",value);
                    }
                }
            } else {//按日
                long betweenDay = DateUtil.betweenDay(startDate, endDate, true);
                parts=betweenDay%section==0?betweenDay/section:betweenDay/section+1;//分段后的段数
                for (long i = 0; i < parts; i++) {
                    Date currentStartDate = DateUtil.offsetDay(startDate, (int) i*section);
                    int year = DateUtil.year(currentStartDate);
                    int month = DateUtil.month(currentStartDate) + 1;
                    int day = DateUtil.dayOfMonth(currentStartDate);

                    //为了防止出现某个时间段内完全没有用户从而使得dateSectionUsersNumMap集合缺少这个时间段的元素的情况,要每次进行判断
                    Long value = dateSectionUsersNumMap.get(year + "年" + month + "月" + day + "日");
                    if(value==null){//说明还未创建对应的元素,这里要创建出来
                        value=0l;
                        dateSectionUsersNumMap.put(year + "年" + month + "月" + day + "日",value);
                    }

                    //增加对应的时间分量
                    Date currentEndDate = DateUtil.offsetDay(currentStartDate, section);

                    //表示某个用户在某个时间段内的在线时间
                    Long onlineHours = calculateOnlineHours(accountLogs, null, currentStartDate, currentEndDate,endDate);
                    if(onlineHours>=threshold){//如果在线时间大于门槛值
                        value=+1l;
                        dateSectionUsersNumMap.put(year + "年" + month + "月" + day + "日",value);
                    }
                }
            }
        }

        //根据统计来的dateSectionUsersNumMap的数据,来进行进一步的数据分析和汇总
        Long total=0l;
        Long max=0l;
        String maxDate=null;
        Long min=Long.MAX_VALUE;//这里设置成最大的,这样第一个数据一定会将其替换掉..从而进行正常的循环对比的流程
        String minDate=null;
        Map<String, Map<String, Object>> statisticsMap= new LinkedHashMap<>();
        for (String name : dateSectionUsersNumMap.keySet()) {
            Long value=dateSectionUsersNumMap.get(name);
            //1)构建jfreechart需要用到的那个数据集
            Map<String, Object> map=new HashMap<>();//只有一个元素
            map.put("活跃用户",value);
            statisticsMap.put(name,map);//相当于为了满足jfreechart的条形图的构建格式要求而加了一层key值
            //2)求最多人数的时间段和那个时间段的人数
            if(value>=max){
                max=value;
                maxDate=name;
            }
            //3)求最少人数的时间段和那个时间段的人数
            if(value<min){
                min=value;
                minDate=name;
            }
            //4)求总人数
            total+=value;
        }
        //5)求平均人数
        Long average=total/parts;

        //最后将求得的数据放入最终的集合中
        Map<String,Object> activeUsersMap=new HashMap<>();
        activeUsersMap.put("start",jsonObject.getStr("start"));
        activeUsersMap.put("end",jsonObject.getStr("end"));
        activeUsersMap.put("section",section);
        activeUsersMap.put("threshold",threshold);

        activeUsersMap.put("total",total);
        activeUsersMap.put("average",average);
        activeUsersMap.put("maxDate",maxDate);
        activeUsersMap.put("max",max);
        activeUsersMap.put("minDate",minDate);
        activeUsersMap.put("min",min);
        activeUsersMap.put("statisticsMap",statisticsMap);

        return activeUsersMap;
    }


    //*************本类的私有的公共方法*************
    //计算某个时间段内的指定用户的在线时长
    private Long calculateOnlineHours(List<AccountLog> accountLogs,Long userId,Date currentStartDate,Date currentEndDate,Date endDate){
        //去匹配每个log记录
        Long onlineHours = 0l;//当前时间段的当前用户的在线时长统计值
        long ms;
        for (AccountLog accountLog : accountLogs) {
            if(userId==null){//如果为null,表示不考虑是哪个具体的用户,而是全部用户都算
                //全部用户都要算一遍
            }else if(!accountLog.getUserId().equals(userId)){
                continue;//不是当前用户的记录,直接过
            }
            ms=0l;
            //对于updatime为null的情况的处理:按当前时间的最大值算
            if(accountLog.getUpdatime()==null){
                accountLog.setUpdatime(endDate);
            }
            //4种情况
            //c<start&u>=start&u<end -> 取start-u段
            if(accountLog.getCreatime().getTime()<currentStartDate.getTime()&&
                    accountLog.getUpdatime().getTime()>=currentStartDate.getTime()&&
                    accountLog.getUpdatime().getTime()<currentEndDate.getTime()){
                ms=accountLog.getUpdatime().getTime()-currentStartDate.getTime();
            }else
            //c>=Start&u<end -> 取c-u段
            if(accountLog.getCreatime().getTime()>=currentStartDate.getTime()&&
                    accountLog.getUpdatime().getTime()< currentEndDate.getTime()){
                ms=accountLog.getUpdatime().getTime()-accountLog.getCreatime().getTime();
            }else
            //c>=start&c<end&u>=end -> 取c-end段
            if(accountLog.getCreatime().getTime()>= currentStartDate.getTime()&&
                    accountLog.getCreatime().getTime()< currentEndDate.getTime()&&
                    accountLog.getUpdatime().getTime()>= currentEndDate.getTime()){
                ms= currentEndDate.getTime()-accountLog.getCreatime().getTime();

            }else
            //c<Start&u>=end -> 取start-end段
            if(accountLog.getCreatime().getTime()< currentStartDate.getTime()&&
                    accountLog.getUpdatime().getTime()>= currentEndDate.getTime()){
                ms= currentEndDate.getTime()- currentStartDate.getTime();
            }
            onlineHours+=ms/(1000*60*60);
        }
        return onlineHours;
    }


}
