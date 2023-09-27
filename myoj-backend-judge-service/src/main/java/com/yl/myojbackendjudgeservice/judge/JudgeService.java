package com.yl.myojbackendjudgeservice.judge;


import com.yl.myojbackendmodel.entity.QuestionSubmit;

/**
 * @Date: 2023/9/14 - 09 - 14 - 21:11
 * @Description: com.yl.myoj.judge
 */
public interface JudgeService {
    QuestionSubmit doJudge(long questionSubmitId);
}
