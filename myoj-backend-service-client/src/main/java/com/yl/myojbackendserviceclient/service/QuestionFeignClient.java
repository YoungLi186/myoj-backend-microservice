package com.yl.myojbackendserviceclient.service;


import com.yl.myojbackendmodel.entity.Question;
import com.yl.myojbackendmodel.entity.QuestionSubmit;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * @author
 * @description 针对表【question(题目)】的数据库操作Service
 * @createDate 2023-09-08 21:03:45
 */
@FeignClient(name = "myoj-backend-question-service",path = "/api/question/inner")
public interface QuestionFeignClient  {


    /**
     * 获取题目信息
     * @param questionId
     * @return
     */
    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);


    /**
     * 获取题目提交信息
     * @param questionSubmitId
     * @return
     */
    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId);


    /**
     * 更新题目提交信息
     * @param questionSubmit
     * @return
     */
    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);


    /**
     * 更新题目
     * @param question
     * @return
     */
    @PostMapping("/update")
    boolean updateQuestionById(@RequestBody Question question);



}
