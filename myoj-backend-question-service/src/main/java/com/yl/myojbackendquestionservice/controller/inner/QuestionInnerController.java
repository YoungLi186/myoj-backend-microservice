package com.yl.myojbackendquestionservice.controller.inner;

import com.yl.myojbackendmodel.entity.Question;
import com.yl.myojbackendmodel.entity.QuestionSubmit;
import com.yl.myojbackendquestionservice.service.QuestionService;
import com.yl.myojbackendquestionservice.service.QuestionSubmitService;
import com.yl.myojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Date: 2023/9/22 - 09 - 22 - 20:47
 * @Description: com.yl.myojbackendquestionservice.controller.inner
 */
@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;


    /**
     * 获取题目信息
     * @param questionId
     * @return
     */
    @Override
    @GetMapping("/get/id")
   public Question getQuestionById(@RequestParam("questionId") long questionId){
       return questionService.getById(questionId);
    }


    /**
     * 获取题目提交信息
     * @param questionSubmitId
     * @return
     */
    @Override
    @GetMapping("/question_submit/get/id")
  public   QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId){
        return questionSubmitService.getById(questionSubmitId);
    }


    /**
     * 更新题目提交信息
     * @param questionSubmit
     * @return
     */
    @Override
    @PostMapping("/question_submit/update")
   public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit){
        return questionSubmitService.updateById(questionSubmit);
    }



    @PostMapping("/update")
   public boolean updateQuestionById(@RequestBody Question question){
        return questionService.updateById(question);
    }



}
