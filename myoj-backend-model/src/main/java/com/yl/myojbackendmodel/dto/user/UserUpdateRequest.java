package com.yl.myojbackendmodel.dto.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * 用户 id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;


    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态：正常、注销
     */
    private String userState;

    private static final long serialVersionUID = 1L;
}