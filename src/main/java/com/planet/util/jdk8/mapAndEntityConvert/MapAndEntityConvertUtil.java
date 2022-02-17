package com.planet.util.jdk8.mapAndEntityConvert;

import com.planet.util.jdk8.mapAndEntityConvert.annotation.FieldFromMapKey;
import com.planet.util.jdk8.mapAndEntityConvert.annotation.FieldToMapKey;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class MapAndEntityConvertUtil {
    /** 默认的表示日期时间的字符串转成Date类型的转换格式 */
    private static final String DATE_FORMAT="yyyy-MM-dd HH:mm:ss";
    /**
     * 将指定类型的实例对象转成另一种类型的实例对象
     * @param t
     * @param model
     * @param rClass
     * @param <T>
     * @param <R>
     * @return
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     */
    public static <T,R> R entityToEntity(T t,int model,Class<R> rClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, InstantiationException, ParseException {
        if(t==null||rClass==null){
            log.error("将一个类型的实例对象转成另一个类型的实例对象失败:入参错误");
            return null;
        }
        if(model!=0&&model!=1){
            log.error("模型选择只能为0或1,但这里却用了其他非法值");
            return null;
        }
        //先将指定的实例对象转成Map对象
        Map<String,Object> map=entityToMap(t,model);
        //再将Map对象转成另一个类型的实例对象
        return mapToEntity(map,rClass);
    }

    public static <T> Map<String,Object> mapToMapByEntity(Map<String,Object> map,Class<T> tClass){
        //待完成..

        return null;
    }

    /**
     * 将指定Map对象转成指定类型的实体对象
     * @param map
     * @param tClass
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public static <T> T mapToEntity(Map<String,Object> map,Class<T> tClass) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException, InstantiationException, ParseException {
        if(map==null||map.size()<=0||tClass==null){
            log.error("map转成Entity失败:入参错误");
            return null;
        }
        //获取指定实体对象对应的类的全部属性
        List<Field> fields=getFieldsByRecursion(tClass);
        //利用类反射的工具方法,动态创建T类型的实例对象
        T t=createObject(tClass.getName(),null,null);
        for (Field field : fields) {
            String fieldName=null;
            if(field.isAnnotationPresent(FieldFromMapKey.class)){//判断是否有FieldFromMapKey注解,如果有则使用注解指定的值来寻找Map中的key
                FieldFromMapKey fieldFromMapKey=field.getDeclaredAnnotation(FieldFromMapKey.class);
                fieldName=fieldFromMapKey.value();
            }else{
                fieldName=field.getName();
            }
            Object value=map.get(fieldName);
            if(value==null){
                continue;
            }
            //几种常见的基本类型和Mvc特定规则下的类型转换
            value=valueToClassValue(value,field.getType());
            field.setAccessible(true);
            field.set(t,value);
        }
        return t;
    }

    /**
     * 根据指定的的实体类对象转成Map对象(属性名不转成下划线)
     * @param t
     * @param model 模式:0表示忽略为null的字段;1表示不忽略为null的字段
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public static <T> Map<String,Object> entityToMap(T t,int model) throws IllegalAccessException {
        return entityToMap(t,model,null);
    }

    /**
     * 根据指定的的实体类对象转成Map对象
     * @param t
     * @param model 模式:0表示忽略为null的字段;1表示不忽略为null的字段
     * @param flag 是否将属性名转成下划线格式,默认false
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    public static <T> Map<String,Object> entityToMap(T t,int model,Boolean flag) throws IllegalAccessException {
        if(t==null){
            log.error("将指定的实体对象转成Map对象失败:实体对象为null");
            return null;
        }
        if(model!=0&&model!=1){
            log.error("模型选择只能为0或1,但这里却用了其他非法值");
            return null;
        }
        //获取指定实体对象对应的类的全部属性
        List<Field> fields=getFieldsByRecursion(t.getClass());
        Map<String,Object> map=new HashMap<>();
        for (Field field: fields) {
            //判断是否为非静态属性:如果是静态属性,就过滤掉
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String fieldName=null;
            if(field.isAnnotationPresent(FieldToMapKey.class)){//判断是否有FieldToMapKey注解,如果有则使用注解指定的值来构建Map中的key
                FieldToMapKey fieldToMapKey=field.getDeclaredAnnotation(FieldToMapKey.class);
                fieldName=fieldToMapKey.value().trim();
            }else{
                fieldName=field.getName().trim();
            }
            field.setAccessible(true);
            Object value=field.get(t);
            if(value==null&&model==0){//忽略为null的字段
                continue;
            }
            if(flag!=null&&flag.equals(true)){//转成下划线
                fieldName= camelToUnderline(fieldName);
            }
            map.put(fieldName,value);
        }
        return map;
    }


    //**********************本类的私有方法*******************

    private static final char UNDERLINE = '_';

    /**
     * 驼峰格式字符串转换为下划线格式字符串
     *
     * @param param
     * @return
     */
    private static String camelToUnderline(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
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
     * @param param
     * @return
     */
    private static String underlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 几种常见的基本类型和Mvc特定规则下的类型转换
     * @param value
     * @param tClass
     * @param <T>
     * @return
     * @throws ParseException
     */
    private static <T> T valueToClassValue(Object value,Class<T> tClass) throws ParseException {
        //针对mvc架构的Map型参数进行特定处理
        if(value instanceof String[]){
            if(tClass.equals(String[].class)){
                //类型完全一致
            }else if(tClass.equals(Integer[].class)){
                String[] strings=((String[])value);
                Integer[] integers=new Integer[strings.length];
                for (int i=0;i<strings.length;i++){
                    integers[i]=Integer.valueOf(String.valueOf(strings[i]));
                }
                value=integers;
            }else if(tClass.equals(Double[].class)){
                String[] strings=((String[])value);
                Double[] doubles=new Double[strings.length];
                for (int i=0;i<strings.length;i++){
                    doubles[i]=Double.valueOf(String.valueOf(strings[i]));
                }
                value=doubles;
            }else if(tClass.equals(Date[].class)){
                String[] strings=((String[])value);
                Date[] dates=new Date[strings.length];
                SimpleDateFormat sdf=new SimpleDateFormat(DATE_FORMAT);
                for (int i=0;i<strings.length;i++){
                    dates[i]=sdf.parse(String.valueOf(strings[i]));
                }
                value=dates;
            }else if(tClass.equals(List.class)){
                value= Arrays.asList(value);
            }else{
                value=((String[])value)[0];
            }
        }
        //基本类型的入参的类型检测:考虑到可能会用字符串类型的参数来传Integer、Double和Date的情况
        if(value instanceof String){
            if(tClass.equals(Integer.class)){
                value=Integer.valueOf(String.valueOf(value));
            }else if(tClass.equals(Double.class)){
                value=Double.valueOf(String.valueOf(value));
            }else if(tClass.equals(Date.class)){
                SimpleDateFormat sdf=new SimpleDateFormat(DATE_FORMAT);
                value=sdf.parse(String.valueOf(value));
            }
        }
        return (T)value;
    }

    /**
     * 根据给定的类路径、属性名列表、属性值列表，来利用反射创建指定类的实例对象
     * 注意:该类必须有公共的无参构造器;不要用于创建spring管理的类的实例对象
     * 这个方法是来自我们做的另一个工具类FunctionByReflectUtil的,由于我们不想让这两个类存在太多依赖关系,所以这里直接把那个类的这个方法拿过来单独放置使用
     * @param classPath
     * @param fieldNames
     * @param fieldValues
     * @param <T>
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    private static <T> T createObject(String classPath, List<String> fieldNames, List<Object> fieldValues) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        if(classPath==null||"".equals(classPath)){
            log.error("动态创建实例对象失败:类路径为空");
            return null;
        }
        Class cls=Class.forName(classPath);
        //1.创建指定类的实例对象(该类必须有公共的无参构造器)
        Object object=cls.newInstance();
        T tObj=(T)object;

        if(fieldNames==null||fieldNames.size()<=0){//返回没有设置任何属性值的实例对象
            return tObj;
        }
        //2.如果有需要设置值的属性,则遍历属性列表对每个需要设置值的属性设置值
        for (int i=0;i<fieldNames.size();i++){
            Field field=cls.getDeclaredField(fieldNames.get(i));
            field.setAccessible(true);
            field.set(tObj,fieldValues.get(i));
        }
        return tObj;
    }
    /**
     * 根据类对象,来递归(向父级)获取该类或该类的上级类的全部属性对象
     * 这个方法是来自我们做的另一个工具类FunctionByReflectUtil的,由于我们不想让这两个类存在太多依赖关系,所以这里直接把那个类的这个方法拿过来单独放置使用
     * @param cls
     * @return
     */
    private static List<Field> getFieldsByRecursion(Class cls){
        if(cls==null){
            log.error("(递归)获取该类以及该类的上级类的全部属性对象失败:类型为null");
            return null;
        }
        List<Field> fields=new ArrayList<>();
        //将当前实体类的属性放入fields
        if(cls.getDeclaredFields()!=null&&cls.getDeclaredFields().length>0){
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
        }
        //判断是否有父类并且父类不为Object,进行递归获取父级的属性
        if(cls.getSuperclass()!=null&&!(cls.getSuperclass().getSimpleName()).equals("Object")){
            List<Field> superFields=getFieldsByRecursion(cls.getSuperclass());
            if(superFields!=null&&superFields.size()>0){
                fields.addAll(superFields);
            }
        }
        return fields;
    }

}
