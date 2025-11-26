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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.info("处理请求: {}", requestURI);

        // 从请求头中获取token
        String token = request.getHeader(jwtConfig.getHeader());
        log.info("从请求头获取到的token: {}", token);

        // 如果token不为空，则尝试解析和验证
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

                // 将用户ID和role存储到ThreadLocal中，保持原有逻辑兼容性
                BaseContext.setCurrentId(userId);
                BaseContext.setCurrentRole(role);

                // 构建Spring Security权限列表
                List<GrantedAuthority> authorities = new ArrayList<>();
                String roleName = getRoleNameByCode(role);
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

                // 创建身份认证对象并设置到SecurityContext中
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
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

    // 根据角色代码获取角色名称
    private String getRoleNameByCode(Integer roleCode) {
        switch (roleCode) {
            case 1: return "CHILD";
            case 2: return "SOCIAL_WORKER";
            case 3: return "ADMIN";
            default: return "UNKNOWN";
        }
    }
}