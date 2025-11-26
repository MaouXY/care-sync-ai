package com.caresync.ai.config;

import com.caresync.ai.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                // 添加JWT过滤器，在UsernamePasswordAuthenticationFilter之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // TODO 后续根据需求添加角色权限
    /*// 需要管理员权限
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> adminMethod() { }

    // 需要社工权限
    @PreAuthorize("hasRole('SOCIAL_WORKER')")
    public Result<?> socialWorkerMethod() { }

    // 需要儿童权限
    @PreAuthorize("hasRole('CHILD')")
    public Result<?> childMethod() { }*/
}