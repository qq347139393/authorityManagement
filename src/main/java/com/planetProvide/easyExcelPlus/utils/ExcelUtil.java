package com.planetProvide.easyExcelPlus.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 获取\生成和导出标准格式的Excel文件的工具类
 * @author Administrator
 *
 */
public class ExcelUtil {

	/**
	 * 生成标准格式的Excel文件的对象(可在单元格里生成图片)
	 * @param sheetName 窗口名
	 * @param headerParams 表头标题(第一行)
	 * @param bodyHeadersMap 表体的首行元数据行(第二行)[每个Map中有且只有一个Entry元素,元素的key存的是对应表体内容的每一行的属性名;元素value存的是对应的显示名称]
	 * @param bodyRows 表体的内容数据(第三行开始)[每个Map对象代表对应的一个实体类的对象;Map对象中的每个Entry元素,对应每个实体类对象的属性键值对:key存的是属性名(属性名与bodyHeadersMap的Map的元素中的key是相同的);value存的是属性值]
	 * @param footerParams 表尾的总结栏(留出最后一行来作为表尾的总结栏,并且上面要空出一行)
	 * @param hssfw 映射Excel文件的Java对象
	 * @return
	 */
	public static HSSFWorkbook createStandardExcel(String sheetName,List<String> headerParams,List<Map<String, String>> bodyHeadersMap,
		List<Map<String, Object>> bodyRows,List<String> footerParams,HSSFWorkbook hssfw){
		int rowSum=0;//记录当前工作表的总行数的变量
		//1.创建一个Excel文件对象(工作簿)
		if(hssfw==null){
			hssfw=new HSSFWorkbook();
		}

		//2.创建一个指定名称的工作表:左下角的工作区的名称
		if(sheetName==null||"".equals(sheetName)){//如果不为当前excel文件的工作表设置名称,则按缺省名称来处理
			sheetName="sheet1";
		}
		HSSFSheet sheet=hssfw.createSheet(sheetName);

		//3.创建一行的表头(目前我们的标准的模板暂时设定为第一行是表头)
		if(headerParams!=null&&headerParams.size()>0){//表头有内容,才执行创建[表头]的操作
			HSSFRow headerParamsRow0=sheet.createRow(rowSum);
//			//创建对应当前Excel文件对象的样式
//			HSSFCellStyle style=hssfw.createCellStyle();
//			//设置样式为[居中显示]
//			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			HSSFCell headerParamsCell0=null;
			for (int i = 0; i < headerParams.size(); i++) {
				headerParamsCell0=headerParamsRow0.createCell(i);
				headerParamsCell0.setCellValue(headerParams.get(i));
			}
			rowSum++;//总行数增1
		}

		//4.创建第二行的表体中的首行属性项标题(目前我们的标准的模板暂时设定为第二行是表体中的首行标题)
		if(bodyHeadersMap!=null&&bodyHeadersMap.size()>0){//表体的首行属性项标题有内容,才执行创建[表体的首行属性项标题]的操作
			HSSFRow bodyHeadersRow1=sheet.createRow(rowSum);
			HSSFCell bodyHeadersCell1=null;
			for (int i = 0; i < bodyHeadersMap.size(); i++) {
				bodyHeadersCell1=bodyHeadersRow1.createCell(i);
				Set<String> keys=bodyHeadersMap.get(i).keySet();
				String key="";
				for (String k : keys) {//必然只有一个元素
					key=k;
				}
				bodyHeadersCell1.setCellValue(bodyHeadersMap.get(i).get(key));//将value值赋给当前单元格上
			}
			rowSum++;//总行数增1
		}

		//5.创建第三行至第n行的表体中的多行内容(目前我们的标准的模板暂时设定为第三行至第n行)
		if(bodyRows!=null&&bodyRows.size()>0){//表体的内容项中有内容,才执行创建[表体的内容项]的操作
			//List<String> bodyRowsKeys=(List<String>) bodyRows.get(0).keySet();//顺序?
			//List<String> bodyRowsKeys=new ArrayList<String>(bodyRows.get(0).keySet());//将set集合转成list集合
			//创建有序集合,后面用于按指定的顺序逐一显示当前记录的每个属性值(属性的顺序是指定好的)
			List<String> bodyRowsKeys=new ArrayList<String>();
			for (int i = 0; i < bodyHeadersMap.size(); i++) {
				Set<String> keys=bodyHeadersMap.get(i).keySet();
				String key="";
				for (String k : keys) {//必然只有一个元素
					key=k;
				}
				bodyRowsKeys.add(key);
			}
			//画图的顶级管理器:一个Excel页中(HSSFSheet)只能获取一个HSSFPatriarch对象(一定要注意这点)
			HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

			//bodyRows有多少个元素,就代表有应对数量的对象..那么就会在当前的Excel表中生成对应条数的记录
			for (int i = 0; i < bodyRows.size(); i++) {
				//创建新的一行
				HSSFRow bodyRow=sheet.createRow(rowSum);
				//遍历每条记录的每个属性(这里每条记录的属性个数和格式都一致)
				for (int j = 0; j < bodyRows.get(i).size(); j++) {
					//获取当前记录的当前属性的属性值
					Object value=bodyRows.get(i).get(bodyRowsKeys.get(j));
					if(value==null){
						//如果value为null,直接赋空值
						//这里编译器有bug,需要这样写才能通过编译(如果换用其他的编译器可能不需要这样写)
						bodyRow.createCell(j).setCellValue(value==null?null:"");
					}else if(value instanceof String){
						//如果value可以转成字符串类型,按常规的(表单)值处理
						bodyRow.createCell(j).setCellValue((String)value);
					}else if(value instanceof File){
						//如果value可以转成文件类型,按(图片)文件处理
						File fileValue=(File)value;
						//判断指定的(图片)文件是否存在:如果不存在则跳过;如果存在,则在excel表的对应表格中写入指定(图片)文件
						if(fileValue.exists()) {
							if(fileValue.isDirectory()){
								//如果当前文件是一个文件夹,则将当前单元格的值赋予空值
								System.err.println("当前文件是一个文件夹,,该属性录入失败:按空值处理..");
								//这里编译器有bug,需要这样写才能通过编译(如果换用其他的编译器可能不需要这样写)
								bodyRow.createCell(j).setCellValue("".equals("")?null:"");
							}else if(fileValue.isFile()){
								//如果当前文件是一个文件,则进行下一步操作

								FileOutputStream fileOut = null;
								BufferedImage bufferImg = null;
								ByteArrayOutputStream byteArrayOut=null;
								try {
									//获取当前(图片)文件的规范的绝对路径
									String canonicalPath=fileValue.getCanonicalPath();

									//获取当前(图片)文件的后缀名
									String[] strs=canonicalPath.split("\\.");
									//获取最后一个字符串,就是文件的后缀名
									String fileSuffix=strs[strs.length-1];

									if(fileSuffix.equalsIgnoreCase("png")){
										fileSuffix="png";
									}else if(fileSuffix.equalsIgnoreCase("jpg")||fileSuffix.equalsIgnoreCase("jpge")){
										fileSuffix="jpg";
									}else{

										//如果value是未知类型,按异常错误处理
										System.err.println("获取当前记录的当前属性的属性值为未知类型,该属性录入失败:按空值处理..");
										//这里编译器有bug,需要这样写才能通过编译(如果换用其他的编译器可能不需要这样写)
										bodyRow.createCell(j).setCellValue("".equals("")?null:"");
										continue;
									}

									byteArrayOut = new ByteArrayOutputStream();

									bufferImg = ImageIO.read(fileValue);
									ImageIO.write(bufferImg, fileSuffix, byteArrayOut);

									/*
									 * anchor主要用于设置图片的相关位置属性:可以通过构造器来进行具体的设置
									 * 1)第一个参数:图片左上起点在指定单元格中的横坐标位置:0~1023(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度)
									 * 2)第二个参数:图片左上起点在指定单元格中的纵坐标位置:0~255(255是一个单元格的高度,表示刚好占满单元格的高度)
									 * 3)第三个参数:图片右下终点在指定单元格中的横坐标位置:0~1023(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度)
									 * 4)第四个参数:图片右下终点在指定单元格中的纵坐标位置:0~255(255是一个单元格的高度,表示刚好占满单元格的高度)
									 * 5)第五个参数:图片的起点所在的指定单元格的横向序号(从0开始计)
									 * 6)第六个参数:图片的起点所在的指定单元格的纵向序号(从0开始计)
									 * 7)第七个参数:图片的终点所在的指定单元格的横向序号(从0开始计)
									 * 8)第八个参数:图片的终点所在的指定单元格的纵向序号(从0开始计)
									 */
									HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 1023, 255,(short) j, rowSum, (short) j, rowSum);
									//设置在Excel中生成的图片随单元格移动但不调整大小
									anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);

									if(fileSuffix.equals("png")){
										//如果当前图片文件的后缀是png
										patriarch.createPicture(anchor, hssfw.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_PNG));
									}else if(fileSuffix.equals("jpg")){
										//如果当前图片文件的后缀是jpg
										patriarch.createPicture(anchor, hssfw.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
									}else{
										//如果当前图片文件的后缀既不是png也不是jpg,则属性录入失败
										System.err.println("当前图片文件的后缀既不是png也不是jpg,该属性录入失败:按空值处理..");
										//这里编译器有bug,需要这样写才能通过编译(如果换用其他的编译器可能不需要这样写)
										bodyRow.createCell(j).setCellValue("".equals("")?null:"");
									}
								} catch (IOException e) {
									System.err.println("(图片)文件读取失败..");
									e.printStackTrace();
									//这里编译器有bug,需要这样写才能通过编译(如果换用其他的编译器可能不需要这样写)
									bodyRow.createCell(j).setCellValue("".equals("")?null:"");
									//继续下一个属性的遍历处理
									continue;
								}finally {
									try {
										if(byteArrayOut!=null){
											byteArrayOut.close();
										}
										if(fileOut!=null) {
											fileOut.close();
										}
									} catch (IOException e) {
										System.err.println("文件工具类关闭失败..");
										e.printStackTrace();
									}
								}
							}else{
								//如果当前图片文件的后缀不是[png]、[jpg]、[jpge],则将当前单元格的值赋予空值
								System.err.println("当前图片文件的后缀不是[png]、[jpg]、[jpge],该属性录入失败:按空值处理..");
								//这里编译器有bug,需要这样写才能通过编译(如果换用其他的编译器可能不需要这样写)
								bodyRow.createCell(j).setCellValue("".equals("")?null:"");
							}
						}else{
							//如果当前(图片)文件不存在,则将当前单元格的值赋予空值
							System.err.println("当前(图片)文件不存在,该属性录入失败:按空值处理..");
							//这里编译器有bug,需要这样写才能通过编译(如果换用其他的编译器可能不需要这样写)
							bodyRow.createCell(j).setCellValue("".equals("")?null:"");
						}
					}else{
						//可能是Double、Integer等类型
						try{
							//尝试按字符串处理
							bodyRow.createCell(j).setCellValue(value.toString());
						}catch(Exception e){
							//如果转换字符串失败
							System.err.println("属性值转换字符串失败,该属性录入失败:按空值处理..");
							//这里编译器有bug,需要这样写才能通过编译(如果换用其他的编译器可能不需要这样写)
							bodyRow.createCell(j).setCellValue("".equals("")?null:"");
						}
					}
				}
				rowSum++;//总行数增1
			}
		}

		//6.创建最后一行的表尾的内容(目前我们的标准的模板暂时设定为最后一行为表尾的总计项,并且前面要空出一行)
		if(footerParams!=null&&footerParams.size()>0){//表尾中有内容时,才执行创建[表尾]的操作
			HSSFRow footerParamsRow=sheet.createRow(rowSum+1);//前面要空出一行,所以要rowSum+1
			HSSFCell footerParamsCell=null;
			for (int i = 0; i < footerParams.size(); i++) {
				footerParamsCell=footerParamsRow.createCell(i);
				footerParamsCell.setCellValue(footerParams.get(i));
			}
			rowSum++;//总行数增1
		}

		System.err.println("create "+sheetName+" Excel file success!");
		return hssfw;//返回生成的Excel文件
	}

	/**
	 * 导出指定的Excel文件
	 * @param excelFileName 导出的Excel文件的文件名
	 * @param hssfw 需要导出的Excel文件的对象
	 * @param res 当前请求对应的响应对象
	 */
	public static void exportExcel(String excelFileName,HSSFWorkbook hssfw,HttpServletResponse res){
		try {
            try {
            	excelFileName = new String(excelFileName.getBytes(),"ISO8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            res.setContentType("application/octet-stream;charset=ISO8859-1");
            res.setCharacterEncoding("UTF-8");
            res.setHeader("Content-Disposition", "attachment;filename="+ excelFileName);
            res.addHeader("Pargam", "no-cache");
            res.addHeader("Cache-Control", "no-cache");

            OutputStream out=res.getOutputStream();//获取当前响应对象的输出流
			hssfw.write(out);//将当前的Eccel文件放入当前相应对象的输出流对象中
			out.flush();//将当前输出流中存放的内容输出到指定的位置上
			out.close();//关闭当前输出流对象
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}

}
