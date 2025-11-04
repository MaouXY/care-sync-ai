-- PostgreSQL数据库初始化脚本
-- 适用于care-sync-ai项目

-- 1. 创建数据库
CREATE DATABASE care_sync_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Chinese (Simplified)_China.936'
    LC_CTYPE = 'Chinese (Simplified)_China.936'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- 2.使用数据库care_sync_db


-- 3. 创建儿童表（child）
CREATE TABLE child (
                       id BIGSERIAL PRIMARY KEY, -- 儿童唯一ID（自增）
                       child_no VARCHAR(20) NOT NULL UNIQUE, -- 儿童登录ID（社工预先创建）
                       -- TODO 添加绑定的社会工作者ID（外键关联social_workers表），作为服务状态（默认NULL，开始后绑定），带服务（未指定服务社工）、服务中（指定服务社工）、完成（完成服务，可能是成年、被领养）
                       name VARCHAR(50) NOT NULL, -- 儿童姓名
                       age INT NOT NULL CHECK (age > 0), -- 儿童年龄
                       verify_code VARCHAR(4) NOT NULL, -- 4位登录验证码（BCrypt加密存储）
                       has_new_chat BOOLEAN DEFAULT FALSE, -- 是否有新聊天记录（社工端列表标注）
                       ai_struct_info JSONB, -- AI结构化信息：情感趋势、核心需求等
                       ai_analysis_time TIMESTAMP, -- 最后一次AI分析时间（避免重复分析）
                       interest_tags JSONB, -- 预留：兴趣标签（如["童话","绘画"]）
                       study_situation JSONB, -- 预留：学习情况（如{"math":"良好"}）
                       create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 索引：优化登录查询与新消息筛选
CREATE INDEX idx_child_child_no ON child(child_no);
CREATE INDEX idx_child_has_new_chat ON child(has_new_chat);
-- ai_struct_info扩展结构示例（适配AI分析结果展示页）TODO 去除 分析类型 分析状态 改成显示 potential_problems 和 emotion_trend
-- {
--   "emotion_trend": ["孤独", "平静", "开心"], -- 情感趋势标签（对应图表X轴）TODO 在儿童列表-就展示，但现在并未使用
--   "core_needs": ["故事陪伴", "学习辅导"], -- 核心需求标签（展示在摘要卡片）TODO 在儿童详情列表-就展示，但现在并未使用
--   "potential_problems": "沟通较少", -- 潜在问题标签（红色预警展示）TODO 在儿童列-就展示，但现在并未使用
--   "description": "该儿童存在孤独感，沟通较少，核心需求为故事陪伴, 学习辅导", -- 分析摘要（综合描述）
--   "latest_analysis": "2023-07-15 15:30:00", -- 最新分析时间（页面右上角标注）
--   "emotion_scores": { -- 情感评分指标（雷达图数据）
--     "情绪稳定性": 75,
--     "焦虑水平": 35,
--     "幸福感": 65,
--     "社交自信": 45
--   },
--   "emotion_history": [ -- 情感历史记录（趋势图数据）
--     {"date": "2023-07-15", "scores": {"情绪稳定性": 75, "焦虑水平": 35, "幸福感": 65, "社交自信": 45}},
--     {"date": "2023-07-08", "scores": {"情绪稳定性": 60, "焦虑水平": 45, "幸福感": 60, "社交自信": 45}},
--     {"date": "2023-07-01", "scores": {"情绪稳定性": 55, "焦虑水平": 50, "幸福感": 55, "社交自信": 40}}
--   ],
--   "key_findings": [ -- 关键发现列表（分析结果详情）
--     "情绪稳定性较上周提升了15%，表现出更好的情绪调节能力。",
--     "焦虑水平有所下降，但在提及学校作业时仍表现出一定压力。",
--     "社交互动中的自信心仍然不足，需要更多的鼓励和支持。",
--     "与AI助手的互动频率增加，表明他对这种交流方式感到舒适。"
--   ],
--   "recommendations": [ -- 建议列表（分析结果详情->服务计划生成依据）
--     {
--       "title": "继续保持与小明的定期沟通",
--       "description": "每周安排1-2次简短的交流，关注他的日常感受和需求。",
--       "priority": "high" -- 优先级（影响展示排序）？？？
--     },
--     {
--       "title": "开展自信心提升活动",
--       "description": "设计一些小明擅长的活动，通过成功体验增强他的自信心。",
--       "priority": "medium"
--     },
--     {
--       "title": "与学校老师保持沟通",
--       "description": "了解小明在学校的表现，共同制定支持计划。"
--     },
--     {
--       "title": "提供情绪管理技巧指导",
--       "description": "教导小明一些简单的情绪调节方法，帮助他应对压力情境。"
--     }
--   ]
-- }

-- interest_tags结构示例
-- ["童话", "绘画", "科学实验", "运动"]

-- study_situation结构示例
-- {
--   "math": "良好",
--   "chinese": "中等",
--   "english": "需要提高",
--   "homework_status": "按时完成",
--   "teacher_comments": "上课注意力有时不集中"
-- }

-- 4. 创建AI聊天记录表（ai_chat_record）
CREATE TABLE ai_chat_record (
                                id BIGSERIAL PRIMARY KEY,
                                child_id BIGINT NOT NULL, -- 关联儿童ID
                                session_id VARCHAR(50) NOT NULL, -- 业务会话ID（儿童与AI的聊天会话）
                                digi_session_id VARCHAR(50) NOT NULL, -- livetalking生成的数字人Session ID
                                round_num INT NOT NULL, -- 会话内轮次（1、2、3...）
                                content_type VARCHAR(10) NOT NULL CHECK (content_type IN ('VOICE', 'TEXT')), -- 内容类型
                                content TEXT NOT NULL, -- 聊天内容（语音转文字后/文本直接存储）
                                is_ai_reply BOOLEAN NOT NULL, -- 是否AI回复（TRUE=AI，FALSE=儿童）
                                is_filtered BOOLEAN DEFAULT FALSE, -- 内容过滤状态
                                emotion_tag VARCHAR(20), -- 单轮情感标签（如"开心""低落"）
                                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 外键：删除儿童时同步删除聊天记录
                                FOREIGN KEY (child_id) REFERENCES child(id) ON DELETE CASCADE
);
-- 索引优化：按数字人Session查询关联聊天、按业务会话查询完整对话
CREATE INDEX idx_chat_digi_session ON ai_chat_record(digi_session_id);
CREATE INDEX idx_chat_session ON ai_chat_record(session_id);
CREATE INDEX idx_chat_child_time ON ai_chat_record(child_id, create_time);

-- 5. 创建AI服务方案表（ai_assist_scheme）
CREATE TABLE ai_assist_scheme (
                                  id BIGSERIAL PRIMARY KEY, -- 方案ID
                                  child_id BIGINT NOT NULL, -- 关联儿童ID
                                  worker_id BIGINT NOT NULL, -- 关联社工ID
                                  target VARCHAR(200) NOT NULL, -- 服务目标（如"缓解孤独感"）
                                  measures TEXT[] NOT NULL CHECK (array_length(measures, 1) <= 3), -- 措施（最多3条）
                                  cycle INT DEFAULT 7, -- 周期（默认1周）
                                  -- TODO 需要添加项目是否开始的功能，默认草稿状态，开始后状态变更为IN_PROGRESS并在跟踪界面显示，完成后变更为COMPLETED，我觉得还需要专门添加一个项目跟踪详情页面，并且增加一个项目进度详情表和子任务完成情况表，记录每个具体措施的完成，作为在儿童界面显示的最近互动记录，和
                                  scheme_status VARCHAR(20) DEFAULT 'DRAFT' CHECK (scheme_status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED')), -- 方案状态
                                  ai_suggestions JSONB NOT NULL, -- AI原始建议（含目标、措施、依据）
                                  worker_adjust_reason TEXT, -- 社工调整理由
                                  ai_analysis_id BIGINT, -- 关联AI分析记录ID
                                  evaluation_index JSONB, -- 预留：评估指标
                                  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 外键关联
                                  FOREIGN KEY (child_id) REFERENCES child(id) ON DELETE CASCADE,
                                  FOREIGN KEY (worker_id) REFERENCES social_worker(id) ON DELETE CASCADE,
                                  FOREIGN KEY (ai_analysis_id) REFERENCES ai_analysis_log(id) ON DELETE SET NULL
);
-- 索引：优化方案查询与状态筛选
CREATE INDEX idx_scheme_child_worker ON ai_assist_scheme(child_id, worker_id);
CREATE INDEX idx_scheme_status ON ai_assist_scheme(scheme_status);
-- ai_suggestions结构示例（适配服务计划页面）
-- {
--   "target_suggest": ["缓解孤独感", "提升社交自信"], -- 建议目标（与风险等级联动）
--   "measures_suggest": [ -- 建议措施（最多3条，适配计划列表）
--     "每周3次AI故事推送（数字人播报，偏好童话类）",
--     "设计互动游戏提升社交技能",
--     "定期安排线上互动活动"
--   ],
--     TODO  方案目标  服务措施（一级条目（一周时间），二级条目（具体措施，））
-- }

-- evaluation_index结构示例（适配进度跟踪页面）
-- {
--   "key_indicators": [ -- 关键指标（进度条展示）
--     {"name": "情绪稳定性", "target_value": 80, "current_value": 65, "trend": "up"},
--     {"name": "社交参与度", "target_value": 75, "current_value": 50, "trend": "stable"}
--   ],
--   "milestones": [ -- 里程碑（时间线展示）
--     {"week": 1, "description": "建立信任关系，每周至少2次有效沟通", "status": "completed"},
--     {"week": 4, "description": "情绪稳定性提升10%以上", "status": "pending"}
--   ]
-- }

-- 6. 创建AI服务进度日志表（assist_track_log）
CREATE TABLE assist_track_log (
                                  id BIGSERIAL PRIMARY KEY, -- 日志ID
                                  scheme_id BIGINT NOT NULL, -- 关联服务方案ID
                                  worker_id BIGINT NOT NULL, -- 关联社工ID（记录人）
                                  week INT NOT NULL, -- 服务周次（如"第1周""第2周"）
                                  completion_status VARCHAR(20) NOT NULL, -- 完成状态：COMPLETED（已完成）/UNFINISHED（未完成）
                                  record_content TEXT NOT NULL, -- 记录内容（如"已完成1次电话沟通"）
                                  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 外键关联：删除方案时同步删除日志
                                  FOREIGN KEY (scheme_id) REFERENCES ai_assist_scheme(id) ON DELETE CASCADE,
                                  FOREIGN KEY (worker_id) REFERENCES social_worker(id) ON DELETE CASCADE
);
-- 索引：优化社工查询方案进度、周次筛选
CREATE INDEX idx_track_scheme_id ON assist_track_log(scheme_id);
CREATE INDEX idx_track_week ON assist_track_log(week);

-- 7. 创建AI分析记录表（ai_analysis_log）
CREATE TABLE ai_analysis_log (
                                 id BIGSERIAL PRIMARY KEY,
                                 child_id BIGINT NOT NULL,
                                 session_ids TEXT[], -- 本次分析涉及的会话ID列表
                                 analysis_result JSONB NOT NULL, -- 完整分析结果（与child.ai_struct_info关联）
                                 trigger_type VARCHAR(20) NOT NULL, -- 触发方式：AUTO（定时）/MANUAL（社工手动触发）
                                 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                 FOREIGN KEY (child_id) REFERENCES child(id) ON DELETE CASCADE
);
-- 索引：按儿童查询分析历史
CREATE INDEX idx_analysis_child ON ai_analysis_log(child_id);
-- analysis_result 结构与 ai_struct_info 一致

-- 8. 创建社工表（social_worker）
CREATE TABLE social_worker (
                               id BIGSERIAL PRIMARY KEY, -- 社工唯一ID
                               worker_no VARCHAR(20) NOT NULL UNIQUE, -- 社工账号
                               password VARCHAR(100) NOT NULL, -- 密码（BCrypt加密存储）
                               name VARCHAR(50) NOT NULL, -- 社工姓名
                               phone VARCHAR(20) NOT NULL, -- 社工联系电话（用于儿童端紧急呼叫）
                               role VARCHAR(20) DEFAULT 'NORMAL', -- 角色：NORMAL（普通社工）/ADMIN（管理员，预留）
                               create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 索引：优化社工登录查询
CREATE INDEX idx_social_worker_worker_no ON social_worker(worker_no);

-- 9. 创建模拟训练场景表（simulation_scenario）
CREATE TABLE simulation_scenario (
                                     id BIGSERIAL PRIMARY KEY, -- 场景ID
                                     scenario_name VARCHAR(100) NOT NULL, -- 场景名称（如"留守儿童表达孤独"）
                                     scenario_type VARCHAR(50) NOT NULL, -- 场景类型（如"情感表达"、"安全意识"）
                                     description TEXT NOT NULL, -- 场景描述
                                     difficulty_level INT DEFAULT 2 CHECK (difficulty_level BETWEEN 1 AND 5), -- 难度等级（1-5）
                                     target_skill VARCHAR(100) NOT NULL, -- 目标技能（如"共情能力"）
                                     ai_personality_template JSONB NOT NULL, -- AI个性模板（模拟儿童的性格特征）
                                     is_public BOOLEAN DEFAULT TRUE, -- 是否公开场景
                                     created_by BIGINT, -- 创建者ID（关联社工表）
                                     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 外键关联：创建者（社工）
                                     FOREIGN KEY (created_by) REFERENCES social_worker(id) ON DELETE SET NULL
);
-- 索引：优化场景查询
CREATE INDEX idx_scenario_type ON simulation_scenario(scenario_type);
CREATE INDEX idx_scenario_name ON simulation_scenario(scenario_name);
-- ai_personality_template结构示例（适配模拟训练界面）
-- {
--   "age": 10, -- 模拟儿童年龄（显示在会话头部）
--   "gender": "female", -- 性别（影响头像选择）
--   "personality_traits": ["内向", "敏感", "想象力丰富"], -- 性格特征（场景说明）
--   "speech_pattern": "简短，有时会重复关键词", -- 说话模式（AI生成内容风格）
--   "emotional_state": "孤独，渴望关注", -- 情绪状态（与风险标签联动）
--   "background_story": "父母外出务工，与奶奶一起生活，很少有机会与人交流", -- 背景故事（场景介绍）
--   "interests": ["画画", "听故事"], -- 兴趣爱好（话题引导建议）
--   "fears": ["孤独", "黑暗"], -- 恐惧点（AI情绪触发点）
--   "communication_preferences": { -- 沟通偏好（训练提示）
--     "preferred_topics": ["童话故事", "小动物", "学校生活"],
--     "avoid_topics": ["父母离开", "批评"],
--     "response_style": "需要引导，安全感建立后才会多说话"
--   }
-- }

-- 10. 创建训练会话表（training_session）
CREATE TABLE training_session (
                                  id BIGSERIAL PRIMARY KEY, -- 会话ID
                                  worker_id BIGINT NOT NULL, -- 社工ID
                                  scenario_id BIGINT NOT NULL, -- 场景ID
                                  session_status VARCHAR(20) DEFAULT 'IN_PROGRESS' CHECK (session_status IN ('IN_PROGRESS', 'COMPLETED', 'INTERRUPTED')), -- 会话状态
                                  start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 开始时间
                                  end_time TIMESTAMP, -- 结束时间
                                  total_rounds INT DEFAULT 0, -- 总对话轮次
                                  overall_score DECIMAL(3,1), -- 总体评分（0-10分）
                                  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 外键关联
                                  FOREIGN KEY (worker_id) REFERENCES social_worker(id) ON DELETE CASCADE,
                                  FOREIGN KEY (scenario_id) REFERENCES simulation_scenario(id) ON DELETE CASCADE
);
-- 索引：优化会话查询
CREATE INDEX idx_session_worker ON training_session(worker_id);
CREATE INDEX idx_session_status ON training_session(session_status);

-- 11. 创建训练聊天记录表（training_chat_record）TODO AI简单！指导意见
CREATE TABLE training_chat_record (
                                      id BIGSERIAL PRIMARY KEY, -- 记录ID
                                      session_id BIGINT NOT NULL, -- 训练会话ID
                                      round_num INT NOT NULL, -- 对话轮次
                                      content_type VARCHAR(10) NOT NULL CHECK (content_type IN ('TEXT')), -- 内容类型
                                      content TEXT NOT NULL, -- 聊天内容
                                      is_ai_reply BOOLEAN NOT NULL, -- 是否AI回复（TRUE=AI模拟儿童，FALSE=社工）
                                      emotion_analysis JSONB, -- 情感分析结果
                                      ai_guidance TEXT, -- AI对社工回应的指导意见
                                      create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 外键关联：删除会话时同步删除聊天记录
                                      FOREIGN KEY (session_id) REFERENCES training_session(id) ON DELETE CASCADE
);
-- 索引：优化会话内聊天记录查询
CREATE INDEX idx_training_chat_session ON training_chat_record(session_id);
CREATE INDEX idx_training_chat_round ON training_chat_record(session_id, round_num);
-- emotion_analysis结构示例（适配训练指导区域）
-- {
--   "detected_emotions": [ -- 检测到的情绪（与情感标签对应）
--     {"emotion": "孤独", "confidence": 85},
--     {"emotion": "焦虑", "confidence": 62}
--   ],
--   "emotion_intensity": 75, -- 情绪强度（进度条展示）
-- }

-- 12. 创建训练评估表（training_evaluation）
CREATE TABLE training_evaluation (
                                     id BIGSERIAL PRIMARY KEY, -- 评估ID
                                     session_id BIGINT NOT NULL, -- 训练会话ID
                                     empathy_score DECIMAL(3,1), -- 共情能力评分
                                     communication_score DECIMAL(3,1), -- 沟通技巧评分
                                     problem_solving_score DECIMAL(3,1), -- 问题解决能力评分
                                     emotional_recognition_score DECIMAL(3,1), -- 情感识别能力评分
                                     strengths TEXT, -- 优势
                                     areas_for_improvement TEXT, -- 改进点
                                     ai_comprehensive_comment TEXT, -- AI综合评价
                                     create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 外键关联：删除会话时同步删除评估
                                     FOREIGN KEY (session_id) REFERENCES training_session(id) ON DELETE CASCADE
);
-- 索引：优化会话评估查询
CREATE INDEX idx_evaluation_session ON training_evaluation(session_id);









