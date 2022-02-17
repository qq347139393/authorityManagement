package com.planet.util.shiro;

import com.planet.common.constant.SuperConstant;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description：摘要
 */
public class DigestsUtil {

    /**
     * @Description sha1方法
     * @param input 需要散列字符串
     * @param salt 盐字符串
     * @return
     */
    public static String sha1(String input, String salt) {
       return new SimpleHash(SuperConstant.HASH_ALGORITHM, input, salt,SuperConstant.HASH_INTERATIONS).toString();
    }

    /**
     * @Description 随机获得salt字符串
     * @return
     */
    public static String generateSalt(){
        SecureRandomNumberGenerator randomNumberGenerator = new SecureRandomNumberGenerator();
        return randomNumberGenerator.nextBytes().toHex();
    }

    /**
     * @Description 生成密码字符密文和salt密文
     * @param
     * @return
     */
    public static Map<String,String> encryptPassword(String passwordPlain) {
       Map<String,String> map = new HashMap<>();
       String salt = generateSalt();
       String password =sha1(passwordPlain,salt);
       map.put("salt", salt);
       map.put("password", password);
       return map;
    }

    /**
     * 密码比对
     * @param encryptPassword
     * @param passwordPlain
     * @param salt
     * @return
     */
    public static boolean checkPassword(String encryptPassword,String passwordPlain,String salt){
        String password =sha1(passwordPlain,salt);
        if(password.equals(encryptPassword)){
            return true;
        }
        return false;
    }
}
