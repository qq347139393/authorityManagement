package com.planet.util.JFreeChartUtil;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

/**
 * 生成映射指定的[2D或3D饼状统计图]的JFreeChart对象的工具类
 * @author father
 *
 */
@Slf4j
public class PieChart2DOr3DUtil {
	/** 统计图的头标题的默认值 */
	public static final String HEAD_TITLE="[2D或3D饼状统计图]统计汇总";
	/** 3D效果开关的默认值:1,表示开启3D效果,生成3D饼状统计图;不为1时,表示不开启3D效果,生成2D饼状统计图 */
	public static final Integer FLAG_3D=0;
	/** 默认的字体 */
	public static final String FONT="宋体";
	/** 默认的字号 */
	public static final Integer FONT_SIZE=15;
	/** 默认的起始角度值 */
	public static final Double START_ANGLE=270.0;
	/** 默认的背景透明度值 */
	public static final Float FOREGROUND_ALPHA=0.60f;
	/** 默认的内部间隙值 */
	public static final Double INTERIOR_GAP=0.02;

	/**
	 * 生成映射指定的[2D或3D饼状统计图]的JFreeChart对象的工具方法
	 * @param dataMap 构建[2D或3D饼状统计图]所需要的数据的数据集
	 * @param flag3D 3D效果开关:1,表示开启3D效果,生成3D饼状统计图;不为1时,表示不开启3D效果,生成2D饼状统计图
	 * @param headTitle 统计图的头标题
	 * @param startAngle 起始角度
	 * @param foregroundAlpha 背景透明度
	 * @param interiorGap 内部间隙
	 * @return 映射指定的[2D或3D饼状统计图]的JFreeChart对象
	 */
	public static JFreeChart createPieChart2DOr3D(Map<String, Object> dataMap,Integer flag3D,String headTitle,Double startAngle,Float foregroundAlpha,Double interiorGap){
		//1.参数值验证与调整
		if(dataMap==null||dataMap.isEmpty()) {
			log.error("生成饼状图所需要的统计数据不能为空,生成映射[2D或3D饼状统计图]的JFreeChart对象失败..");
			return null;
		}
		if(flag3D==null) {
			flag3D=FLAG_3D;
		}
		if(headTitle==null) {
			headTitle=HEAD_TITLE;
		}
		if(startAngle==null) {
			startAngle=START_ANGLE;
		}
		if(foregroundAlpha==null) {
			foregroundAlpha=FOREGROUND_ALPHA;
		}
		if(interiorGap==null) {
			interiorGap=INTERIOR_GAP;
		}

		//2.构建[2D或3D饼状统计图]所需要的指定结构的数据
		final DefaultPieDataset dataset = new DefaultPieDataset();

		//dataMap的每个元素,表示当前[2D或3D饼状统计图]中有多少个扇形部分
		//Java8开始带的专门用于遍历Map集合key-value键值对的Lambda表达式
		dataMap.forEach((piePartName,value) -> {
			if(value==null) {
				dataset.setValue(piePartName, 0.0);
			}else if(value instanceof Double) {
				dataset.setValue(piePartName, (double)value);
			}else if(value instanceof Integer) {
				dataset.setValue(piePartName, (Integer)value);
			}else if(value instanceof Long) {
				dataset.setValue(piePartName, (Long)value);
			}else if(value instanceof String){
				dataset.setValue(piePartName, Double.valueOf((String)value));
			}else{
				log.error("给定的数据集中的Map中的value类型错误,生成映射[2D或3D饼状统计图]的JFreeChart对象失败..");
			}
		});

		//3.创建映射[2D或3D饼状统计图]的JFreeChart对象
		JFreeChart pieChart2DOr3D = null;
		//控制统计图的显示样式的对象
		PiePlot piePlot2DOr3D=null;
		if(flag3D==1) {
			//3D开关为1时,表示开启3D效果:生成3D饼状统计图
			pieChart2DOr3D = ChartFactory.createPieChart3D(headTitle ,dataset ,true ,true,false);
			piePlot2DOr3D = ( PiePlot3D ) pieChart2DOr3D.getPlot();
		}else {
			//3D开关不为1时,表示不开启3D效果:生成2D饼状统计图
			pieChart2DOr3D = ChartFactory.createPieChart(headTitle,dataset,true,true,false);
			piePlot2DOr3D=(PiePlot) pieChart2DOr3D.getPlot();;
		}

		//4.处理中文显示乱码以及设置一些显示样式
		//起始角度
		piePlot2DOr3D.setStartAngle( startAngle );
		//背景透明度
		piePlot2DOr3D.setForegroundAlpha( foregroundAlpha );
		//内部间隙
		piePlot2DOr3D.setInteriorGap( interiorGap );

		//5.处理中文显示乱码以及设置一些显示样式
		//处理主标题乱码
		pieChart2DOr3D.getTitle().setFont(new Font(FONT,Font.BOLD,FONT_SIZE+3));
		//处理子标题乱码
		pieChart2DOr3D.getLegend().setItemFont(new Font(FONT,Font.BOLD,FONT_SIZE));
//		barChart.getLegend().setPosition(RectangleEdge.RIGHT);//右侧显示子菜单
		//设置图例项目字体
		pieChart2DOr3D.getLegend().setItemFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		//设置标签字体
		piePlot2DOr3D.setLabelFont(new Font(FONT,Font.BOLD,FONT_SIZE));
		/*
		 * 设置[饼状统计图]中的每个扇形部分的显示内容
		 * {0}:指定当前扇形部分的名称
		 * {1}:指定当前扇形部分的具体数值
		 * {2}:指定当前扇形部分的百分比
		 */
		piePlot2DOr3D.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}:{1}({2})", NumberFormat.getNumberInstance(),new DecimalFormat("0.00%")));
		//++++++++++++

		return pieChart2DOr3D;
	}

	//测试
	public static void main(String[] args) throws IOException {
		//1.构建2D或3D饼状统计图所需要的数据集
		Random r=new Random();
		Map<String, Object> dataMap=new HashMap<>();
		int sum=8;
		for (int i = 0; i < sum; i++) {
			dataMap.put("第"+(i+1)+"部分", r.nextInt(200)+0.5);
		}

		Object fileOrFilePath="D://testPie2Dor3D.png";

		//2.调用工具方法
		//1)生成映射[2D或3D饼状统计图]的JFreeChart对象
		JFreeChart chart=createPieChart2DOr3D(dataMap, 12,null,null,null,null);
		//2)将映射[2D或3D饼状统计图]的JFreeChart对象转成指定格式的图片文件
		String filePath=ChartToPictureFileUtil.chartToPictureFile(chart, null, null, fileOrFilePath);
		System.out.println(filePath);
	}

}
