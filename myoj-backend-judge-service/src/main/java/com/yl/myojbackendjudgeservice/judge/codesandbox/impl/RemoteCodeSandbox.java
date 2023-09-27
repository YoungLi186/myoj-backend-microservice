package com.yl.myojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yl.myojbackendcommon.common.ErrorCode;
import com.yl.myojbackendcommon.exception.BusinessException;
import com.yl.myojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.yl.myojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.yl.myojbackendmodel.codesandbox.ExecuteCodeResponse;

/**
 * @Date: 2023/9/13 - 09 - 13 - 21:05
 * @Description: com.yl.myoj.judge.codesandbox
 * 远程代码沙箱
 */
public class RemoteCodeSandbox implements CodeSandbox {

    //鉴权请求头和秘钥
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = "http://localhost:8090/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(json)
                .execute()
                .body();

        if (StrUtil.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);


    }
}
