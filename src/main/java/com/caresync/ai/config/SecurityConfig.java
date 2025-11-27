package com.caresync.ai.config;

import com.caresync.ai.result.Result;
import com.caresync.ai.security.JwtAuthenticationFilter;
import com.caresync.ai.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护，适用于API服务
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置URL访问权限
                .authorizeHttpRequests(authorize -> authorize
                        // 允许公开访问的路径
                        .requestMatchers("/user/register", "/api/social-worker/login", "/api/social-worker/logout","/api/child/login", "/error","/common/upload","/api/judge0/webhook/callback").permitAll()
                        // 允许Knife4J相关路径访问
                        .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/swagger-resources/**").permitAll()
                        // 允许静态资源访问
                        .requestMatchers("/static/**", "/resources/**").permitAll()
                        // 所有其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 禁用默认的登录表单，因为我们使用JWT认证
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用HTTP Basic认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 配置异常处理
                .exceptionHandling(exception -> exception
                        // 处理认证异常（401）
                        .authenticationEntryPoint(new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
                                // 这个异常处理通常不会被触发，因为JWT过滤器已经处理了401
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write(JsonUtil.toJsonString(Result.error("UNAUTHORIZED"+ "认证失败")));
                            }
                        })
                        // 处理权限不足异常（403）
                        .accessDeniedHandler(new AccessDeniedHandler() {
                            @Override
                            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
                                log.warn("权限不足，拒绝访问: {}", request.getRequestURI());
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write(JsonUtil.toJsonString(Result.error("ACCESS_DENIED"+ "权限不足，无法访问该资源")));
                            }
                        })
                )
                // 添加JWT过滤器，在UsernamePasswordAuthenticationFilter之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // TODO 后续根据需求添加角色权限
    /*
    // 需要管理员权限
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<?> adminMethod() {
        // 管理员专属逻辑
    }

    // 需要社工权限
    @PreAuthorize("hasRole('ROLE_SOCIAL_WORKER')")
    public Result<?> socialWorkerMethod() {
        // 社工专属逻辑
    }

    // 需要儿童权限
    @PreAuthorize("hasRole('ROLE_CHILD')")
    public Result<?> childMethod() {
        // 儿童专属逻辑
    }

    // 多角色支持
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SOCIAL_WORKER')")
    @GetMapping("/admin-social-worker/data")
    public ResponseEntity<?> getDataForAdminAndSocialWorker() {
        // 管理员和社工都可访问的逻辑
    }
    */
}