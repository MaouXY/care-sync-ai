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

        // 1. 检查请求是否是公开路径，如果是则直接放行
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 从请求头中获取token
        String token = request.getHeader(jwtConfig.getHeader());
        log.info("从请求头获取到的token: {}", token);

        // 3. 如果token不为空，则尝试解析和验证
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
        } else {
            // 4. 如果token为空且不是公开访问路径，则返回401未授权错误
            if (!isPublicPath(requestURI)) {
                log.warn("请求路径需要认证，但未提供token: {}", requestURI);
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JsonUtil.toJsonString(Result.error("UNAUTHORIZED")));
                return;
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 检查请求路径是否是公开访问的路径
     * @param requestURI 请求路径
     * @return 是否是公开路径
     */
    private boolean isPublicPath(String requestURI) {
        // 这里定义所有不需要认证的公开路径
        return requestURI.equals("/user/register") || 
               requestURI.equals("/api/social-worker/login") || 
               requestURI.equals("/api/social-worker/logout") || 
               requestURI.equals("/api/child/login") || 
               requestURI.equals("/error") || 
               requestURI.equals("/common/upload") || 
               requestURI.equals("/api/judge0/webhook/callback") ||
               requestURI.startsWith("/doc.html") || 
               requestURI.startsWith("/swagger-ui/") || 
               requestURI.startsWith("/v3/api-docs/") || 
               requestURI.startsWith("/webjars/") || 
               requestURI.startsWith("/swagger-resources/") || 
               requestURI.startsWith("/static/") || 
               requestURI.startsWith("/resources/");
    }
}