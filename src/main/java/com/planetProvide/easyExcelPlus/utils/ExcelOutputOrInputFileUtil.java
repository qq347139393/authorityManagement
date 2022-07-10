package com.planetProvide.easyExcelPlus.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;

public class ExcelOutputOrInputFileUtil {
	/** 默认的文件的包路径 */
	public static final String FILEPATH="D:\\";

	/**
	 * 将指定的Excel文件对象生成指定位置的文件
	 * @param wb
	 * @param filepath
	 */
	public static void excelOutputFile(HSSFWorkbook wb,String filepath,Boolean coverFlag){
		if(wb==null){
			System.err.println("Excel文件对象为空,将指定的Excel文件对象生成指定位置的文件失败..");
			return;
		}

		if(coverFlag==null){
			//默认为替换
			coverFlag=true;
		}

		if(filepath==null||"".equals(filepath)){
			filepath=FILEPATH+System.currentTimeMillis()+".xls";
		}

		File file=new File(filepath);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("新建指定位置的文件失败,将指定的Excel文件对象生成指定位置的文件失败..");
				e.printStackTrace();
				return;
			}
		}else if(file.isDirectory()){
			System.err.println("指定的位置已经有了文件夹,将指定的Excel文件对象生成指定位置的文件失败..");
			return;
		}else if(file.isFile()){
			//如果指定位置已经有了文件
			if(coverFlag==false){
				System.err.println("指定的位置已经有了文件并且覆盖开关设置为否,将指定的Excel文件对象生成指定位置的文件失败..");
				return;
			}
			System.err.println("旧文件被覆盖..");
		}

		FileOutputStream fileOut=null;
		try {
			fileOut=new FileOutputStream(file);
			//将Excel文件对象生成指定文件的Excel文件
			wb.write(fileOut);

			System.out.println("将Excel文件对象生成指定文件的Excel文件完毕");
		} catch (FileNotFoundException e) {
			System.err.println("指定的文件未找到,将指定的Excel文件对象生成指定位置的文件失败..");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.err.println("将Excel文件对象生成指定文件的Excel文件的数据传输过程中出现异常,将指定的Excel文件对象生成指定位置的文件失败..");
			e.printStackTrace();
			return;
		}finally {
			if(fileOut!=null){
				try {
					fileOut.close();
				} catch (IOException e) {
					System.err.println("关闭文件输出流对象失败..");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取指定文件路径的Excel文件转成HSSFWorkbook对象
	 * @param filepath 指定的Excel文件的存放地址
	 * @return
	 */
	public static HSSFWorkbook getExcelByFilepath(String filepath){
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(filepath);
			HSSFWorkbook wb=new HSSFWorkbook(fis);

			return wb;
		} catch (FileNotFoundException e) {
			System.err.println("指定位置的文件未找到,获取指定文件路径的Excel文件转成HSSFWorkbook对象失败..");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println("发生了IO异常,获取指定文件路径的Excel文件转成HSSFWorkbook对象失败..");
			e.printStackTrace();
			return null;
		}finally {
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					System.err.println("文件输入流关闭失败..");
					e.printStackTrace();
				}
			}
		}
	}
}
