package com.planetProvide.easyExcelPlus.core.baseExcelImportValid;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.planetProvide.easyExcelPlus.core.annotation.*;
import com.planetProvide.easyExcelPlus.core.constant.DefaultConstant;
import com.planetProvide.easyExcelPlus.core.entity.Msg;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Excel导入字段校验</p>
 */
@Data
public class BaseExcelImportValid<T> {

    private String setRowCode;

    private String getRowCode;

    private String setRowMsgs;

    private int unqualifiedRowCode;



    /**
     * Excel导入字段校验
     *
     */
    public T annotationValid(T t) throws IllegalAccessException {
        Field[] fields = t.getClass().getDeclaredFields();
        List<Msg> msgs =new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            Field field=fields[i];
            //设置可访问
            field.setAccessible(true);
            //获取当前属性的值
            Object fieldValue = null;
            try {
                fieldValue = field.get(t);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessException();
            }
            //如果有忽略字段的注解,直接过下一个
            boolean isExcelIgnore=field.isAnnotationPresent(ExcelIgnore.class);
            if(isExcelIgnore){//如果有,则直接下一个
                continue;
            }

            Msg msg=new Msg();//用于存错误信息的对象
            List<String> msgList=new ArrayList<>();//用于存当前属性的全部错误提示信息的集合

            //获取当前属性的字段排序和名称
            boolean isExcelProperty=field.isAnnotationPresent(ExcelProperty.class);
            if(isExcelProperty){
                int index = field.getAnnotation(ExcelProperty.class).index();
                if(index!=-1){
                    msg.setMsgOrder(index);
                }else{//如果为-1,说明按照默认迭代顺序来
                    msg.setMsgOrder(i);
                }
                String[] names=field.getAnnotation(ExcelProperty.class).value();
                //最后一个是当前字段的名称
                String name=names[names.length-1];
                if(name!=null&&!"".equals(name)){
                    msg.setMsgName(names[names.length-1]);
                }else{//如果名称为空,我们就取属性名
                    msg.setMsgName(field.getName());
                }
            }else{//没有就按照迭代顺序来:为了不乱和重叠,要么全部字段都用index属性,要么都不用index属性
                msg.setMsgOrder(i);
                //没有就取属性名
                msg.setMsgName(field.getName());
            }

            //是否包含必填校验注解
            boolean isExcelNotNull = field.isAnnotationPresent(ExcelNotNull.class);
            if (isExcelNotNull && (fieldValue==null||"".equals(fieldValue))) {//违背
//                t.setCode(1);//这个可以在最后面统一判断一次
                msgList.add(field.getAnnotation(ExcelNotNull.class).message());//违背原因
            }
            //是否包含必须为空的注解
            boolean isExcelMustNull = field.isAnnotationPresent(ExcelMustNull.class);
            if (isExcelMustNull && fieldValue!=null&&!"".equals(fieldValue)) {//违背
                msgList.add(field.getAnnotation(ExcelMustNull.class).message());//违背原因
            }
            //是否包含指定值范围
            boolean isExcelNumRange = field.isAnnotationPresent(ExcelNumRange.class);
            if (isExcelNumRange) {
                if(fieldValue==null){//违背
                    msgList.add(field.getAnnotation(ExcelNumRange.class).message());//违背原因
                }
                Double number=new Double(fieldValue.toString());
                Double max=field.getAnnotation(ExcelNumRange.class).max();
                Double min=field.getAnnotation(ExcelNumRange.class).min();
                if(number>max||number<min){//违背
                    msgList.add(field.getAnnotation(ExcelNumRange.class).message());//违背原因
                }
            }
            //是否字段必须符合指定的正则表达式
            boolean isExcelPatternReg = field.isAnnotationPresent(ExcelPatternReg.class);
            if (isExcelPatternReg) {
                if(fieldValue==null){
                    fieldValue="";//因为有可能输入空字符串也合格
                }
                String reg=field.getAnnotation(ExcelPatternReg.class).value();
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(fieldValue.toString());
                if(!matcher.matches()){//违背
                    msgList.add(field.getAnnotation(ExcelPatternReg.class).message());//违背原因
                }
            }
            //是否字段必须为合格的Email格式[a-zA-Z0-9]+@[a-zA-Z0-9]+\.[a-zA-Z0-9]+
            boolean isExcelQualifiedEmail = field.isAnnotationPresent(ExcelQualifiedEmail.class);
            if (isExcelQualifiedEmail) {
                if(fieldValue==null){//违背
                    msgList.add(field.getAnnotation(ExcelQualifiedEmail.class).message());//违背原因
                }
                String reg="[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+";
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(fieldValue.toString());
                if(!matcher.matches()){//违背
                    msgList.add(field.getAnnotation(ExcelQualifiedEmail.class).message());//违背原因
                }
            }
            //是否包含必填校验注解/^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\d{8}$/
            boolean isExcelQualifiedMobilePhone = field.isAnnotationPresent(ExcelQualifiedMobilePhone.class);
            if (isExcelQualifiedMobilePhone) {
                if(fieldValue==null){//违背
                    msgList.add(field.getAnnotation(ExcelQualifiedMobilePhone.class).message());//违背原因
                }
                String reg="/^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$/";
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(fieldValue.toString());
                if(!matcher.matches()){//违背
                    msgList.add(field.getAnnotation(ExcelQualifiedMobilePhone.class).message());//违背原因
                }
            }
            //是否字段必须为合格的url值
            boolean isExcelQualifiedUrl = field.isAnnotationPresent(ExcelQualifiedUrl.class);
            if (isExcelQualifiedUrl) {
                if(fieldValue==null){//违背
                    msgList.add(field.getAnnotation(ExcelQualifiedUrl.class).message());//违背原因
                }
                String reg="(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(fieldValue.toString());
                if(!matcher.matches()){//违背
                    msgList.add(field.getAnnotation(ExcelQualifiedUrl.class).message());//违背原因
                }
            }
            //是否字段的字符串长度必须在指定范围
            boolean isExcelStrLength = field.isAnnotationPresent(ExcelStrLength.class);
            if (isExcelStrLength) {
                int max=field.getAnnotation(ExcelStrLength.class).max();
                int min=field.getAnnotation(ExcelStrLength.class).min();
                if(fieldValue==null||fieldValue.toString().length()>max||fieldValue.toString().length()<min){//违背
                    msgList.add(field.getAnnotation(ExcelStrLength.class).message());//违背原因
                }
            }

            //调用一个专门为子类新增新注解的校验规则的方法,这个方法专门用于让自定义子类继承重写
            extraAnnotationValid(field,msgList);

            //最后将这个字段的所有错误信息汇总起来
            if(msgList!=null&&msgList.size()>0){
                //获取setRowCode方法,并赋值为1
                try {
                    t.getClass().getMethod((setRowCode==null||"".equals(setRowCode))?DefaultConstant.DEFAULT_SET_ROW_CODE:setRowCode, int.class).invoke(
                            t, unqualifiedRowCode==0?DefaultConstant.DEFAULT_UNQUALIFIED_ROW_CODE:unqualifiedRowCode);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
//                t.setRowCode(1);//这个可以在最后面统一判断一次
                msg.setUnqualifiedMsg(msgList);
                msgs.add(msg);
            }
        }

        int rowCode = 0;
        try {
            rowCode = (Integer) t.getClass().getMethod((getRowCode==null||"".equals(getRowCode))?DefaultConstant.DEFAULT_GET_ROW_CODE:getRowCode).invoke(t);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if(rowCode==(unqualifiedRowCode==0?DefaultConstant.DEFAULT_UNQUALIFIED_ROW_CODE:unqualifiedRowCode)){//说明当前记录不符合校验规则
            try {
                t.getClass().getMethod((setRowMsgs==null||"".equals(setRowMsgs))?DefaultConstant.DEFAULT_SET_ROW_MSGS:setRowMsgs, List.class).invoke(t, msgs);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return t;
        }else{//符合校验规则的返回null,表示此记录不会存入失败记录的集合中
            return null;
        }
    }
    //为自定义新注解实施新的校验规则的方法:为用户程序自定义用的
    protected void extraAnnotationValid(Field field,List<String> msgList) {
        System.out.println("extraAnnotationValid");
    }
    //非注解的校验方式,用于自定义拓展
    public T extraValid(T t) {
        int rowCode = 0;
        try {
            rowCode = (Integer) t.getClass().getMethod((getRowCode==null||"".equals(getRowCode))?DefaultConstant.DEFAULT_GET_ROW_CODE:getRowCode).invoke(t);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if(rowCode==(unqualifiedRowCode==0?DefaultConstant.DEFAULT_UNQUALIFIED_ROW_CODE:unqualifiedRowCode)){
            //待自定义校验方式..
            return t;
        }else{
            //符合校验规则的返回null,表示此记录不会存入失败记录的集合中
            return null;
        }
    }
    //列级校验,可以对要持久化的多条记录的某个字段的值通过对比来校验:比如指定的字段值不可重复这种
    public List<T> rowsValid(List<T> rows,List<T> unqualifiedRows) {
        System.out.println("rowsValid");
        //默认情况下不进行这方面的校验,但如果用户程序有需要,则可以用子类重写此方法,在此方法中自定义列级校验的具体逻辑

        return unqualifiedRows;
    }



}
