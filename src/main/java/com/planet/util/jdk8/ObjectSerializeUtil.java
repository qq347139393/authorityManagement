package com.planet.util.jdk8;

import com.planet.common.constant.UtilsConstant;
import lombok.extern.log4j.Log4j2;

import java.io.*;

/**
 * @Description：自定义序列化工具
 */
@Log4j2
public class ObjectSerializeUtil {

    /**
     * 序列化方法
     * @param obj
     * @param characterSet
     * @return
     * @throws IOException
     */
    public static String objToSerialize(Object obj,String characterSet) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        String string = byteArrayOutputStream.toString(characterSet);
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return string;
    }

    /**
     * 序列化方法(默认字符集ISO-8859-1)
     * @param obj
     * @return
     * @throws IOException
     */
    public static String objToSerialize(Object obj) throws IOException {
        return objToSerialize(obj, UtilsConstant.REDIS_CHARACTER_SET);
    }

    /**
     * 反序列化方法
     * @param str
     * @param characterSet
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object serializeToObj(String str,String characterSet) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(characterSet));
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return object;
    }

    /**
     * 反序列化方法(默认字符集ISO-8859-1)
     * @param str
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object serializeToObj(String str) throws IOException, ClassNotFoundException {
        return serializeToObj(str,UtilsConstant.REDIS_CHARACTER_SET);
    }

}
