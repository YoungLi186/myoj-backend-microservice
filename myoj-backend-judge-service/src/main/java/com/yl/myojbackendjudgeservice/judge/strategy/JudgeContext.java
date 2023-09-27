package com.yl.myojbackendjudgeservice.judge.strategy;


import com.yl.myojbackendmodel.codesandbox.JudgeInfo;
import com.yl.myojbackendmodel.dto.question.JudgeCase;
import com.yl.myojbackendmodel.entity.Question;
import com.yl.myojbackendmodel.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * @Date: 2023/9/15 - 09 - 15 - 8:50
 * @Description: com.yl.myoj.judge.strategy
 * 策略管理使用
 */
@Data
public class JudgeContext {

    private List<String> outputList;
    private List<String> inPutList;

    private Question question;

    private List<JudgeCase> judgeCaseList;

    private JudgeInfo judgeInfo;

    private QuestionSubmit questionSubmit;


}
