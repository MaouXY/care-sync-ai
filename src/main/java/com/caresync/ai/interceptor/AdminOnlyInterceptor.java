package com.caresync.ai.interceptor;

import com.caresync.ai.annotation.AdminOnly;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.result.Result;
import com.caresync.ai.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 管理员权限拦截器
 * 用于拦截带有@AdminOnly注解的接口，并进行权限校验
 */
@Component
@Slf4j
//@CrossOrigin
public class AdminOnlyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否是方法处理
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查方法上是否有@AdminOnly注解
        AdminOnly adminOnly = handlerMethod.getMethodAnnotation(AdminOnly.class);

        // 检查类上是否有@AdminOnly注解
        if (adminOnly == null) {
            adminOnly = handlerMethod.getBeanType().getAnnotation(AdminOnly.class);
        }

        // 如果有@AdminOnly注解，则进行权限校验
        if (adminOnly != null) {
            Integer role = BaseContext.getCurrentRole();
            log.info("判断权限：当前用户角色: {}", role);

            // 检查用户角色是否为管理员（根据User类中的定义，1-社工，3-管理员）
            if (role == null || (role != 2 && role != 3)) {
                log.warn("权限不足，当前用户角色: {}", role);
                sendErrorResponse(response, 406, "PERMISSION_DENIED");
                return false;
            }
        }

        return true;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtil.toJsonString(Result.error(message)));
    }
}
