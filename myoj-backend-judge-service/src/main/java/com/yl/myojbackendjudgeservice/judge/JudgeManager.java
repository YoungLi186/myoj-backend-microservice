package com.yl.myojbackendjudgeservice.judge;


import com.yl.myojbackendjudgeservice.judge.strategy.DefaultJudgeStrategy;
import com.yl.myojbackendjudgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.yl.myojbackendjudgeservice.judge.strategy.JudgeContext;
import com.yl.myojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.yl.myojbackendmodel.codesandbox.JudgeInfo;
import com.yl.myojbackendmodel.entity.QuestionSubmit;
import com.yl.myojbackendmodel.enums.QuestionSubmitLanguageEnum;
import org.springframework.stereotype.Service;

/**
 * @Date: 2023/9/15 - 09 - 15 - 9:56
 * @Description: com.yl.myoj.judge
 * 判题策略管理（简化调用）策略模式
 */
@Service
public class JudgeManager {

    public JudgeInfo doJudge(JudgeContext judgeContext) {


        //根据语言选择策略
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();

        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();//默认策略

        if (language.equals(QuestionSubmitLanguageEnum.JAVA.getValue())) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }

        return judgeStrategy.doJudge(judgeContext);


    }
}
