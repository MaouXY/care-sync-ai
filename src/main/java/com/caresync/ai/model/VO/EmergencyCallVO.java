package com.caresync.ai.model.VO;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 紧急呼叫记录VO
 */
@Data
public class EmergencyCallVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 紧急呼叫ID
     */
    private Long id;

    /**
     * 儿童ID
     */
    private Long childId;

    /**
     * 儿童姓名
     */
    private String childName;

    /**
     * 紧急呼叫原因
     */
    private String reason;

    /**
     * 呼叫时间
     */
    private LocalDateTime callTime;

    /**
     * 处理状态：PENDING(待处理), PROCESSING(处理中), PROCESSED(已处理)
     */
    private String status;

    /**
     * 处理社工ID
     */
    private Long handledWorkerId;

    /**
     * 处理社工姓名
     */
    private String handledWorkerName;

    /**
     * 处理时间
     */
    private LocalDateTime handledTime;

    /**
     * 处理备注
     */
    private String handledRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}