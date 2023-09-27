package com.yl.myojbackendjudgeservice.controller.inner;

import com.yl.myojbackendjudgeservice.judge.JudgeService;
import com.yl.myojbackendmodel.entity.QuestionSubmit;
import com.yl.myojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * @Date: 2023/9/22 - 09 - 22 - 20:58
 * @Description: com.yl.myojbackendjudgeservice.controller.inner
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {

    @Resource
    private JudgeService judgeService;
    
    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    @Override
    @PostMapping("/do")
    public QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId){
      return   judgeService.doJudge(questionSubmitId);
    }
}
