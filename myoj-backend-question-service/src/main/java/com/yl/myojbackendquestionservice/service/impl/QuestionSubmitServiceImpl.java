package com.yl.myojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yl.myojbackendcommon.common.ErrorCode;
import com.yl.myojbackendcommon.constant.CommonConstant;
import com.yl.myojbackendcommon.exception.BusinessException;
import com.yl.myojbackendcommon.utils.SqlUtils;
import com.yl.myojbackendmodel.dto.questionsubmit.QuestionSubmitAddRequest;
import com.yl.myojbackendmodel.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.yl.myojbackendmodel.entity.Question;
import com.yl.myojbackendmodel.entity.QuestionSubmit;
import com.yl.myojbackendmodel.entity.User;
import com.yl.myojbackendmodel.enums.QuestionSubmitLanguageEnum;
import com.yl.myojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.yl.myojbackendmodel.vo.QuestionSubmitVO;
import com.yl.myojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.yl.myojbackendquestionservice.mq.CodeMqProducer;
import com.yl.myojbackendquestionservice.service.QuestionService;
import com.yl.myojbackendquestionservice.service.QuestionSubmitService;
import com.yl.myojbackendserviceclient.service.JudgeFeignClient;
import com.yl.myojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.yl.myojbackendcommon.constant.MqConstant.CODE_EXCHANGE_NAME;
import static com.yl.myojbackendcommon.constant.MqConstant.CODE_ROUTING_KEY;

/**
 * @author 18683
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-09-08 21:03:45
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {


    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    @Lazy//解决依赖循环
    private JudgeFeignClient judgeFeignClient;


    @Resource
    private CodeMqProducer codeMqProducer;


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {

        //判断编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }

        Long questionId = questionSubmitAddRequest.getQuestionId();

        // 判断题目是否存在
        Question question = questionService.getById(questionId);

        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //是否已提交题目
        long userId = loginUser.getId();

        //每个用户需要串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(questionSubmitAddRequest.getLanguage());

        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");

        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目提交失败");
        }

        Long questionSubmitId = questionSubmit.getId();

        codeMqProducer.sendMessage(CODE_EXCHANGE_NAME, CODE_ROUTING_KEY, String.valueOf(questionSubmitId));

        return questionSubmitId;

    }


    /**
     * 获取查询包装类
     *
     * @param questionQuerySubmitRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionQuerySubmitRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionQuerySubmitRequest == null) {
            return queryWrapper;
        }

        String language = questionQuerySubmitRequest.getLanguage();
        Integer status = questionQuerySubmitRequest.getStatus();
        Long userId = questionQuerySubmitRequest.getUserId();
        Long questionId = questionQuerySubmitRequest.getQuestionId();
        String sortField = questionQuerySubmitRequest.getSortField();
        String sortOrder = questionQuerySubmitRequest.getSortOrder();


        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取查询的封装类
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);

        Long userId = null;
        if (loginUser != null) {
            userId = loginUser.getId();
        }

        if (!questionSubmit.getUserId().equals(userId) && !userFeignClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode("无权限查看");
        }


        return questionSubmitVO;
    }

    /**
     * 分页获取题目提交封装类
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {

        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }


        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList
                .stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser)).collect(Collectors.toList());

        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }


}




