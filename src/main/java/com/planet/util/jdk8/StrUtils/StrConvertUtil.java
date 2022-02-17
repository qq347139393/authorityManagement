package com.planet.util.jdk8.StrUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrConvertUtil {
    private static final char UNDERLINE = '_';
    //============驼峰与下划线的转换============
    /**
     * 驼峰格式字符串转换为下划线格式字符串
     *
     * @param str
     * @return
     */
    public static String camelToUnderline(String str) {
        if (str == null || "".equals(str.trim())) {
            return "";
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线格式字符串转换为驼峰格式字符串
     *
     * @param str
     * @return
     */
    public static String underlineToCamel(String str) {
        if (str == null || "".equals(str.trim())) {
            return "";
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(str.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线格式字符串转换为驼峰格式字符串2
     * 利用正则表达式
     *
     * @param str
     * @return
     */
    public static String underlineToCamel2(String str) {
        if (str == null || "".equals(str.trim())) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str);
        Matcher mc = Pattern.compile("_").matcher(str);
        int i = 0;
        while (mc.find()) {
            int position = mc.end() - (i++);
            sb.replace(position - 1, position + 1, sb.substring(position, position + 1).toUpperCase());
        }
        return sb.toString();
    }

    //==========将字符串指定位置转成小写的方法==========
    public static String toLowerCase(String str,int start,int end){
        StringBuilder sb=new StringBuilder(str);
        end=end>str.length()?str.length():end;
        sb.replace(start,end,sb.substring(start, end).toLowerCase());
        return sb.toString();
    }

    public static String toLowerCase(String str,int start){
        StringBuilder sb=new StringBuilder(str);
        return toLowerCase(str,start,str.length());
    }

    public static String toLowerCaseEnd(String str,int end){
        StringBuilder sb=new StringBuilder(str);
        return toLowerCase(str,0,end);
    }

    public static String toLowerCase(String str){
        return toLowerCase(str,0,str.length());
    }

    //==========将字符串指定位置转成大写的方法==========
    public static String toUpperCase(String str,int start,int end){
        StringBuilder sb=new StringBuilder(str);
        end=end>str.length()?str.length():end;
        sb.replace(start,end,sb.substring(start, end).toUpperCase());
        return sb.toString();
    }

    public static String toUpperCase(String str,int start){
        StringBuilder sb=new StringBuilder(str);
        return toUpperCase(str,start,str.length());
    }

    public static String toUpperCaseEnd(String str,int end){
        StringBuilder sb=new StringBuilder(str);
        return toUpperCase(str,0,end);
    }

    public static String toUpperCase(String str){
        return toUpperCase(str,0,str.length());
    }


    public static void main(String[] args) {
//        String abcdefg = toLowerCase("ABCDEFG", 1, 1000);
//        String abcdefg = toLowerCase("ABCDEFG", 1);
//        String abcdefg = toLowerCaseEnd("ABCDEFG", 1);
//        String abcdefg = toLowerCase("ABCDEFG");
//        System.out.println(abcdefg);
        String abcdefg = toUpperCaseEnd("abcdefg", 1);
        System.out.println(abcdefg);

//        String aaa = "app_version_fld";
//        System.out.println(underlineToCamel(aaa));
//        System.out.println(underlineToCamel2(aaa));
//        aaa = "appVersionFld";
//        System.out.println(camelToUnderline(aaa));
//        String a="1";
//        a.toLowerCase();
//        a.toUpperCase();

//        StringBuilder sb=new StringBuilder("9");
//        sb.append("Abc",0,3);
//        System.out.println(sb);

//        StringBuilder sb=new StringBuilder("9876");
//        sb.delete(0,2);
//        System.out.println(sb);//76
//        StringBuilder sb=new StringBuilder("9876");
//        sb.insert(1,"abc");
//        System.out.println(sb);
//        StringBuilder sb=new StringBuilder("9876");
//        sb.replace(1,3,"abc");
//        System.out.println(sb);




    }
}
