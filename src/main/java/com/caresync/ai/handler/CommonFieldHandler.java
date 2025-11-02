package com.caresync.ai.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.caresync.ai.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * 用于自动填充创建时间、更新时间、创建人ID、更新人ID等公共字段
 */
@Component
public class CommonFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 从BaseContext中获取当前登录用户的ID
        Long userId = BaseContext.getCurrentId();
        if(userId == null){
            userId = 0L;
        }
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 填充创建者ID
        this.strictInsertFill(metaObject, "createUserId", Long.class, userId);
        // 填充更新者ID（与创建者ID相同）
        this.strictInsertFill(metaObject, "updateUserId", Long.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 从BaseContext中获取当前登录用户的ID
        Long userId = BaseContext.getCurrentId();
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 填充更新人ID
        this.strictUpdateFill(metaObject, "updateUserId", Long.class, userId);
    }


}
