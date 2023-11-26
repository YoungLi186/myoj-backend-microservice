package com.yl.myojbackendcommon.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @Date: 2023/11/26 - 11 - 26 - 10:32
 * @Description: com.yl.myojbackendcommon.common
 */
@Data
public class User {

    /**
     * 用户 id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;
}
