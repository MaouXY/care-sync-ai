package com.caresync.ai.controller;

import com.caresync.ai.context.BaseContext;
import com.caresync.ai.model.DTO.*;
import com.caresync.ai.model.VO.*;
import com.caresync.ai.model.VO.TrainingChatResponseVO;
import com.caresync.ai.model.ai.ChatContent;
import com.caresync.ai.model.ai.ChatMessage;
import com.caresync.ai.model.ai.ChatRequest;
import com.caresync.ai.model.entity.SimulationScenario;
import com.caresync.ai.model.entity.TrainingChatRecord;
import com.caresync.ai.model.entity.TrainingEvaluation;
import com.caresync.ai.model.entity.TrainingSession;
import com.caresync.ai.result.Result;
import com.caresync.ai.service.ISimulationScenarioService;
import com.caresync.ai.service.ITrainingChatRecordService;
import com.caresync.ai.service.ITrainingEvaluationService;
import com.caresync.ai.service.ITrainingSessionService;
import com.caresync.ai.utils.ArkUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.caresync.ai.utils.JsonUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI模拟训练模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/social-worker/simulation")
@Tag(name = "AI模拟训练模块接口", description = "AI模拟留守儿童训练相关接口")
public class AiSimulationController {

    private static final Logger logger = LoggerFactory.getLogger(AiSimulationController.class);

    @Autowired
    private ISimulationScenarioService simulationScenarioService;

    @Autowired
    private ITrainingSessionService trainingSessionService;

    @Autowired
    private ITrainingChatRecordService trainingChatRecordService;

    @Autowired
    private ArkUtil arkUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITrainingEvaluationService trainingEvaluationService;

    /**
     * 获取模拟训练场景列表
     * @return 场景列表
     */
    @GetMapping("/scenarios")
    @Operation(summary = "获取模拟训练场景列表", description = "获取所有可用的模拟训练场景列表")
    public Result<List<SimulationScenarioVO>> getScenarioList() {
        try {
            logger.info("开始获取模拟训练场景列表");

            // 查询所有公开的场景
            List<SimulationScenario> scenarios = simulationScenarioService.lambdaQuery()
                    .eq(SimulationScenario::getIsPublic, true)
                    .orderByDesc(SimulationScenario::getCreateTime)
                    .list();

            logger.info("成功获取到{}个模拟训练场景", scenarios.size());

            // 转换为VO列表
            List<SimulationScenarioVO> scenarioVOs = new ArrayList<>();
            for (SimulationScenario scenario : scenarios) {
                SimulationScenarioVO vo = new SimulationScenarioVO();
                vo.setId(scenario.getId());
                vo.setScenarioName(scenario.getScenarioName());
                vo.setScenarioType(scenario.getScenarioType());
                vo.setDescription(scenario.getDescription());
                vo.setDifficultyLevel(scenario.getDifficultyLevel());

                scenarioVOs.add(vo);
            }

            logger.info("成功转换为{}个SimulationScenarioVO对象", scenarioVOs.size());
            return Result.success(scenarioVOs);
        } catch (Exception e) {
            logger.error("获取模拟训练场景列表失败: {}", e.getMessage());
            return Result.error("获取场景列表失败");
        }
    }

    /**
     * 开始新的训练会话
     * @param startTrainingSessionDTO 开始训练会话DTO
     * @return 训练会话VO
     */
    @PostMapping("/start")
    @Operation(summary = "开始新的训练会话", description = "选择场景并开始一个新的训练会话")
    public Result<TrainingSessionVO> startTrainingSession(@RequestBody StartTrainingSessionDTO startTrainingSessionDTO) {
        try {
            logger.info("开始创建新的训练会话，场景ID: {}", startTrainingSessionDTO.getScenarioId());

            // 获取当前登录社工的ID
            Long workerId = BaseContext.getCurrentId();
            if (workerId == null) {
                logger.error("未获取到当前登录社工信息");
                return Result.error("未登录，请先登录");
            }

            logger.info("当前登录社工ID: {}", workerId);

            // 校验场景是否存在
            SimulationScenario scenario = simulationScenarioService.getById(startTrainingSessionDTO.getScenarioId());
            if (scenario == null) {
                logger.error("场景不存在，场景ID: {}", startTrainingSessionDTO.getScenarioId());
                return Result.error("选择的场景不存在");
            }

            // 创建训练会话实体
            TrainingSession trainingSession = new TrainingSession();
            trainingSession.setWorkerId(workerId);
            trainingSession.setScenarioId(startTrainingSessionDTO.getScenarioId());
            trainingSession.setSessionStatus("IN_PROGRESS"); // 设置会话状态为进行中
            trainingSession.setStartTime(LocalDateTime.now()); // 设置开始时间为当前时间
            trainingSession.setTotalRounds(0); // 初始对话轮次为0

            // 保存训练会话
            boolean saved = trainingSessionService.save(trainingSession);
            if (!saved) {
                logger.error("创建训练会话失败");
                return Result.error("创建训练会话失败，请重试");
            }

            logger.info("成功创建训练会话，会话ID: {}", trainingSession.getId());

            // 转换为VO返回
            TrainingSessionVO trainingSessionVO = new TrainingSessionVO();
            BeanUtils.copyProperties(trainingSession, trainingSessionVO);

            return Result.success(trainingSessionVO);
        } catch (Exception e) {
            logger.error("创建训练会话异常: {}", e.getMessage());
            return Result.error("创建训练会话失败");
        }
    }

