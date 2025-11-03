package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 儿童列表查询请求参数
 */
@Data
public class ChildQueryDTO {
    private String childNo; // 儿童登录ID（模糊查询）
    private String name; // 儿童姓名（模糊查询）
    private Integer minAge; // 最小年龄
    private Integer maxAge; // 最大年龄
    private Boolean hasNewChat; // 是否有新聊天记录
    private Integer page; // 页码
    private Integer pageSize; // 每页条数
}