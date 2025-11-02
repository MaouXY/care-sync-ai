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

-- 5. 创建AI帮扶方案表（ai_assist_scheme）
CREATE TABLE ai_assist_scheme (
                                  id BIGSERIAL PRIMARY KEY, -- 方案ID
                                  child_id BIGINT NOT NULL, -- 关联儿童ID
                                  worker_id BIGINT NOT NULL, -- 关联社工ID
                                  target VARCHAR(200) NOT NULL, -- 帮扶目标（如"缓解孤独感"）
                                  measures TEXT[] NOT NULL CHECK (array_length(measures, 1) <= 3), -- 措施（最多3条）
                                  cycle INT DEFAULT 7, -- 周期（默认1周）
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

-- 6. 创建AI帮扶进度日志表（assist_track_log）
CREATE TABLE assist_track_log (
                                  id BIGSERIAL PRIMARY KEY, -- 日志ID
                                  scheme_id BIGINT NOT NULL, -- 关联帮扶方案ID
                                  worker_id BIGINT NOT NULL, -- 关联社工ID（记录人）
                                  week INT NOT NULL, -- 帮扶周次（如"第1周""第2周"）
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