    /**
     * 发送训练消息
     * @param sendTrainingMessageDTO 发送训练消息DTO
     * @return 训练对话响应结果
     */

    @PostMapping("/send")
    @Operation(summary = "发送训练消息", description = "社工向模拟儿童发送消息")
    public Result<TrainingChatResponseVO> sendTrainingMessage(@RequestBody SendTrainingMessageDTO sendTrainingMessageDTO) {
        try {
            logger.info("收到训练消息，会话ID: {}, 内容: {}", sendTrainingMessageDTO.getSessionId(), sendTrainingMessageDTO.getPrompt());

            // 1. 验证会话是否存在
            Long sessionId = sendTrainingMessageDTO.getSessionId();
            TrainingSession session = trainingSessionService.getById(sessionId);
            if (session == null) {
                log.error("训练会话不存在，会话ID: {}", sessionId);
                return Result.error("训练会话不存在");
            }

            // 3. 保存社工发送的消息
            TrainingChatRecord workerMessage = TrainingChatRecord.builder()
                    .sessionId(sessionId)
                    .roundNum(session.getTotalRounds()+1)
                    .contentType("TEXT")
                    .content(sendTrainingMessageDTO.getPrompt())
                    .aiReply(false) // 社工消息
                    .aiGuidance("")
                    .build();

            trainingChatRecordService.save(workerMessage);
            logger.info("保存社工消息成功，记录ID: {}", workerMessage.getId());

            // 4. 调用大模型进行三次交互

            // 4.1 第一次调用：模拟儿童回复
            String childReply = getChildReply(sendTrainingMessageDTO.getPrompt(), sendTrainingMessageDTO.getHistory());
            logger.info("模拟儿童回复: {}", childReply);

            // 4.2 第二次调用：生成情感分析
            String emotionAnalysisJson = getEmotionAnalysis(childReply, sendTrainingMessageDTO.getPrompt(), sendTrainingMessageDTO.getHistory());
            logger.info("情感分析结果: {}", emotionAnalysisJson);

            // 检查emotionAnalysisJson是否为null或空
            logger.info("emotionAnalysisJson是否为null: {}", emotionAnalysisJson == null);
            if (emotionAnalysisJson != null) {
                logger.info("emotionAnalysisJson长度: {}", emotionAnalysisJson.length());
            } else {
                // 如果emotionAnalysisJson为null，使用空JSON字符串
                emotionAnalysisJson = "{}";
                logger.info("使用空JSON字符串作为情感分析结果");
            }

            // 4.3 第三次调用：生成指导意见
            String aiGuidance = getAiGuidance(childReply,emotionAnalysisJson,sendTrainingMessageDTO.getPrompt(), sendTrainingMessageDTO.getHistory());
            logger.info("AI指导意见: {}", aiGuidance);

            // 5. 保存AI回复的消息（儿童模拟回复）
            try {
                TrainingChatRecord aiReplyMessage = TrainingChatRecord.builder()
                        .sessionId(sessionId)
                        .roundNum(session.getTotalRounds())
                        .contentType("TEXT")
                        .content(childReply)
                        .aiReply(true)
                        //.emotionAnalysis((Map<String, Object>) emotionAnalysisObj)
                        .emotionAnalysis(emotionAnalysisJson)
                        .aiGuidance(aiGuidance != null ? aiGuidance : "")
                        .build();

                trainingChatRecordService.save(aiReplyMessage);
                logger.info("保存AI回复消息成功，记录ID: {}", aiReplyMessage.getId());

                // 6. 更新会话的总轮次
                if (session != null) {
                    session.setTotalRounds(session.getTotalRounds()+2);
                    trainingSessionService.updateById(session);
                    logger.info("更新会话轮次成功，新轮次: {}", session.getTotalRounds());
                } else {
                    logger.error("会话对象为null，无法更新轮次");
                }
            } catch (Exception e) {
                logger.error("保存AI回复消息或更新会话轮次异常: {}", e.getMessage(), e);
                // 继续执行，确保返回响应给客户端
            }

            // 7. 构建返回的VO对象
            TrainingChatResponseVO responseVO = new TrainingChatResponseVO();
            responseVO.setSessionId(sessionId);
            responseVO.setChildReply(childReply);
            responseVO.setEmotionAnalysis(emotionAnalysisJson);
            responseVO.setAiGuidance(aiGuidance);
            responseVO.setTimestamp(LocalDateTime.now());

            return Result.success(responseVO);
        } catch (Exception e) {
            logger.error("发送训练消息异常: {}", e.getMessage());
            return Result.error("发送消息失败，请重试");
        }
    }

