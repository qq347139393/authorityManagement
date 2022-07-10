package com.planet.util.JFreeChartUtil;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 生成映射指定的[散点折线统计图]的JFreeChart对象的工具类
 * @author father
 *
 */
public class XYLineChartUtil{
	/** 折线图的唯一的折线名称 */
	public static final String LINE_NAME="散点折线";
	/** 统计图的头标题的默认值 */
	public static final String HEAD_TITLE="[散点折线图]统计汇总";
	/** 折线图的x轴标题的默认值 */
	public static final String X_Title="变化趋势";
	/** 折线图的y轴标题的默认值 */
	public static final String Y_TITLE="数值";
	/** 默认的字体 */
	public static final String FONT="宋体";
	/** 默认的字号 */
	public static final Integer FONT_SIZE=15;
	/**
	 * 生成映射指定的[散点折线统计图]的JFreeChart对象的工具方法
	 * @param dataLists 构建[散点折线统计图]所需要的数据的数据集
	 * @param lineNames 若干条折线名称
	 * @param headTitle 统计图的头标题
	 * @param xTitle 散点折线图的x轴标题
	 * @param yTitle 散点折线图的y轴标题
	 * @return 映射指定的[散点折线统计图]的JFreeChart对象
	 */
	public static JFreeChart createXYLineChart(List<List<List<Object>>> dataLists,List<String> lineNames,String headTitle,String xTitle,String yTitle) {

		//1.参数值验证与调整
		if(dataLists==null||dataLists.isEmpty()) {
			System.err.println("生成散点折线图所需要的统计数据不能为空,生成映射[散点折线统计图]的JFreeChart对象失败..");
			return null;
		}
		if(lineNames==null||lineNames.isEmpty()) {
			lineNames=new ArrayList<String>();
			//如果折线名称集合为空,则创建与生成散点折线图所需要的统计数据集的折线个数相等的折线名称字符串然后逐个放入折线名称集合中
			for (int i = 0; i < dataLists.size(); i++) {
				String lineName=LINE_NAME+(i+1);
				lineNames.add(lineName);
			}
		}
		Integer lineNamesSizeLack=(lineNames.size()-dataLists.size())<0?-(lineNames.size()-dataLists.size()):0;
		if(lineNamesSizeLack!=0) {
			/*
			 * 如果折线名称集合的长度比生成散点折线图所需要的统计数据集的长度少,
			 * 则创建与生成散点折线图所需要的统计数据集的折线个数相等的折线名称字符串然后逐个放入折线名称集合中,
			 * 从而保证折线名称集合的元素个数总是大于等于生成散点折线图所需要的统计数据集的元素个数
			 */
			Integer lineNamesSize=lineNames.size();
			for (int i = 0; i < lineNamesSizeLack; i++) {
				String lineName=LINE_NAME+(i+1+lineNamesSize);
				lineNames.add(lineName);
			}
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

		//2.构建[散点折线统计图]所需要的指定结构的数据
		XYSeriesCollection XYDataset = new XYSeriesCollection();
		for (int i = 0; i < dataLists.size(); i++) {
			//dataLists的每个元素,代表一条折线
			List<List<Object>> dataList=dataLists.get(i);

			//lineName表示当前这条折线的显示名称
			String lineName=lineNames.get(i);
			//xYSeries是映射当前这条折线的XYSeries类的对象
			XYSeries xYSeries = new XYSeries(lineName);

			for (int j = 0; j < dataList.size(); j++) {
				//dataList的每个元素,代表当前这条折线上的每个节点
				List<Object> data=dataList.get(j);

				//data上固定只有两个元素:第一个元素表示当前节点的x轴的值;第二个元素表示当前节点的y轴的值
				Double xValue=0.0;
				Double yValue=0.0;
				Object xObj=data.get(0);
				Object yObj=data.get(1);
				if(xObj==null) {
					xValue=0.0;
				}else if(xObj instanceof Double) {
					xValue=(double)xObj;
				}else if(xObj instanceof Integer) {
					xValue=(double)(Integer)xObj;
				}else if(xObj instanceof String) {
					xValue=Double.valueOf((String)xObj);
				}else{
					System.err.println("给定的数据集中的xValue类型错误,生成映射[散点折线统计图]的JFreeChart对象失败..");
				}

				if(yObj==null) {
					yValue=0.0;
				}else if(yObj instanceof Double) {
					yValue=(double)yObj;
				}else if(yObj instanceof Integer) {
					yValue=(double)(Integer)yObj;
				}else if(yObj instanceof String) {
					yValue=Double.valueOf((String)yObj);
				}else{
					System.err.println("给定的数据集中的yValue类型错误,生成映射[散点折线统计图]的JFreeChart对象失败..");
				}
				//将当前节点的一组x轴的值和y轴的值存入映射当前折线节点的XYSeries对象中
				xYSeries.add( xValue , yValue );
			}
			//将映射当前折线的XYSeries对象放入构建散点折线数据集的XYSeriesCollection对象中
			XYDataset.addSeries(xYSeries);
		}


		//3.创建映射[散点折线统计图]的JFreeChart对象
		JFreeChart XYLineChart = ChartFactory.createXYLineChart(
			headTitle,
			xTitle,
			yTitle,
			XYDataset,
			PlotOrientation.VERTICAL,
			true, true, false);

		//4.处理中文显示乱码以及设置一些显示样式
		//处理主标题乱码
		XYLineChart.getTitle().setFont(new Font(FONT,Font.BOLD,FONT_SIZE+3));
		//处理子标题乱码
		XYLineChart.getLegend().setItemFont(new Font(FONT,Font.BOLD,FONT_SIZE));
//		barChart.getLegend().setPosition(RectangleEdge.RIGHT);//右侧显示子菜单
		//调出图表区域对象
		XYPlot xYPlot = (XYPlot) XYLineChart.getPlot();
		//获取到X轴
		ValueAxis valueAxis =  xYPlot.getDomainAxis();
//		//获取到Y轴
		NumberAxis numberAxis = (NumberAxis) xYPlot.getRangeAxis();
//		//处理X轴外的乱码
		valueAxis.setLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
//		//处理X轴上的乱码
		valueAxis.setTickLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
//		//处理Y轴外的乱码
		numberAxis.setLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
//		//处理Y轴上的乱码
		numberAxis.setTickLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));

		XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)xYPlot.getRenderer();
		//设置曲线是否显示数据点
		xylineandshaperenderer.setBaseShapesVisible(true);

		//*设置曲线显示各数据点的值
		XYItemRenderer xyitem = xYPlot.getRenderer();
		xyitem.setBaseItemLabelsVisible(true);
		xyitem.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
		//下面三句是对设置折线图数据标示的关键代码
		xyitem.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
		xyitem.setBaseItemLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		xYPlot.setRenderer(xyitem);

		return XYLineChart;
	}

	public static void main(String[] args) throws IOException {
		//1.构建散点折线统计图所需要的数据集
		Random r=new Random();

		List<List<List<Object>>> dataLists=new ArrayList<List<List<Object>>>();
		int lineSum=5;//5条折线
		for (int i = 0; i < lineSum; i++) {
			List<List<Object>> dataList=new ArrayList<List<Object>>();

			int lineNodeSum=6;//每条折线都有6个节点
			for (int j = 0; j < lineNodeSum; j++) {
				List<Object> data=new ArrayList<Object>();
				//每个节点都有固定2个元素:第一个元素表示当前节点的x坐标;第二个元素表示当前节点的y坐标
				data.add(r.nextInt(100));
				data.add(r.nextInt(100));

				//将每个节点放入折线对象所需要的数据集合中
				dataList.add(data);
			}

			//将每个折线对象所需要的数据集合放入散点折线图所需要的总数据集合中
			dataLists.add(dataList);
		}

		Object fileOrFilePath="D://testXYLine.png";



		//2.调用工具方法
		//1)生成映射[散点折线统计图]的JFreeChart对象
		JFreeChart chart=createXYLineChart(dataLists, null, null, null, null);
		//2)将映射[散点折线统计图]的JFreeChart对象转成指定格式的图片文件
		String filePath=ChartToPictureFileUtil.chartToPictureFile(chart, null, null, fileOrFilePath);
		System.out.println(filePath);

	}


}


