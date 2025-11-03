package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 儿童信息响应结果
 */
@Data
public class ChildInfoVO implements Serializable {
    private Long id;
    private String childNo;
    private String name;
    private Integer age;
    private Boolean hasNewChat;
    private Object aiStructInfo;
    private LocalDateTime aiAnalysisTime;
    private Object interestTags;
    private Object studySituation;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}