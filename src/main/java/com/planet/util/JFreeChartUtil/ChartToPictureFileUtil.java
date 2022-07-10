package com.planet.util.JFreeChartUtil;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 将指定的JFreeChart对象转成指定格式的图片文件的工具类
 * @author father
 *
 */
public class ChartToPictureFileUtil {
	/** 默认的生成图片的宽度 */
	public static final Integer WIDTH=640;
	/** 默认的生成图片的高度 */
	public static final Integer HEIGHT=480;
	/** 默认的生成图片文件存放的文件夹路径 */
	public static final String FILE_PATH="D://";
	/** 默认的生成图片文件的后缀名 */
	public static final String FILE_SUFFIX=".png";

	/**
	 * 将指定的JFreeChart对象转成指定格式的图片文件的工具方法
	 * @param chart 映射指定的[统计图]的JFreeChart对象
	 * @param width 生成图片的宽度
	 * @param height 生成图片的高度
	 * @param fileOrFilePath 指定生成图片的本地路径或一个文件对象
	 * @return 生成图片的本地路径
	 * @throws IOException
	 */
	public static String chartToPictureFile(JFreeChart chart,Integer width,Integer height,Object fileOrFilePath) throws IOException {
		//1.参数值验证与调整
		if(width==null||width<=0) {
			width=WIDTH;
		}
		if(height==null||height<=0) {
			height=HEIGHT;
		}
		if(fileOrFilePath==null) {
			fileOrFilePath=FILE_PATH+UUID.randomUUID().toString().replace("-", "")+FILE_SUFFIX;
		}else if(fileOrFilePath instanceof String) {
			if("".equals((String)fileOrFilePath)) {
				fileOrFilePath=FILE_PATH+UUID.randomUUID().toString().replace("-", "")+FILE_SUFFIX;
			}
		}else if(fileOrFilePath instanceof File) {

		}else {
			//参数为其他类型,直接改为默认的String类型并赋予默认的文件路径值
			fileOrFilePath=FILE_PATH+UUID.randomUUID().toString().replace("-", "")+FILE_SUFFIX;
		}


		//2.将指定的映射[统计图]的JFreeChart对象转成对应格式的图片文件
		File chartFile=null;
		if(fileOrFilePath instanceof File) {
			chartFile=(File)fileOrFilePath;
		}else if(fileOrFilePath instanceof String) {
			chartFile = new File((String)fileOrFilePath);
		}else {
			System.err.println("给定的文件对象或文件地址的类型错误,指定的[统计图]图片文件生成失败..");
			return null;
		}

		if(chartFile.isDirectory()) {
			System.err.println("给定的文件对象或文件地址对应的是一个文件夹,指定的[统计图]图片文件生成失败..");
			return null;
		}

		//获取文件的路径值
		String chartFilePath=chartFile.getAbsolutePath();

		String fileSuffix=chartFilePath.split("\\.")[1];
		if("jpg".equals(fileSuffix)||"jpeg".equals(fileSuffix)||"JPG".equals(fileSuffix)||"JPEG".equals(fileSuffix)) {
			ChartUtilities.saveChartAsJPEG( chartFile , chart , width , height );
		}else if("png".equals(fileSuffix)||"PNG".equals(fileSuffix)) {
			ChartUtilities.saveChartAsPNG(chartFile, chart, width, height);
		}else {
			System.err.println("文件后缀名错误,指定的[统计图]图片文件生成失败..");
			return null;
		}
		System.out.println("指定的[统计图]图片文件生成成功..");

		return chartFilePath;
	}


}