    /**
     * 获取模拟儿童回复
     */
    private String getChildReply(String workerMessage, List<ChatMessage> history) {
        try {

            // 设置系统提示词，引导模型模拟儿童语气和行为
            String systemPrompt = "你现在需要模拟一个留守儿童的角色，根据社工的对话内容进行回复。" + //TODO 从数据库中获取 模拟儿童的提示词
                    "请用儿童的语气，简单、直接、真实，避免复杂的句子和词汇。" +
                    "结合你作为留守儿童的背景，表现出相应的情绪和需求。";
            // 添加当前用户消息
            ChatMessage userMessage = ChatMessage.builder()
                    .role("user")
                    .content(workerMessage)
                    .build();
            history.add(userMessage);

            // 构建聊天请求
            ChatRequest chatRequest = ChatRequest.builder()
                    .prompt(workerMessage)
                    .history(history)
                    .build();

            // 调用ArkUtil获取儿童回复（修改系统消息）
            ChatContent chatContent = arkUtil.botChat(chatRequest, systemPrompt);
            return chatContent.getContent();
        } catch (Exception e) {
            logger.error("获取儿童回复异常: {}", e.getMessage());
            // 返回默认回复
            return "我知道了...";
        }
    }

    /**
     * 获取情感分析结果
     */
    private String getEmotionAnalysis(String childReply, String workerMessage, List<ChatMessage> history) {
        try {
            // 设置系统提示词，引导模型生成结构化的情感分析结果
            String systemPrompt = "你需要对儿童的回复进行情感分析，按照指定的JSON格式输出结果。" +
                    "格式要求：{\"detected_emotions\": [{\"emotion\": \"情绪名称\", \"confidence\": 置信度}, ...], \"emotion_intensity\": 情绪强度}。" +
                    "情绪名称可以是：开心、伤心、孤独、焦虑、生气、害怕、平静等。" +
                    "置信度范围是0-100的整数。" +
                    "情绪强度是0-100的整数，表示整体情绪的强烈程度。" +
                    "请只输出JSON格式的结果，不要添加其他说明文字。";

            // 添加当前用户消息
            ChatMessage userMessage = ChatMessage.builder()
                    .role("user")
                    .content(workerMessage)
                    .build();
            history.add(userMessage);

            // 添加当前AI回复消息
            ChatMessage aiMessage = ChatMessage.builder()
                    .role("ai")
                    .content(childReply)
                    .build();
            history.add(aiMessage);

            // 构建聊天请求
            ChatRequest chatRequest = ChatRequest.builder()
                    .prompt(systemPrompt)
                    .history(history)
                    .build();

            // 调用ArkUtil获取情感分析结果
            ChatContent chatContent = arkUtil.botChat(chatRequest,systemPrompt);
            return chatContent.getContent();
        } catch (Exception e) {
            logger.error("获取情感分析异常: {}", e.getMessage());
            // 返回默认的情感分析结果
            return "{\"detected_emotions\": [{\"emotion\": \"平静\", \"confidence\": 50}], \"emotion_intensity\": 50}";
        }
    }

