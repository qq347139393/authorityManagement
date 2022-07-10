package com.planet.util.jdk8;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射工具类
 */
public class ReflectUtil {
    /**
     * 根据给定的类路径、属性名列表、属性值列表，来利用反射创建指定类的实例对象
     * 注意:该类必须有公共的无参构造器;不要用于创建spring管理的类的实例对象
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
    public static <T> T createObject(String classPath, List<String> fieldNames, List<Object> fieldValues) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        if(classPath==null||"".equals(classPath)){
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
     * 根据类路径、方法名、方法参数类型列表和方法参数值列表，执行指定类的指定方法
     * 注意:该类必须有公共的无参构造器;不要用于创建spring管理的类的实例对象
     * @param classPath
     * @param methodName
     * @param parameterClasses
     * @param parameterValues
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static Object executeMethod(String classPath,String methodName,List<Class> parameterClasses,List<Object> parameterValues) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if(classPath==null||"".equals(classPath)||methodName==null||"".equals(methodName)){
            return null;
        }
        //1.创建指定类的实例对象(该类必须有公共的无参构造器)
        Object object=Class.forName(classPath).newInstance();
        return executeMethod(object,methodName,parameterClasses,parameterValues);
    }

    /**
     * 根据实例对象、方法名、方法参数类型列表和方法参数值列表，执行指定类的指定方法
     * @param t
     * @param methodName
     * @param parameterClasses
     * @param parameterValues
     * @param <T>
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static <T> Object executeMethod(T t,String methodName,List<Class> parameterClasses,List<Object> parameterValues) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(t==null||methodName==null||"".equals(methodName)){
            return null;
        }
        Class cls=t.getClass();
        //1.获取指定的方法
        Object[] parameterClassObjs=parameterClasses.toArray();
        Class[] parameterClassArray=new Class[parameterClassObjs.length];
        for (int i=0;i<parameterClassObjs.length;i++){
            parameterClassArray[i]=(Class)parameterClassObjs[i];
        }
        Method method=cls.getMethod(methodName,parameterClassArray);
        //2.执行指定方法,然后返回结果
        method.setAccessible(true);
        return method.invoke(t,parameterValues.toArray());
    }

    /**
     * 根据属性名和实例对象,获取该实例对象中指定属性的属性值
     * @param fieldName
     * @param t
     * @param <T>
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static <T> Object getFieldValue(String fieldName,T t) throws NoSuchFieldException, IllegalAccessException {
        Class cls=t.getClass();
        //1.获取指定属性名的属性对象
        Field field=getFieldByRecursion(fieldName,t.getClass());
        //2.根据属性对象和该类的实例对象,来获取实例对象中该属性的属性值
        field.setAccessible(true);
        return field.get(t);
    }

    /**
     * 根据属性名和类对象,来递归(向父级)获取该类或该类的上级类的对应属性名的属性对象
     * @param fieldName
     * @param cls
     * @return
     */
    public static Field getFieldByRecursion(String fieldName,Class cls){
        Field field=null;
        try {
            field=cls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
            //尝试从其父类进行查找,并屏蔽掉Object基类
            if(cls.getSuperclass()==null||cls.getSuperclass().getSimpleName().equals("Object")){
                return null;
            }
            field=getFieldByRecursion(fieldName,cls.getSuperclass());
        }
        return field;
    }

    /**
     * 根据类对象,来递归(向父级)获取该类或该类的上级类的全部属性对象
     * @param cls
     * @return
     */
    public static List<Field> getFieldsByRecursion(Class cls){
        if(cls==null){
            return null;
        }
        List<Field> fields=new ArrayList<>();
        //将当前实体类的属性放入fields
        if(cls.getDeclaredFields()!=null&&cls.getDeclaredFields().length>0){
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
        }
        //判断是否有父类并且父类不为Object,进行递归获取父级的属性
        if(cls.getSuperclass()!=null&&cls.getSuperclass().getSimpleName().equals("Object")){
            List<Field> superFields=getFieldsByRecursion(cls.getSuperclass());
            if(superFields!=null&&superFields.size()>0){
                fields.addAll(superFields);
            }
        }
        return fields;
    }

    /**
     * 根据属性名和类对象,获取该属性的属性类型
     * @param fieldName
     * @param cls
     * @return
     * @throws NoSuchFieldException
     */
    public static Class getFieldClass(String fieldName,Class cls) throws NoSuchFieldException {
        //1.获取指定属性名的属性对象
        Field field=cls.getDeclaredField(fieldName);
        //2.根据属性对象获取属性类型
        field.setAccessible(true);
        return field.getType();
    }
}
