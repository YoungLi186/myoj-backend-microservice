package com.yl.myojbackendjudgeservice.judge.strategy;


import com.yl.myojbackendmodel.codesandbox.JudgeInfo;

/**
 * @Date: 2023/9/15 - 09 - 15 - 8:49
 * @Description: com.yl.myoj.judge.strategy
 */
public interface JudgeStrategy {
    JudgeInfo doJudge(JudgeContext judgeContext);
}
