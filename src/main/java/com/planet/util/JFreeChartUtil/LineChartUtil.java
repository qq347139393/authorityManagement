package com.planet.util.JFreeChartUtil;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * 生成映射指定的[折线统计图]的JFreeChart对象的工具类
 * @author father
 *
 */
@Slf4j
public class LineChartUtil {
	/** 折线图的唯一的折线名称 */
	public static final String LINE_NAME="连续折线";
	/** 统计图的头标题的默认值 */
	public static final String HEAD_TITLE="[折线图]统计汇总";
	/** 折线图的x轴标题的默认值 */
	public static final String X_Title="变化趋势";
	/** 折线图的y轴标题的默认值 */
	public static final String Y_TITLE="数值";
	/** 默认的字体 */
	public static final String FONT="宋体";
	/** 默认的字号 */
	public static final Integer FONT_SIZE=15;

	/**
	 * 生成映射指定的多条折线的[折线组统计图]的JFreeChart对象的工具方法
	 * @param dataMaps 构建[折线组统计图]所需要的数据的数据集
	 * @param headTitle 统计图的头标题
	 * @param xTitle 折线图的x轴标题
	 * @param yTitle 折线图的y轴标题
	 * @return 映射指定的[折线统计图]的JFreeChart对象
	 */
	public static JFreeChart createGroupLineChart(Map<String,Map<String,Object>> dataMaps,String headTitle,String xTitle,String yTitle) {
		//1.参数值验证与调整
		if(dataMaps==null) {
			log.error("生成折线图所需要的统计数据不能为空,生成映射[折线统计图]的JFreeChart对象失败..");
			return null;
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

		//2.构建[折线统计图]所需要的指定结构的数据
		DefaultCategoryDataset lineChartDataset = new DefaultCategoryDataset();

		//dataMapList的每个元素,表示当前[折线统计图]中有多少个折线节点
		for (String groupKey : dataMaps.keySet()) {
			Map<String, Object> dataMap = dataMaps.get(groupKey);
			for (String key : dataMap.keySet()) {
				Object value=dataMap.get(key);
				if(value==null) {
					lineChartDataset.addValue( 0.0 , groupKey , key );
				}else if(value instanceof Double) {
					lineChartDataset.addValue( (double)value , groupKey , key );
				}else if(value instanceof Integer) {
					lineChartDataset.addValue( (Integer)value , groupKey , key );
				}else if(value instanceof Long){
					lineChartDataset.addValue( (Long)value , groupKey , key );
				}else if(value instanceof String){
					lineChartDataset.addValue( Double.valueOf((String)value) , groupKey , key );
				}else{
					log.error("给定的数据集中的Map中的value类型错误,生成映射[折线统计图]的JFreeChart对象失败..");
				}
			}
		}

		//3.创建映射[折线统计图]的JFreeChart对象
		JFreeChart lineChart = ChartFactory.createLineChart(
			headTitle,
			xTitle,
			yTitle,
			lineChartDataset,PlotOrientation.VERTICAL,
			true,true,false);

		//4.处理中文显示乱码以及设置一些显示样式
		//处理主标题乱码
		lineChart.getTitle().setFont(new Font(FONT,Font.BOLD,FONT_SIZE+3));
		//处理子标题乱码
		lineChart.getLegend().setItemFont(new Font(FONT,Font.BOLD,FONT_SIZE));
//		barChart.getLegend().setPosition(RectangleEdge.RIGHT);//右侧显示子菜单

		//调出图表区域对象
		CategoryPlot categoryPlot = (CategoryPlot) lineChart.getPlot();
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

		//*在图形上显示每个折线点对应的数值
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) categoryPlot.getRenderer();
		renderer.setBaseItemLabelsVisible(true);
		renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);
		renderer.setBaseItemLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));

		//+++++++++++++

		return lineChart;
	}

	//测试
	public static void main(String[] args) throws IOException {
		//1.构建折线统计图所需要的数据集
		Random r=new Random();
		Map<String,Map<String,Object>> dataMaps=new LinkedHashMap<>();
		for (int i = 0; i < 3; i++) {
			String groupName="group"+(i+1);
			Map<String,Object> dataMap=new LinkedHashMap<>();
			for (int i1 = 0; i1 < 8; i1++) {
				String name="第"+(i1+1)+"天";
				dataMap.put(name,r.nextInt(200)+0.5);
			}
			dataMaps.put(groupName,dataMap);
		}

		Object fileOrFilePath="D://testLine.png";


		//2.调用工具方法
		//1)生成映射[折线统计图]的JFreeChart对象
		JFreeChart chart=createGroupLineChart(dataMaps, null, null, null);
		//2)将映射[折线统计图]的JFreeChart对象转成指定格式的图片文件
		String filePath=ChartToPictureFileUtil.chartToPictureFile(chart, null, null, fileOrFilePath);
		System.out.println(filePath);
	}


}