    /**
     * 获取AI指导意见
     */
    private String getAiGuidance(String childReply, String emotionAnalysisJson, String workerMessage, List<ChatMessage> history) {
        try {
            // 设置系统提示词，引导模型生成社工指导意见
            String systemPrompt = "你是一名专业的儿童心理辅导专家，需要根据社工和儿童的对话，为社工提供指导意见。" +
                    "请分析儿童的情绪和需求，给出具体、实用的建议，帮助社工更好地与儿童沟通和提供帮助。" +
                    "建议要温暖、支持性，符合儿童心理发展特点。回复需要简短，直至要点。";

            // 添加当前用户消息
            ChatMessage userMessage = ChatMessage.builder()
                    .role("user")
                    .content(workerMessage)
                    .build();
            history.add(userMessage);

            // 添加当前AI回复消息
            ChatMessage aiMessage = ChatMessage.builder()
                    .role("ai")
                    .content(childReply)
                    .build();
            history.add(aiMessage);

            // 添加情感分析结果消息
            ChatMessage emotionMessage = ChatMessage.builder()
                    .role("user")
                    .content(emotionAnalysisJson)
                    .build();
            history.add(emotionMessage);

            // 构建聊天请求
            String prompt = "现在输出你对社工的指导意见：" + childReply;
            ChatRequest chatRequest = ChatRequest.builder()
                    .prompt(prompt)
                    .history(history)
                    .build();

            // 调用ArkUtil获取指导意见
            ChatContent chatContent = arkUtil.botChat(chatRequest, systemPrompt);
            return chatContent.getContent();
        } catch (Exception e) {
            logger.error("获取AI指导意见异常: {}", e.getMessage());
            // 返回默认指导意见
            return "建议社工继续保持耐心倾听，给予儿童更多的情感支持和鼓励。";
        }
    }

    /**
     * 获取训练会话记录
     * @param sessionId 会话ID
     * @return 会话记录列表
     */
    @GetMapping("/history/{sessionId}")
    @Operation(summary = "获取训练会话记录", description = "获取指定训练会话的完整聊天记录")
    public Result<List<ChatMessageVO>> getTrainingSessionHistory(@PathVariable String sessionId) {
        // 暂时返回成功，不实现具体业务逻辑
        return Result.success();
    }

