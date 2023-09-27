package com.yl.myojbackendjudgeservice.judge.codesandbox.impl;



import com.yl.myojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.yl.myojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.yl.myojbackendmodel.codesandbox.ExecuteCodeResponse;
import com.yl.myojbackendmodel.codesandbox.JudgeInfo;
import com.yl.myojbackendmodel.enums.JudgeInfoMessageEnum;
import com.yl.myojbackendmodel.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * @Date: 2023/9/13 - 09 - 13 - 21:05
 * @Description: com.yl.myoj.judge.codesandbox
 * 示例代码沙箱
 */
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();


        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);

        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;

    }
}
