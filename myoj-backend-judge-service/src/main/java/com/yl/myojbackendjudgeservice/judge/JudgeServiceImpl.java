package com.yl.myojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;

import com.yl.myojbackendcommon.common.ErrorCode;
import com.yl.myojbackendcommon.exception.BusinessException;
import com.yl.myojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.yl.myojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.yl.myojbackendjudgeservice.judge.codesandbox.CoedSandboxProxy;
import com.yl.myojbackendjudgeservice.judge.strategy.JudgeContext;
import com.yl.myojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.yl.myojbackendmodel.codesandbox.ExecuteCodeResponse;
import com.yl.myojbackendmodel.codesandbox.JudgeInfo;
import com.yl.myojbackendmodel.dto.question.JudgeCase;
import com.yl.myojbackendmodel.entity.Question;
import com.yl.myojbackendmodel.entity.QuestionSubmit;
import com.yl.myojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.yl.myojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Date: 2023/9/14 - 09 - 14 - 21:21
 * @Description: com.yl.myoj.judge
 * 判题服务实现
 */

@Service
public class JudgeServiceImpl implements JudgeService {

    @Value("${codesandbox.type:example}")
    private String type;


    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private JudgeManager judgeManager;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //1)题目提交记录以及题目是否存在
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }

        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        //2）判断题目的状态
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "判题已经结束");
        }

        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean success = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        }
        //3)调用代码沙箱
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CoedSandboxProxy(codeSandbox);
        //获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        //使用链式更方便的给对象赋值
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);

        //4）根据沙箱的执行结果，判断题目的执行结果和状态
        List<String> outputList = executeCodeResponse.getOutputList();
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setOutputList(outputList);
        judgeContext.setInPutList(inputList);
        judgeContext.setQuestion(question);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setQuestionSubmit(questionSubmit);


        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        //5）修改数据库中题目提交记录的状态和结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        boolean b = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新失败");
        }

        return questionFeignClient.getQuestionSubmitById(questionSubmitId);

    }

}
