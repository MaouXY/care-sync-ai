package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 儿童列表分页响应结果
 */
@Data
public class ChildQueueVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String childNo;
    private String name;
    private Integer age;
    private Boolean hasNewChat;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}