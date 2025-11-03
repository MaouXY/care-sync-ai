package com.caresync.ai.model.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新儿童信息请求参数
 */
@Data
public class UpdateChildInfoDTO implements Serializable {
    private Long id;
    private String name;
    private Integer age;
    private Object interestTags;
    private Object studySituation;
}