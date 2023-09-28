package com.yl.myojbackenduserservice.controller.inner;

import cn.hutool.core.util.StrUtil;
import com.yl.myojbackendcommon.utils.JwtUtils;
import com.yl.myojbackendmodel.entity.User;
import com.yl.myojbackendserviceclient.service.UserFeignClient;
import com.yl.myojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.List;


/**
 * @Date: 2023/9/22 - 09 - 22 - 20:35
 * @Description: com.yl.myojbackenduserservice.controller.inner
 * 该服务仅内部调用
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource

    private UserService userService;


    /**
     * 根据id获取用户
     *
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/get/id")
    public User getById(@RequestParam("userId") long userId) {
        return userService.getById(userId);
    }


    /**
     * 根据id获取用户列表
     *
     * @param idList
     * @return
     */
    @Override
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("idList") Collection<Long> idList) {

        return userService.listByIds(idList);
    }

    @Override
    @GetMapping( "/get/loginUser/{token}")
    public User getLoginUserAndPermitNull(@PathVariable("token") String token) {
        if (StrUtil.isNotBlank(token)){
            Long usrId = JwtUtils.getClaims(token).get("id", Long.class);
            return userService.getById(usrId);
        }
        return  null;
    }
}
