package com.yl.myojbackendjudgeservice.judge.codesandbox;


import com.yl.myojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.yl.myojbackendmodel.codesandbox.ExecuteCodeResponse;

/**
 * @Date: 2023/9/13 - 09 - 13 - 20:48
 * @Description: com.yl.myoj.judge.codesandbox
 * 代码沙箱接口
 */
public interface CodeSandbox {

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);


}
