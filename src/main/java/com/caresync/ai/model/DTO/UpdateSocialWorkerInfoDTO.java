package com.caresync.ai.model.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新社工信息请求参数
 */
@Data
public class UpdateSocialWorkerInfoDTO implements Serializable {
    private Long id;
    private String name;
    private String phone;
}