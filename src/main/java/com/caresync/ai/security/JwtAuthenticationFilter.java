package com.caresync.ai.security;

import com.caresync.ai.config.JwtConfig;
import com.caresync.ai.constant.JwtClaimsConstant;
import com.caresync.ai.context.BaseContext;
import com.caresync.ai.result.Result;
import com.caresync.ai.utils.JsonUtil;
import com.caresync.ai.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 用于验证每个请求中的JWT令牌，并将用户信息设置到SecurityContext中
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 记录请求URI用于调试
        String requestURI = request.getRequestURI();
        //跳过knife4j的静态资源请求
        if (requestURI.startsWith("/webjars")||requestURI.startsWith("/favicon.ico")||requestURI.startsWith("/v3")) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("处理请求: {}", requestURI);

        // 1. 从请求头中获取token
        String token = request.getHeader(jwtConfig.getHeader());
        log.info("从请求头获取到的token: {}", token);

        // 2. 如果token不为空，则尝试解析和验证
        if (StringUtils.hasText(token)) {
            try {
                // 解析JWT令牌
                Claims claims = JwtUtil.parseJWT(jwtConfig.getSecret(), token);
                // 从claims中获取用户ID
                Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
                log.info("当前登录用户ID: {}", userId);
                // 从claims中获取用户角色
                Integer role = Integer.valueOf(claims.get(JwtClaimsConstant.USER_ROLE).toString());
                log.info("当前登录用户角色: {}", role);

                // 将用户ID和role存储到ThreadLocal中，保持原有逻辑
                BaseContext.setCurrentId(userId);
                BaseContext.setCurrentRole(role);

                // 创建身份认证对象并设置到SecurityContext中
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            } catch (JwtException e) {
                log.error("JWT令牌验证失败: {}", e.getMessage());
                // 设置响应状态码和错误信息
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JsonUtil.toJsonString(Result.error("INVALID_TOKEN")));
                return;
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}
