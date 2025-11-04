package com.caresync.ai.model.json;

import lombok.Data;

/**
 * 学习情况（对应child表的study_situation字段）
 */
@Data
public class StudySituation {
    private String math; // 数学成绩
    private String chinese; // 语文成绩
    private String english; // 英语成绩
    private String homeworkStatus; // 作业完成情况
    private String teacherComments; // 老师评价
}

