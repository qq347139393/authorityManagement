package com.planet.util.JFreeChartUtil;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * 生成映射指定的[气泡统计图]的JFreeChart对象的工具类
 * @author father
 *
 */
public class BubbleChartUtil {

	/** 统计图的头标题的默认值 */
	public static final String HEAD_TITLE="[气泡统计图]统计汇总";
	/** 默认的字体 */
	public static final String FONT="宋体";
	/** 默认的字号 */
	public static final Integer FONT_SIZE=15;
	/** 默认的气泡统计图的x轴标题 */
	public static final String X_Title="变化趋势";
	/** 默认的气泡统计图的y轴标题 */
	public static final String Y_TITLE="数值";
	/** 默认的背景透明度值 */
	public static final Float FOREGROUND_ALPHA=0.60f;
	/** 默认的x值最小的球的球心的x值与最低显示的x值的比值 */
	public static final Double X_LOWER_MARGIN=0.5;
	/** 默认的x值最大的球的球心的x值与右侧头部显示的x值的比值 */
	public static final Double X_UPPER_MARGIN=0.5;
	/** 默认的y值最小的球的球心的y值与最低显示的y值的比值 */
	public static final Double Y_LOWER_MARGIN=0.5;
	/** 默认的y值最大的球的球心的y值与顶部最高显示的y值的比值 */
	public static final Double Y_UPPER_MARGIN=0.5;

	/**
	 * 生成映射指定的[气泡统计图]的JFreeChart对象的工具方法
	 * @param dataArraysList 构建[气泡统计图]所需要的数据的数据集
	 * @param headTitle 统计图的头标题
	 * @param xTitle 气泡统计图的x轴标题
	 * @param yTitle 气泡统计图的y轴标题
	 * @param foregroundAlpha 背景透明度
	 * @param xLowerMargin x值最小的球的球心的x值与最低显示的x值的比值
	 * @param xUpperMargin x值最大的球的球心的x值与右侧头部显示的x值的比值
	 * @param yLowerMargin y值最小的球的球心的y值与最低显示的y值的比值
	 * @param yUpperMargin y值最大的球的球心的y值与顶部最高显示的y值的比值
	 * @return 映射指定的[气泡统计图]的JFreeChart对象
	 */
	public static JFreeChart createBubbleChart(List<Map<String, double[][]>> dataArraysList,String headTitle,String xTitle,String yTitle,Float foregroundAlpha,
			Double xLowerMargin,Double xUpperMargin,Double yLowerMargin,Double yUpperMargin){
		//1.参数值验证与调整
		if(dataArraysList==null||dataArraysList.isEmpty()) {
			System.err.println("生成气泡图所需要的统计数据不能为空,生成映射[气泡统计图]的JFreeChart对象失败..");
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
		if(foregroundAlpha==null) {
			foregroundAlpha=FOREGROUND_ALPHA;
		}
		if(xLowerMargin==null) {
			xLowerMargin=X_LOWER_MARGIN;
		}
		if(xUpperMargin==null) {
			xUpperMargin=X_UPPER_MARGIN;
		}
		if(yLowerMargin==null) {
			yLowerMargin=Y_LOWER_MARGIN;
		}
		if(yUpperMargin==null) {
			yUpperMargin=Y_UPPER_MARGIN;
		}

		//2.构建[气泡统计图]所需要的指定结构的数据
		DefaultXYZDataset defaultxyzdataset = new DefaultXYZDataset();

		//dataArraysList有多少个元素,表示当前[气泡统计图]中有多少个气泡组
		for (int i = 0; i < dataArraysList.size(); i++) {
			//每个dataArrays,表示一个气泡组,且该Map集合中只有一组key-value键值对:Map集合的key值是当前气泡组的名称;Map集合value值是当前气泡组的全部气泡的三项关键数据集合
			Map<String, double[][]> dataArrays=dataArraysList.get(i);

			//获取当前气泡组的名称
			String oneBubbleChartName=null;
			Set<String> keys=dataArrays.keySet();
			if(keys!=null&&keys.size()>0) {
				for (String key : keys) {
					//只会有唯一一个key值,所以这里直接赋值即可
					oneBubbleChartName=key;
				}
			}else {
				System.err.println("当前的气泡组的名称为空,当前的气泡组数据无法添加到JFreeChart对象中..");
				continue;
			}

			/*
			 * 获取当前气泡组的全部气泡的三项关键数据集合——
			 * 1）dataArray数组的第一级只会有三个元素：
			 * a1：第一个元素:一组球中每个球的球心的y坐标
			 * a2：第二个元素：一组球中每个球的球心的x坐标
			 * a3：第三个元素：一组球中每个球的球心的半径
			 * 2）dataArray数组的第二级：一组球的每个球的某项的具体值
			 *
			 * eg：
			 * //一组球中每个球的球心的y坐标
				double ad[] = { 30 , 40 , 50 , 60 , 70 , 80 };
				//一组球中每个球的球心的x坐标
				double ad1[] = { 10 , 20 , 30 , 40 , 50 , 60 };
				//一组球中每个球的球心的半径
				double ad2[] = { 14 , 5 , 10 , 8 , 9 , 6 };
				double ad3[][] = { ad , ad1 , ad2 };
			 */
			double[][] dataArray=null;
			Collection<double[][]> values=dataArrays.values();

			if(values!=null&&values.size()>0) {
				for (double[][] value : values) {
					//只会有一个value值
					dataArray=value;
				}
			}

			//向总数据集中添加一组球的数据
			defaultxyzdataset.addSeries(oneBubbleChartName, dataArray);
		}

		//3.创建映射[气泡统计图]的JFreeChart对象
		JFreeChart bubbleChart = ChartFactory.createBubbleChart(
			headTitle,
			yTitle,
			xTitle,
			defaultxyzdataset,
			PlotOrientation.HORIZONTAL,
			true, true, false);

		//4.处理中文显示乱码以及设置一些显示样式
		XYPlot xYPlot = ( XYPlot )bubbleChart.getPlot();
		//设置透明度
		xYPlot.setForegroundAlpha( foregroundAlpha );

		//1)处理中文显示乱码
		//处理主标题乱码
		bubbleChart.getTitle().setFont(new Font(FONT,Font.BOLD,FONT_SIZE+3));
		//处理子标题乱码
		bubbleChart.getLegend().setItemFont(new Font(FONT,Font.BOLD,FONT_SIZE));
//		barChart.getLegend().setPosition(RectangleEdge.RIGHT);//右侧显示子菜单
		//设置图例项目字体
		bubbleChart.getLegend().setItemFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//设置标签字体
//		bubbleChart.setLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));

		NumberAxis xNumberaxis = ( NumberAxis )xYPlot.getRangeAxis();
		NumberAxis yNumberaxis = ( NumberAxis )xYPlot.getDomainAxis();
		//处理X轴外的乱码
		xNumberaxis.setLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//处理X轴上的乱码
		xNumberaxis.setTickLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//处理Y轴外的乱码
		yNumberaxis.setLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//处理Y轴上的乱码
		yNumberaxis.setTickLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));

