package com.yl.myojbackendquestionservice.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.yl.myojbackendcommon.annotation.AuthCheck;
import com.yl.myojbackendcommon.common.BaseResponse;
import com.yl.myojbackendcommon.common.DeleteRequest;
import com.yl.myojbackendcommon.common.ErrorCode;
import com.yl.myojbackendcommon.common.ResultUtils;
import com.yl.myojbackendcommon.constant.UserConstant;
import com.yl.myojbackendcommon.exception.BusinessException;
import com.yl.myojbackendcommon.exception.ThrowUtils;
import com.yl.myojbackendcommon.utils.JwtUtils;
import com.yl.myojbackendmodel.dto.question.*;
import com.yl.myojbackendmodel.dto.questionsubmit.QuestionSubmitAddRequest;
import com.yl.myojbackendmodel.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.yl.myojbackendmodel.entity.Question;
import com.yl.myojbackendmodel.entity.QuestionSubmit;
import com.yl.myojbackendmodel.entity.User;
import com.yl.myojbackendmodel.vo.QuestionSubmitVO;
import com.yl.myojbackendmodel.vo.QuestionVO;
import com.yl.myojbackendquestionservice.manage.RedisLimiter;
import com.yl.myojbackendquestionservice.service.QuestionService;
import com.yl.myojbackendquestionservice.service.QuestionSubmitService;
import com.yl.myojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private RedisLimiter redisLimiter;

    private final static Gson GSON = new Gson();

    /**
     * 创建题目
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);

        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }

        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }

        List<JudgeCase> judgeCases = questionAddRequest.getJudgeCase();

        if (judgeCases != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCases));
        }

        questionService.validQuestion(question, true);
        String token = request.getHeader("Authorization");
        User loginUser = new User();
        BeanUtils.copyProperties(Objects.requireNonNull(JwtUtils.parseJwtToken(token)),loginUser);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String token = request.getHeader("Authorization");
        User user = new User();
        BeanUtils.copyProperties(Objects.requireNonNull(JwtUtils.parseJwtToken(token)),user);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userFeignClient.isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 题目更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {

        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }

        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }

        List<JudgeCase> judgeCases = questionUpdateRequest.getJudgeCase();

        if (judgeCases != null) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCases));
        }

        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);

        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取题目(脱敏)
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 根据 id 获取题目
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Question> getQuestionById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String token = request.getHeader("Authorization");
        User loginUser = new User();
        BeanUtils.copyProperties(Objects.requireNonNull(JwtUtils.parseJwtToken(token)),loginUser);
        if (!loginUser.getId().equals(question.getUserId()) && !userFeignClient.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }


        return ResultUtils.success(question);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

//    /**
//     * 分页获取当前用户创建的资源列表
//     *
//     * @param questionQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/my/list/page/vo")
//    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
//                                                                 HttpServletRequest request) {
//        if (questionQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        String token = request.getHeader("Authorization");
//        User loginUser = userFeignClient.getLoginUserAndPermitNull(token);
//        questionQueryRequest.setUserId(loginUser.getId());
//        long current = questionQueryRequest.getCurrent();
//        long size = questionQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<Question> questionPage = questionService.page(new Page<>(current, size),
//                questionService.getQueryWrapper(questionQueryRequest));
//        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
//    }

//    /**
//     * 编辑题目（用户和管理员）
//     *
//     * @param questionEditRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/edit")
//    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
//        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        Question question = new Question();
//        BeanUtils.copyProperties(questionEditRequest, question);
//        List<String> tags = questionEditRequest.getTags();
//        if (tags != null) {
//            question.setTags(GSON.toJson(tags));
//        }
//        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
//        if (judgeConfig != null) {
//            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
//        }
//
//        List<JudgeCase> judgeCases = questionEditRequest.getJudgeCase();
//
//        if (judgeCases != null) {
//            question.setJudgeCase(JSONUtil.toJsonStr(judgeCases));
//        }
//
//
//        // 参数校验
//        String token = request.getHeader("Authorization");
//        questionService.validQuestion(question, false);
//        User loginUser = userFeignClient.getLoginUserAndPermitNull(token);
//        long id = questionEditRequest.getId();
//        // 判断是否存在
//        Question oldQuestion = questionService.getById(id);
//        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
//        // 仅本人或管理员可编辑
//        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userFeignClient.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        boolean result = questionService.updateById(question);
//        return ResultUtils.success(result);
//    }


    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                           HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的id
     */
    @PostMapping("/question_submit/do")
    @SentinelResource("doQuestionSubmit")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录成功才可以提交题目
        String token = request.getHeader("Authorization");

        User loginUser = new User();
        BeanUtils.copyProperties(Objects.requireNonNull(JwtUtils.parseJwtToken(token)),loginUser);
        // 限流
        boolean rateLimit = redisLimiter.doRateLimit(loginUser.getId().toString());
        if (!rateLimit) {
            return ResultUtils.error(ErrorCode.TOO_MANY_REQUEST, "提交频繁,请稍后重试");
        }
        long result = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(result);
    }


    /**
     * 分页获取题目提交列表（除了管理员和普通用户外只能看到非答案，提交代码等公开信息）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        String token = request.getHeader("Authorization");
        User loginUser = new User();
        if (StrUtil.isNotBlank(token)) {
            BeanUtils.copyProperties(Objects.requireNonNull(JwtUtils.parseJwtToken(token)),loginUser);
        }
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));

    }

}
