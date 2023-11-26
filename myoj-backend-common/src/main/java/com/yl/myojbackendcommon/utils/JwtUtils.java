package com.yl.myojbackendcommon.utils;

import com.yl.myojbackendcommon.common.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtils {

    /**
     * TOKEN的有效期半小时
     */
    private static final int TOKEN_TIME_OUT = 1800;

    /**
     * 加密KEY
     */
    private static final String TOKEN_SECRET = "yyyyllllkkkk";


    /**
     * 生成Token
     *
     * @param params
     * @return
     */
    public static String getToken(Map params) {
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                // 加密方式
                .signWith(SignatureAlgorithm.HS256, TOKEN_SECRET)
                // 过期时间戳
                .setExpiration(new Date(currentTime + TOKEN_TIME_OUT * 1000))
                .addClaims(params)
                .compact();
    }


    /**
     * 获取Token中的claims信息
     */
    public static Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(TOKEN_SECRET)
                .parseClaimsJws(token).getBody();
    }


    /**
     * 根据Token 解析用户信息
     * @param token
     * @return
     */
    public static User parseJwtToken(String token){
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        Claims claims = getClaims(token);
        Long id = claims.get("id", Long.class);
        String userAccount = claims.get("userAccount", String.class);
        String userRole = claims.get("userRole", String.class);
        User user = new User();
        user.setId(id);
        user.setUserAccount(userAccount);
        user.setUserRole(userRole);
        return user;
    }




    /**
     * 是否有效 true-有效，false-失效
     */
    public static boolean verifyToken(String token) {

        if (StringUtils.isEmpty(token)) {
            return false;
        }

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(TOKEN_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return false;
        }

        return true;
    }





}