    /**
     * 结束训练会话
     * @param sessionId 会话ID
     * @param endTrainingSessionDTO 结束训练会话DTO
     * @return 训练评估VO
     */
    @PostMapping("/end/{sessionId}")
    @Operation(summary = "结束训练会话", description = "结束训练会话并获取总结评估")
    public Result<TrainingEvaluationVO> endTrainingSession(@PathVariable String sessionId,
                                                           @RequestBody EndTrainingSessionDTO endTrainingSessionDTO) {
        try {
            logger.info("开始结束训练会话并获取评估，会话ID: {}", sessionId);

            // 转换sessionId为Long类型
            Long sessionIdLong = Long.parseLong(sessionId);

            // 查询训练会话
            TrainingSession trainingSession = trainingSessionService.getById(sessionIdLong);
            if (trainingSession == null) {
                logger.error("未找到训练会话，会话ID: {}", sessionId);
                return Result.error("未找到训练会话");
            }

            // 更新会话状态和结束时间
            trainingSession.setSessionStatus("COMPLETED");
            trainingSession.setEndTime(LocalDateTime.now());



            log.info("更新会话表：{}", trainingSession);

            // 保存更新
            boolean updateResult = trainingSessionService.updateById(trainingSession);
            if (!updateResult) {
                logger.error("训练会话状态更新失败，会话ID: {}", sessionId);
                return Result.error("结束训练会话失败");
            }

            // 查询该会话的所有聊天记录
            List<TrainingChatRecord> chatRecords = trainingChatRecordService.lambdaQuery()
                    .eq(TrainingChatRecord::getSessionId, sessionIdLong)
                    .orderByAsc(TrainingChatRecord::getRoundNum)
                    .list();

            // 创建历史对话列表
            List<ChatMessage> historyMessages = new ArrayList<>();
            for (TrainingChatRecord record : chatRecords) {
                ChatMessage message = ChatMessage.builder()
                        .role(record.getAiReply() ? "ai" : "user")
                        .content(record.getContent())
                        .build();
                historyMessages.add(message);
            }


            // 发起5轮AI请求获取评分和评价
            // 1. 共情能力评分
            BigDecimal empathyScore = getScoreFromAi("请为这段对话中的社工的共情能力进行评分，满分100分，只需要返回数字。", historyMessages);

            // 2. 沟通技巧评分
            BigDecimal communicationScore = getScoreFromAi("请为这段对话中的社工的沟通技巧进行评分，满分100分，只需要返回数字。", historyMessages);

            // 3. 问题解决能力评分
            BigDecimal problemSolvingScore = getScoreFromAi("请为这段对话中的社工的问题解决能力进行评分，满分100分，只需要返回数字。", historyMessages);

            // 4. 情感识别能力评分
            BigDecimal emotionalRecognitionScore = getScoreFromAi("请为这段对话中的社工的情感识别能力进行评分，满分100分，只需要返回数字。", historyMessages);

            // 5. 综合评价
            ChatContent comprehensiveCommentContent = getComprehensiveCommentFromAi("请为这段对话中的社工表现给出综合评价，概述有点和不足和改进方向,要求简短回复。", historyMessages);

            // 创建并保存训练评估记录
            TrainingEvaluation evaluation = new TrainingEvaluation();
            evaluation.setSessionId(sessionIdLong);// 会话ID
            evaluation.setEmpathyScore(empathyScore);// 共情能力评分
            evaluation.setCommunicationScore(communicationScore);// 沟通技巧评分
            evaluation.setProblemSolvingScore(problemSolvingScore);// 问题解决能力评分
            evaluation.setEmotionalRecognitionScore(emotionalRecognitionScore);// 情感识别能力评分

            evaluation.setStrengths(comprehensiveCommentContent.getContent());// 综合评价

            // 保存评估结果
            boolean saveResult = false;
            try {
                saveResult = trainingEvaluationService.save(evaluation);
            } catch (Exception e) {
                logger.error("训练评估结果保存异常: {}", e.getMessage());
            }

            if (!saveResult) {
                logger.error("训练评估结果保存失败，会话ID: {}", sessionId);
                return Result.error("训练评估失败");
            }

            // 转换为VO返回
            TrainingEvaluationVO evaluationVO = new TrainingEvaluationVO();
            BeanUtils.copyProperties(evaluation, evaluationVO);

            logger.info("训练会话结束并评估成功，会话ID: {}", sessionId);
            return Result.success(evaluationVO);
        } catch (NumberFormatException e) {
            logger.error("会话ID格式错误: {}", e.getMessage());
            return Result.error("会话ID格式错误");
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "未知异常";
                if (e.getCause() != null) {
                    errorMessage += ", 原因: " + e.getCause().getMessage();
                }
            }
            logger.error("结束训练会话异常: {}", errorMessage);
            return Result.error("结束训练会话异常");
        }
    }

    /**
     * 从AI获取评分
     */
    private BigDecimal getScoreFromAi(String prompt, List<com.caresync.ai.model.ai.ChatMessage> history) {
        try {
            String systemPrompt = "你是一个专业的社工，负责为其他社工提供评分。";

            ChatRequest request = ChatRequest.builder()
                    .prompt(prompt)
                    .history(history)
                    .build();

            ChatContent content = arkUtil.botChat(request, systemPrompt);
            logger.info("AI评分: {}", content.getContent());

            if (content == null || content.getContent() == null) {
                logger.warn("AI评分内容为空，使用默认分数");
                return BigDecimal.ZERO;
            }
            String scoreStr = content.getContent().trim();
            // 提取数字
            scoreStr = scoreStr.replaceAll("[^0-9.]", "");
            if (scoreStr.isEmpty()) {
                logger.warn("无法从AI响应中提取分数，使用默认分数");
                return BigDecimal.ZERO;
            }
            return new BigDecimal(scoreStr);
        } catch (Exception e) {
            logger.error("获取AI评分失败: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 从AI获取综合评价
     */
    private ChatContent getComprehensiveCommentFromAi(String prompt, List<com.caresync.ai.model.ai.ChatMessage> history) {
        try {
            String systemPrompt = "你是一个专业的社工，负责为其他社工提供综合评价。";

            ChatRequest request = ChatRequest.builder()
                    .prompt(prompt)
                    .history(history)
                    .build();
            arkUtil.botChat(request, systemPrompt);

            ChatContent chatContent = arkUtil.botChat(request, systemPrompt);
            logger.info("AI综合评价请求: {}", chatContent.getContent());

            return chatContent;
        } catch (Exception e) {
            logger.error("获取AI综合评价失败: {}", e.getMessage());
            // 创建默认的评价内容，避免空指针异常
            ChatContent defaultContent = new ChatContent();
            defaultContent.setContent("该会话未生成综合评价，可能是由于AI服务不可用或会话内容不足。");
            return defaultContent;
        }
    }
}