		//2)设置球心与坐标轴显示范围的关系
		/*
		 * x值最小的球的球心的x值与最低显示的x值的比值
		 * 0:表示最低显示的x值就是x值最小的球的球心的x值
		 * 最大值(不固定):最低显示的x值为0
		 */
		xNumberaxis.setLowerMargin( xLowerMargin );
		/*
		 * x值最大的球的球心的x值与右侧头部显示的x值的比值
		 * 0:表示最高显示的x值上限就是x值最大的球的球心的x值
		 * 最大值:无上限,值越大则上限越大
		 */
		xNumberaxis.setUpperMargin( xUpperMargin );

		/*
		 * y值最小的球的球心的y值与最低显示的y值的比值
		 * 0:表示最低显示的y值就是y值最小的球的球心的y值
		 * 最大值(不固定):最低显示的y值为0
		 */
		yNumberaxis.setLowerMargin( yLowerMargin );
		/*
		 * y值最大的球的球心的y值与顶部最高显示的y值的比值
		 * 0:表示最高显示的y值上限就是y值最大的球的球心的y值
		 * 最大值:无上限,值越大则上限越大
		 */
		yNumberaxis.setUpperMargin( yUpperMargin );

		//*3)设置曲线显示各数据点的值
		XYItemRenderer xyitem = xYPlot.getRenderer();
		xyitem.setBaseItemLabelsVisible(true);
		xyitem.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_LEFT));
		//下面三句是对设置折线图数据标示的关键代码
		xyitem.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
		xyitem.setBaseItemLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		xYPlot.setRenderer(xyitem);
		//指定某一组球的统一颜色
//		xyitem.setSeriesPaint( 1 , Color.black );

		return bubbleChart;
	}

	//测试
	public static void main(String[] args) throws IOException {
		//1.构建2D或3D饼状统计图所需要的数据集
		Random r=new Random();
		List<Map<String, double[][]>> dataArraysList=new ArrayList<Map<String,double[][]>>();
		int bubbleChartSum=3;
		for (int i = 0; i < bubbleChartSum; i++) {
			Map<String, double[][]> dataArrays=new HashMap<String, double[][]>();

			double[][] dataArray=new double[3][];

			for (int j = 0; j < dataArray.length; j++) {
				double[] data=new double[7];
				if(j==dataArray.length-1) {
					//半径小一点
					for (int k = 0; k < data.length; k++) {
						data[k]=r.nextInt(20);
					}
				}else {
					for (int k = 0; k < data.length; k++) {
						data[k]=r.nextInt(100);
					}
				}

				dataArray[j]=data;
			}

			dataArrays.put("第"+(i+1)+"组", dataArray);

			dataArraysList.add(dataArrays);
		}

		Object fileOrFilePath="D://testBubbleChart.png";

		//2.调用工具方法
		//1)生成映射[2D或3D饼状统计图]的JFreeChart对象
		JFreeChart chart=createBubbleChart(dataArraysList, null, null, null, null, null, null, null, null);
		//2)将映射[2D或3D饼状统计图]的JFreeChart对象转成指定格式的图片文件
		String filePath=ChartToPictureFileUtil.chartToPictureFile(chart, null, null, fileOrFilePath);
		System.out.println(filePath);
	}


}
