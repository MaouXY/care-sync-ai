package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社工信息响应结果
 */
@Data
public class SocialWorkerInfoVO implements Serializable {
    private Long id;
    private String workerNo;
    private String name;
    private String phone;
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}