package com.planetProvide.easyExcelPlus.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 向指定的Excel文件中添加图片的工具类
 * @author father
 *
 */
public class ExcelAppendPictureUtil {
	/** 默认的页签名 */
	public static final String SHEET_NAME="sheet1";
	/** 默认的图片追加方向设定 */
	public static final Integer CHOOSE=0;
	/** 默认的设置:图片随单元格移动但不调整大小 */
	public static final ClientAnchor.AnchorType ANCHOR_TYPE=ClientAnchor.AnchorType.MOVE_DONT_RESIZE;
	/** 默认的图片左上起点在指定单元格中的横坐标位置的设置:0~1023(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度) */
	public static final Integer DX1=0;
	/** 默认的图片左上起点在指定单元格中的纵坐标位置的设置:0~255(255是一个单元格的高度,表示刚好占满单元格的高度) */
	public static final Integer DY1=0;
	/** 默认的图片右下终点在指定单元格中的横坐标位置的设置:0~1023(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度) */
	public static final Integer DX2=1023;
	/** 默认的图片右下终点在指定单元格中的纵坐标位置的设置:0~255(255是一个单元格的高度,表示刚好占满单元格的高度) */
	public static final Integer DY2=255;
	/** 默认的图片的起点所在的指定单元格的横向序号的设置(从0开始计) */
	public static final Short COL1=0;
	/** 默认的图片的起点所在的指定单元格的纵向序号的设置(从0开始计) */
	public static final Integer ROW1=0;
	/** 默认的图片的终点所在的指定单元格的横向序号的设置(从0开始计) */
	public static final Short COL2=0;
	/** 默认的图片的终点所在的指定单元格的纵向序号的设置(从0开始计) */
	public static final Integer ROW2=0;

	/** 默认的后一张图片与前一张图片的横坐标的相距距离 */
	public static final Integer COL=1;
	/** 默认的后一张图片与前一张图片的纵坐标的相距距离 */
	public static final Integer ROW=1;
	/** 默认的后一张图片与前一张图片的横坐标相距方式:0重叠;1跨过 */
	public static final Integer COL_CHOOSE=0;
	/** 默认的后一张图片与前一张图片的纵坐标的相距方式:0重叠;1跨过 */
	public static final Integer ROW_CHOOSE=0;


