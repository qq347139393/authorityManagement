package com.planet.util.JFreeChartUtil;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * 生成映射指定的[2D或3D分组条形统计图]的JFreeChart对象的工具类
 * @author father
 *
 */
@Slf4j
public class BarChart2DOr3DUtil {

	/** 统计图的头标题的默认值 */
	public static final String HEAD_TITLE="[2D或3D分组条形图]统计汇总";
	/** 3D效果开关的默认值:1,表示开启3D效果,生成3D分组条形统计图;不为1时,表示不开启3D效果,生成2D分组条形统计图 */
	public static final Integer FLAG_3D=0;
	/** 默认的分组条形统计图的x轴标题 */
	public static final String X_Title="若干个分组";
	/** 默认的分组条形统计图的y轴标题 */
	public static final String Y_TITLE="数值";
	/** 默认的字体 */
	public static final String FONT="宋体";
	/** 默认的字号 */
	public static final Integer FONT_SIZE=15;

	/**
	 * 生成映射指定的[2D或3D分组条形统计图]的JFreeChart对象的工具方法
	 * @param dataMaps 构建[2D或3D分组条形统计图]所需要的数据的数据集
	 * @param flag3D 3D效果开关:1,表示开启3D效果,生成3D分组条形统计图;不为1时,表示不开启3D效果,生成2D分组条形统计图
	 * @param headTitle 统计图的头标题
	 * @param xTitle 柱状图的x轴标题
	 * @param yTitle 柱状图的y轴标题
	 * @return 映射指定的[2D或3D分组条形统计图]的JFreeChart对象
	 */
	public static JFreeChart createBarChartWithGroup2DOr3D(Map<String,Map<String,Object>> dataMaps,Integer flag3D,String headTitle,String xTitle,String yTitle){
		//1.参数值验证与调整
		if(dataMaps==null||dataMaps.size()==0) {
			log.error("生成分组条形统计图所需要的统计数据不能为空,生成映射[分组条形统计图]的JFreeChart对象失败..");
			return null;
		}
		if(flag3D==null) {
			flag3D=FLAG_3D;
		}
		if(headTitle==null) {
			headTitle=HEAD_TITLE;
		}
		if(xTitle==null) {
			xTitle=X_Title;
		}
		if(yTitle==null) {
			yTitle=Y_TITLE;
		}

		//2.构建[2D或3D分组条形统计图]所需要的指定结构的数据
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (String groupKey : dataMaps.keySet()) {
			Map<String, Object> dataMap = dataMaps.get(groupKey);
			for (String key : dataMap.keySet()) {
				Object value=dataMap.get(key);
				if(value==null) {
					dataset.addValue( 0.0 , groupKey , key );
				}else if(value instanceof Double) {
					dataset.addValue( (double)value , groupKey , key );
				}else if(value instanceof Integer) {
					dataset.addValue( (Integer)value , groupKey , key );
				}else if(value instanceof Long){
					dataset.addValue( (Long)value , groupKey , key );
				}else if(value instanceof String){
					dataset.addValue( Double.valueOf((String)value) , groupKey , key );
				}else{
					log.error("给定的数据集中的Map中的value类型错误,生成映射[条形统计图]的JFreeChart对象失败..");
				}
			}

		}

		//3.创建映射[2D或3D分组条形统计图]的JFreeChart对象
		JFreeChart barChart2DOr3D=null;
		if(flag3D==1) {
			//3D开关为1时,表示开启3D效果:生成3D分组条形统计图
			barChart2DOr3D=ChartFactory.createBarChart3D(headTitle,xTitle,yTitle,dataset,PlotOrientation.VERTICAL, true, true, false);
		}else {
			//3D开关不为1时,表示不开启3D效果:生成2D分组条形统计图
			barChart2DOr3D=ChartFactory.createBarChart(headTitle,xTitle,yTitle,dataset,PlotOrientation.VERTICAL, true, true, false);
		}

		//4.处理中文显示乱码以及设置一些显示样式
		//处理主标题乱码
		barChart2DOr3D.getTitle().setFont(new Font(FONT,Font.BOLD,FONT_SIZE+3));
		//处理子标题乱码
		barChart2DOr3D.getLegend().setItemFont(new Font(FONT,Font.BOLD,FONT_SIZE));
//		barChart.getLegend().setPosition(RectangleEdge.RIGHT);//右侧显示子菜单

		//调出图表区域对象
		CategoryPlot categoryPlot = (CategoryPlot) barChart2DOr3D.getPlot();
		//获取到X轴
		CategoryAxis categoryAxis = (CategoryAxis) categoryPlot.getDomainAxis();
		//获取到Y轴
		NumberAxis numberAxis = (NumberAxis) categoryPlot.getRangeAxis();
		//处理X轴外的乱码
		categoryAxis.setLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//处理X轴上的乱码
		categoryAxis.setTickLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//处理Y轴外的乱码
		numberAxis.setLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//处理Y轴上的乱码
		numberAxis.setTickLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));

		//处理图形，先要获取绘图区域对象
		BarRenderer barRenderer = (BarRenderer) categoryPlot.getRenderer();
		//设置图形的宽度
//		barRenderer.setMaximumBarWidth(0.1);

		//*在图形上显示对应数值
		barRenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		barRenderer.setBaseItemLabelsVisible(true);
		barRenderer.setBaseItemLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//+++++++++++++

		return barChart2DOr3D;
	}

	//测试
	public static void main(String[] args) throws IOException {
		//构建分组条形统计图所需要的数据集
		Random r=new Random();
		Map<String,Map<String,Object>> dataMaps=new LinkedHashMap<>();
		Integer groupSum=5;
		for (int i = 0; i < groupSum; i++) {
			Map<String,Object> dataMap=new LinkedHashMap<>();
			dataMap.put("活跃用户量",r.nextInt(100));
//			dataMap.put("大豆",r.nextInt(100));
//			dataMap.put("小麦",r.nextInt(100));
//			dataMap.put("高粱",r.nextInt(100));

			dataMaps.put("第"+(i+1)+"组",dataMap);
		}

		String fileOrFilePath="D://BarChartWithGroup2DOr3D.jpg";

		Integer height=groupSum*4;

		//调用工具方法
		//1)生成映射[饼状统计图]的JFreeChart对象
		JFreeChart chart=createBarChartWithGroup2DOr3D(dataMaps,null, null, null, null);
		//2)将映射[饼状统计图]的JFreeChart对象转成指定格式的图片文件
		String filePath=ChartToPictureFileUtil.chartToPictureFile(chart, height*50, null, fileOrFilePath);
		System.out.println(filePath);
	}

}
