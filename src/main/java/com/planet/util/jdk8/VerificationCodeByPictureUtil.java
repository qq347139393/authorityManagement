package com.planet.util.jdk8;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * 生成图片验证码的工具类
 * @author Administrator
 *
 */
public class VerificationCodeByPictureUtil {

    /** 验证码出现的字符的范围 */
    private static final char[] CHARS={
            '0','1','2','3','4','5','6','7','8','9',
            'a','b','c','d','e','f','g','h','i','g','k',
            'l','m','n','o','p','q','r','s','t','u','v',
            'w','x','y','z','A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    /** 验证码出现的字符的个数 */
    private static final Integer SIZE=4;
    /** 验证码图片中的干扰线的数量 */
    private static final Integer LINES=5;
    /** 验证码图片的宽度 */
    private static final Integer WIDTH=80;
    /** 验证码图片的高度 */
    private static final Integer HEIGHT=40;
    /** 验证码图片中出现的字符的(字体)大小 */
    private static final Integer FONT_SIZE=30;

    /**
     * 生成验证码图片和验证码字符串的工具方法
     * @return
     */
    public static Object[] createVerificationCodeAndPicture(int size,int lines,int width,int height
            ,int fontSize){
        //如果小于等于0,则按默认的来
        if(size<=0){
            size=SIZE;
        }
        if(lines<=0){
            lines=LINES;
        }
        if(width<=0){
            width=WIDTH;
        }
        if(height<=0){
            height=HEIGHT;
        }
        if(fontSize<=0){
            fontSize=FONT_SIZE;
        }

        //创建可拼接的字符串对象,用于封装验证码字符串
        StringBuffer sb=new StringBuffer();
        //创建指定宽度、高度和颜色的用于生成验证码图片的（BufferedImage类型）的图片对象
        BufferedImage bufferedImage=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //获取指定的图片对象的（Graphics类）的“画笔”对象
        Graphics graphics=bufferedImage.getGraphics();
        //设置当前画笔对象的颜色为浅灰色
        graphics.setColor(Color.LIGHT_GRAY);
        //绘制指定长宽大小的（浅灰色的）矩形背景图：前两个参数，表示画笔起点的横纵坐标；后两个参数，表示画笔终点的横纵坐标
        graphics.fillRect(0, 0, width, height);
        //创建一个生成随机数的随机数生成对象
        Random random=new Random();

        //循环生成表示验证码的指定个数的字符串
        for (int i = 0; i < size; i++) {
            //生成一个含有一种随机颜色的颜色对象
            Color color=new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            //设置画笔的颜色为当前颜色对象含有的那个随机颜色
            graphics.setColor(color);
            //设置字体的大小和样式
            graphics.setFont(new Font(null, Font.BOLD+Font.ITALIC, fontSize));
            //在规定的可出现的字符范围内，随机取一个字符范围的下标
            int index=random.nextInt(CHARS.length);
            //让画笔对象按照指定的长度和宽度来绘制指定的字符范围下标的字符
            graphics.drawString(CHARS[index]+"", i*width/size, height/2);
            //将每次随机生成的字符添加到封装验证码字符串的StringBuffer类的对象中
            sb.append(CHARS[index]);
        }

        //循环生产指定条数的干扰线
        for (int i = 0; i < lines; i++) {
            //生成一种随机颜色的颜色对象
            Color color=new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            //设置画笔的颜色为当前颜色对象含有的那个随机颜色
            graphics.setColor(color);
            //让画笔对象按照指定坐标的起点和终点来绘制一条干扰线：前两个参数，表示绘制的起点的坐标；后两个参数，表示绘制的终点的坐标
            graphics.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        //创建一个含有两个元素的Object类型的数组对象：第一个元素中存的是生成的验证码字符串；第二个元素中存的是生成的验证码图片对象
        Object[] objectResult=new Object[]{sb.toString(),bufferedImage};

        //返回存生成的验证码字符串和生成的验证码图片对象的Object类型的数组对象
        return objectResult;
    }

    //测试
    public static void main(String[] args) throws IOException {
        Object[] verificationCode=VerificationCodeByPictureUtil.createVerificationCodeAndPicture(0,0,0,0,0);
        OutputStream os=new FileOutputStream("D:/testVericationCode.png");

        ImageIO.write((BufferedImage)verificationCode[1], "png", os);
        os.close();
    }


}
