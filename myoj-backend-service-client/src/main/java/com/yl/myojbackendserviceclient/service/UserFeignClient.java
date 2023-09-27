package com.yl.myojbackendserviceclient.service;

import com.yl.myojbackendcommon.common.ErrorCode;
import com.yl.myojbackendcommon.exception.BusinessException;
import com.yl.myojbackendmodel.entity.User;
import com.yl.myojbackendmodel.enums.UserRoleEnum;
import com.yl.myojbackendmodel.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static com.yl.myojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户服务
 */
@FeignClient(name = "myoj-backend-user-service",path = "/api/user/inner")
public interface UserFeignClient {


    /**
     * 根据id获取用户
     * @param userId
     * @return
     */
    @GetMapping("/get/id")
    User getById(@RequestParam("userId")  long userId);


    /**
     * 根据id获取用户列表
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam("idList")  Collection<Long> idList);

    /**
     * 获取当前登录用户
     *内部调用，为空不用抛出异常
     * @param request
     * @return
     */
   default User getLoginUser(HttpServletRequest request){
       // 先判断是否已登录
       Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
       return (User) userObj;
   }




    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    default boolean isAdmin(User user){
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());

    }


    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
   default UserVO getUserVO(User user){
       if (user == null) {
           return null;
       }
       UserVO userVO = new UserVO();
       BeanUtils.copyProperties(user, userVO);
       return userVO;
   }



}