import com.yl.myojbackendcommon.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @Date: 2023/9/28 - 09 - 28 - 20:24
 * @Description: PACKAGE_NAME
 */
@SpringBootTest()
public class TestJwtUtils {



    @Test
    void testGenerate(){
        //生成token
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id",1);
        tokenMap.put("userAccount", 2);
        String token = JwtUtils.getToken(tokenMap);
        System.out.println(token);
    }

    void testGetClaims(){}

}