	/**
	 * 向指定的Excel文件的页签中插入图片的工具方法
	 * @param wb 映射Excel文件的HSSFWorkbook对象
	 * @param sheetName 页签名
	 * @param pictureFile 图片文件对象或字符串路径
	 * @param choose 图片插入方式选择（0：直接插入；1：下方插入；2上方插入；3右侧插入；4：左侧插入）
	 * @param anchorType 图片与单元格的状态（图片是否随单元格的大小改变而改变）
	 * @param dx1 图片左上起点在指定单元格中的横坐标位置:0~1023(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度)
	 * @param dy1 图片左上起点在指定单元格中的纵坐标位置:0~255(255是一个单元格的高度,表示刚好占满单元格的高度)
	 * @param dx2 图片右下终点在指定单元格中的横坐标位置:0~1023(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度)
	 * @param dy2 图片右下终点在指定单元格中的纵坐标位置:0~255(255是一个单元格的高度,表示刚好占满单元格的高度)
	 * @param col1 图片的起点所在的指定单元格的横向序号(从0开始计)
	 * @param row1 图片的起点所在的指定单元格的纵向序号(从0开始计)
	 * @param col2 图片的终点所在的指定单元格的横向序号(从0开始计)
	 * @param row2 图片的终点所在的指定单元格的纵向序号(从0开始计)
	 * @return
	 */
	public static HSSFWorkbook excelAppendPictureFile(HSSFWorkbook wb, String sheetName, Object pictureFile, Integer choose,
													  ClientAnchor.AnchorType anchorType, Integer dx1, Integer dy1, Integer dx2, Integer dy2, Short col1,
													  Integer row1, Short col2, Integer row2){
		//1.参数合法性校验
		if(wb==null){
			//如果Excel文件对象为空,则创建一个新的Excel文件对象
			wb=new HSSFWorkbook();

		}
		if(sheetName==null||"".equals(sheetName)){
			//如果不为当前excel文件的工作表设置名称,则按缺省名称来处理
			sheetName=SHEET_NAME;
		}
		//获取当前Excel文件中指定名称的那一页
		HSSFSheet sheet=wb.getSheet(sheetName);

		if(choose==null||choose<0||choose>8){
			//图片插入的方向选择为空或超过了范围,按摩认值处理
			choose=CHOOSE;
		}
		if(anchorType==null){
			anchorType=ANCHOR_TYPE;
		}
		if(dx1==null){
			dx1=DX1;
		}
		if(dy1==null){
			dy1=DY1;
		}
		if(dx2==null){
			dx2=DX2;
		}
		if(dy2==null){
			dy2=DY2;
		}
		if(col1==null){
			col1=COL1;
		}
		if(row1==null){
			row1=ROW1;
		}
		if(col2==null){
			col2=COL2;
		}
		if(row2==null){
			row2=ROW2;
		}

		BufferedImage bufferImg = null;
		String fileSuffix=null;
		if(pictureFile instanceof String){
			File file=new File((String)pictureFile);
			if(!file.exists()){
				System.err.println("给定的图片路径错误,向指定的Excel文件中插入图片文件失败..");
				return wb;
			}

			try {
				String canonicalPath=file.getCanonicalPath();
				//获取当前(图片)文件的后缀名
				String[] strs=canonicalPath.split("\\.");
				//获取最后一个字符串,就是文件的后缀名
				fileSuffix=strs[strs.length-1];

				if(fileSuffix.equalsIgnoreCase("png")){
					fileSuffix="png";
				}else if(fileSuffix.equalsIgnoreCase("jpg")||fileSuffix.equalsIgnoreCase("jpge")){
					fileSuffix="jpg";
				}else{
					System.err.println("图片文件的后缀名不是png或jpg(jpge),向指定的Excel文件中插入图片文件失败..");
					return wb;
				}

				bufferImg=ImageIO.read(file);
			} catch (IOException e) {
				System.err.println("读取指定的图片文件失败,向指定的Excel文件中插入图片文件失败..");
				e.printStackTrace();
				return wb;
			}
		}else if(pictureFile instanceof File){
			File file=(File)pictureFile;
			if(!file.exists()){
				System.err.println("给定的图片路径错误,向指定的Excel文件中插入图片文件失败..");
				return wb;
			}
			try {
				String canonicalPath=file.getCanonicalPath();
				//获取当前(图片)文件的后缀名
				String[] strs=canonicalPath.split("\\.");
				//获取最后一个字符串,就是文件的后缀名
				fileSuffix=strs[strs.length-1];

				if(fileSuffix.equalsIgnoreCase("png")){
					fileSuffix="png";
				}else if(fileSuffix.equalsIgnoreCase("jpg")||fileSuffix.equalsIgnoreCase("jpge")){
					fileSuffix="jpg";
				}else{
					System.err.println("图片文件的后缀名不是png或jpg(jpge),向指定的Excel文件中插入图片文件失败..");
					return wb;
				}

				bufferImg=ImageIO.read(file);
			} catch (IOException e) {
				System.err.println("读取指定的图片文件失败,向指定的Excel文件中插入图片文件失败..");
				e.printStackTrace();
				return wb;
			}
		}else{
			System.err.println("给定的图片文件的类型错误,向指定的Excel文件中插入图片文件失败..");
			return wb;
		}

		//2.读取图片文件中的数据
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferImg, "png", byteArrayOut);
		} catch (IOException e) {
			System.err.println("读取指定的图片文件的过程中出现异常,向指定的Excel文件中插入图片文件失败..");
			e.printStackTrace();
			return wb;
		}

		//3.选择插入图片的位置

		if(choose==0){
			//直接在指定位置插入

		}else if(choose==1){
			//在文档内容的下方插入图片
			//获取最后一行的下标
			Integer lastRowNum=sheet.getLastRowNum()+1;
			System.out.println(lastRowNum);

			//放入的图片的首行和尾行都向下移动相应的下标数
			row1+=lastRowNum;
			row2+=lastRowNum;

		}else if(choose==2){
			//在文档内容的上方插入图片

		}else if(choose==3){
			//在文档内容的右侧插入图片
			//获取sheet的首行
			HSSFRow headerRow=sheet.getRow(0);
			Short lastColNum=headerRow.getLastCellNum();

			//放入的图片的首列和尾列向右移动相应的下标数
			col1=(short)(col1+lastColNum);
			col2=(short)(col2+lastColNum);

		}else if(choose==4){
			//在文档内容的左侧插入图片

		}else if(choose==5){
			//在文档内容的右上插入图片

		}else if(choose==6){
			//在文档内容的左上插入图片

		}else if(choose==7){
			//在文档内容的右下插入图片

		}else if(choose==8){
			//在文档内容的左下插入图片

		}

		//anchor主要用于设置图片的属性(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度;255是一个单元格的高度,表示刚好占满单元格的高度)
		HSSFClientAnchor anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, (short)col1, row1, (short)col2, row2);
