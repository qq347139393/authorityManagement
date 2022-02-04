package com.planet.util;

import com.planet.common.constant.UtilsConstant;
import io.jsonwebtoken.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 生成、解析和判断jwt工具类
 */
public class JwtUtil {
    /**
     * @Description 签发令牌
     *      1、头部型
     *          密码签名
     *          加密算法
     *      2、payload
     *          签发的时间
     *          唯一标识
     *          签发者
     *          过期时间
     * @param iss 签发者
     * @param ttlMillis 过期时间:<0表示永久有效
     * @param jwtId jwtId
     * @param claims jwt存储的一些非隐私信息
     * @return
     */
    public static String createJwt(String iss, long ttlMillis, String jwtId, Map<String,Object> claims){
        if (claims == null || claims.isEmpty()){
            claims = new HashMap<>();
        }
        //获取当前时间
        long nowMillis = System.currentTimeMillis();
        //构建令牌
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)//构建非隐私信息
                .setId(jwtId)//构建唯一标识
                .setIssuedAt(new Date(nowMillis))//构建签发时间
                .setSubject(iss)//签发者
                .signWith(SignatureAlgorithm.HS256, UtilsConstant.JWT_SIGNATURE);//指定算法和秘钥
        if (ttlMillis>0){//小于等于0时,表示当前jwt永久有效
            long expMillis = nowMillis+ttlMillis;
            Date expData = new Date(expMillis);
            builder.setExpiration(expData);//指定过期时间
        }
        return builder.compact();
    }

    /**
     * @Description 解析jwt
     * @param jwtStr jwt字符串
     * @return
     */
    public static Claims decodeJwt(String jwtStr){
        //带着密码去解析字符串
        return Jwts.parser()
                .setSigningKey(UtilsConstant.JWT_SIGNATURE)
                .parseClaimsJws(jwtStr)
                .getBody();
    }

    /**
     * @Description 校验令牌:1、头部信息和荷载信息是否被篡改 2、校验jwt是否过期
     * @param  jwtStr jwt字符串
     * @return com.auth0.jwt.algorithms
     */
    public static boolean checkJwt(String jwtStr){
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(UtilsConstant.JWT_SIGNATURE).parseClaimsJws(jwtStr);
        } catch (Exception e) {//如果报异常,就说明解析失败
            return false;
        }
        return true;
    }
}
