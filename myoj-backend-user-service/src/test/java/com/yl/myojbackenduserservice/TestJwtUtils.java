package com.yl.myojbackenduserservice;

import com.yl.myojbackendcommon.common.User;
import com.yl.myojbackendcommon.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Date: 2023/9/28 - 09 - 28 - 20:24
 * @Description: PACKAGE_NAME
 */
@SpringBootTest
public class TestJwtUtils {



    @Test
    void testGenerate(){
        //生成token
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id",1);
        tokenMap.put("userAccount", "2");
        tokenMap.put("userRole","admin");
        String token = JwtUtils.getToken(tokenMap);
        User user = JwtUtils.parseJwtToken(token);
        System.out.println(user);
        System.out.println(token);
    }

}