//		HSSFClientAnchor anchor2 = new HSSFClientAnchor(0, 0, 1023, 255,(short) 10, 12,(short) 13, 14);
		// 设置图片随单元格的变化情况
		anchor.setAnchorType(anchorType);

		// 画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
		HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

		//向指定的Excel文档的指定页签上的指定位置插入图片
		byte[] by=byteArrayOut.toByteArray();
		if(fileSuffix.equals("png")){
			patriarch.createPicture(anchor, wb.addPicture(by, HSSFWorkbook.PICTURE_TYPE_PNG));
//			patriarch.createPicture(anchor2, wb.addPicture(by, HSSFWorkbook.PICTURE_TYPE_PNG));
		}else if(fileSuffix.equals("jpg")){
			patriarch.createPicture(anchor, wb.addPicture(by, HSSFWorkbook.PICTURE_TYPE_JPEG));
		}

		return wb;
	}

	/**
	 * 向指定的Excel文件的页签中按指定排序一次性插入多个图片的工具方法
	 * @param wb 映射Excel文件的HSSFWorkbook对象
	 * @param sheetName 页签名
	 * @param pictureFiles 若干个图片文件对象或字符串路径的集合
	 * @param choose 图片插入方式选择（0：直接插入；1：下方插入；2上方插入；3右侧插入；4：左侧插入）
	 * @param anchorType 图片与单元格的状态（图片是否随单元格的大小改变而改变）
	 * @param dx1 图片左上起点在指定单元格中的横坐标位置:0~1023(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度)
	 * @param dy1 图片左上起点在指定单元格中的纵坐标位置:0~255(255是一个单元格的高度,表示刚好占满单元格的高度)
	 * @param dx2 图片右下终点在指定单元格中的横坐标位置:0~1023(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度)
	 * @param dy2 图片右下终点在指定单元格中的纵坐标位置:0~255(255是一个单元格的高度,表示刚好占满单元格的高度)
	 * @param col1 第一张图片的起点所在的指定单元格的横向序号(从0开始计)
	 * @param row1 第一张图片的起点所在的指定单元格的纵向序号(从0开始计)
	 * @param col2 第一张图片的终点所在的指定单元格的横向序号(从0开始计)
	 * @param row2 第一张图片的终点所在的指定单元格的纵向序号(从0开始计)
	 * @param col 后一张图片与前一张图片的横坐标的相距距离
	 * @param row 后一张图片与前一张图片的纵坐标的相距距离
	 * @param colChoose 后一张图片与前一张图片的横坐标相距方式:0重叠;1跨过
	 * @param rowChoose 后一张图片与前一张图片的纵坐标的相距方式:0重叠;1跨过
	 * @return
	 */
	public static HSSFWorkbook excelAppendPictureFiles(HSSFWorkbook wb, String sheetName, List<Object> pictureFiles, Integer choose, ClientAnchor.AnchorType anchorType,
													   Integer dx1, Integer dy1, Integer dx2, Integer dy2, Short col1, Integer row1, Short col2, Integer row2, Integer col, Integer row, Integer colChoose, Integer rowChoose){
		//1.参数合法性校验
		if(wb==null){
			//如果Excel文件对象为空,则创建一个新的Excel文件对象
			wb=new HSSFWorkbook();

		}
		if(sheetName==null||"".equals(sheetName)){
			//如果不为当前excel文件的工作表设置名称,则按缺省名称来处理
			sheetName=SHEET_NAME;
		}
		//获取当前Excel文件中指定名称的那一页
		HSSFSheet sheet=wb.getSheet(sheetName);

		if(choose==null||choose<0||choose>8){
			//图片插入的方向选择为空或超过了范围,按默认值处理
			choose=CHOOSE;
		}
		if(anchorType==null){
			anchorType=ANCHOR_TYPE;
		}
		if(dx1==null){
			dx1=DX1;
		}
		if(dy1==null){
			dy1=DY1;
		}
		if(dx2==null){
			dx2=DX2;
		}
		if(dy2==null){
			dy2=DY2;
		}
		if(col1==null){
			col1=COL1;
		}
		if(row1==null){
			row1=ROW1;
		}
		if(col2==null){
			col2=COL2;
		}
		if(row2==null){
			row2=ROW2;
		}

		if(col==null){
			col=COL;
		}
		if(row==null){
			row=ROW;
		}
		if(colChoose==null){
			colChoose=COL_CHOOSE;
		}
		if(rowChoose==null){
			rowChoose=ROW_CHOOSE;
		}

		if(pictureFiles==null||pictureFiles.size()==0){
			System.err.println("图片集合为空,向指定的Excel文件中插入当前的图片文件失败..");
			return wb;
		}


		// 画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
		HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

		//2.选择插入图片的位置
		if(choose==0){
			//直接在指定位置插入

		}else if(choose==1){
			//在文档内容的下方插入图片
			//获取最后一行的下标
			Integer lastRowNum=sheet.getLastRowNum()+1;
			System.out.println("lastRowNum="+lastRowNum);

			//放入的图片的首行和尾行都向下移动相应的下标数
			row1+=lastRowNum;
			row2+=lastRowNum;

		}else if(choose==2){
			//在文档内容的上方插入图片

		}else if(choose==3){
			//在文档内容的右侧插入图片
			//获取sheet的首行
			HSSFRow headerRow=sheet.getRow(0);
			Short lastColNum=headerRow.getLastCellNum();

			//放入的图片的首列和尾列向右移动相应的下标数
			col1=(short)(col1+lastColNum);
			col2=(short)(col2+lastColNum);

		}else if(choose==4){
			//在文档内容的左侧插入图片

		}else if(choose==5){
			//在文档内容的右上插入图片

		}else if(choose==6){
			//在文档内容的左上插入图片

		}else if(choose==7){
			//在文档内容的右下插入图片

		}else if(choose==8){
			//在文档内容的左下插入图片

		}

		//3.遍历图片集合,从而将传入的每张图片逐一写入到指定的Excel文件中
		for (int i = 0; i < pictureFiles.size(); i++) {
			Object pictureFile=pictureFiles.get(i);

			BufferedImage bufferImg = null;
			String fileSuffix=null;
			if(pictureFile instanceof String){
				File file=new File((String)pictureFile);
				if(!file.exists()){
					System.err.println("给定的图片路径错误,向指定的Excel文件中插入当前的图片文件失败..");
//					return wb;
					continue;
				}

				try {
					String canonicalPath=file.getCanonicalPath();
					//获取当前(图片)文件的后缀名
					String[] strs=canonicalPath.split("\\.");
					//获取最后一个字符串,就是文件的后缀名
					fileSuffix=strs[strs.length-1];

					if(fileSuffix.equalsIgnoreCase("png")){
						fileSuffix="png";
					}else if(fileSuffix.equalsIgnoreCase("jpg")||fileSuffix.equalsIgnoreCase("jpge")){
						fileSuffix="jpg";
					}else{
						System.err.println("图片文件的后缀名不是png或jpg(jpge),向指定的Excel文件中插入当前的图片文件失败..");
//						return wb;
						continue;
					}

					bufferImg=ImageIO.read(file);
				} catch (IOException e) {
					System.err.println("读取指定的图片文件失败,向指定的Excel文件中插入当前的图片文件失败..");
					e.printStackTrace();
//					return wb;
					continue;
				}
			}else if(pictureFile instanceof File){
				File file=(File)pictureFile;
				if(!file.exists()){
					System.err.println("给定的图片路径错误,向指定的Excel文件中插入当前的图片文件失败..");
//					return wb;
					continue;
				}
				try {
					String canonicalPath=file.getCanonicalPath();
					//获取当前(图片)文件的后缀名
					String[] strs=canonicalPath.split("\\.");
					//获取最后一个字符串,就是文件的后缀名
					fileSuffix=strs[strs.length-1];

					if(fileSuffix.equalsIgnoreCase("png")){
						fileSuffix="png";
					}else if(fileSuffix.equalsIgnoreCase("jpg")||fileSuffix.equalsIgnoreCase("jpge")){
						fileSuffix="jpg";
					}else{
						System.err.println("图片文件的后缀名不是png或jpg(jpge),向指定的Excel文件中插入当前的图片文件失败..");
//						return wb;
						continue;
					}

					bufferImg=ImageIO.read(file);
				} catch (IOException e) {
					System.err.println("读取指定的图片文件失败,向指定的Excel文件中插入当前的图片文件失败..");
					e.printStackTrace();
//					return wb;
					continue;
				}
			}else{
				System.err.println("给定的图片文件的类型错误,向指定的Excel文件中插入当前的图片文件失败..");
//				return wb;
				continue;
			}

			//4.读取图片文件中的数据
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			try {
				ImageIO.write(bufferImg, "png", byteArrayOut);

				//当前图片与上一张图片的横纵坐标的相距计算
				Short col1Sum=0;
				Short col2Sum=0;
				if(colChoose==0){
					//横向按重叠处理
					col1Sum=(short)(col1+col*i);
					col2Sum=(short)(col2+col*i);
				}else{
					//横向其余的值,都按1(跨过)处理
					Integer colDif=col2-col1+1;
					col1Sum=(short)(col1+(col+colDif)*i);
					col2Sum=(short)(col2+(col+colDif)*i);
				}
				Integer row1Sum=0;
				Integer row2Sum=0;
				if(rowChoose==0){
					//纵向按重叠处理
					row1Sum=row1+row*i;
					row2Sum=row2+row*i;
				}else{
					//纵向其余的值,都按1(跨过)处理
					Integer rowDif=row2-row1+1;
					row1Sum=row1+(row+rowDif)*i;
					row2Sum=row2+(row+rowDif)*i;
				}

				//anchor主要用于设置图片的属性(1023是一个单元格宽度的最大值,表示刚好占满单元格的宽度;255是一个单元格的高度,表示刚好占满单元格的高度)
				HSSFClientAnchor anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, (short)col1Sum, row1Sum, (short)col2Sum, row2Sum);
//					HSSFClientAnchor anchor2 = new HSSFClientAnchor(0, 0, 1023, 255,(short) 10, 12,(short) 13, 14);
				// 设置图片随单元格的变化情况
				anchor.setAnchorType(anchorType);

				//向指定的Excel文档的指定页签上的指定位置插入图片
				byte[] by=byteArrayOut.toByteArray();
				if(fileSuffix.equals("png")){
					patriarch.createPicture(anchor, wb.addPicture(by, HSSFWorkbook.PICTURE_TYPE_PNG));
//						patriarch.createPicture(anchor2, wb.addPicture(by, HSSFWorkbook.PICTURE_TYPE_PNG));
				}else if(fileSuffix.equals("jpg")){
					patriarch.createPicture(anchor, wb.addPicture(by, HSSFWorkbook.PICTURE_TYPE_JPEG));
				}

			} catch (IOException e) {
				System.err.println("读取指定的图片文件的过程中出现异常,向指定的Excel文件中插入当前的图片文件失败..");
				e.printStackTrace();
//				return wb;
				continue;
			}finally {
				try {
					byteArrayOut.close();
				} catch (IOException e) {
					System.err.println("关闭文件输入流失败..");
					e.printStackTrace();
				}
			}
		}

		return wb;
	}


}
