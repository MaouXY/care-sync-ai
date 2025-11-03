package com.caresync.ai.model.DTO;

import lombok.Data;

/**
 * 社工登录请求参数
 */
@Data
public class SocialWorkerLoginDTO {
    private String workerNo; // 社工账号
    private String password; // 密码
}