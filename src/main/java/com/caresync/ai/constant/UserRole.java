package com.caresync.ai.constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public enum UserRole {
    ROLE_ADMIN("ADMIN", "管理员"),
    ROLE_SOCIAL_WORKER("SOCIAL_WORKER", "社工"),
    ROLE_CHILD("CHILD", "儿童");

    private String name;
    private String description;
    // 构造方法、getter等
}