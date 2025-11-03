package com.caresync.ai.utils;

import com.caresync.ai.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具类，用于生成和解析 JWT 令牌
 */
@Component
public class JwtUtil {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    /**
     * 生成JWT令牌
     * @param claims 载荷信息
     * @return JWT令牌
     */
    public String createJWT(Map<String, Object> claims) {
        // 生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // 设置过期时间
        Date exp = new Date(nowMillis + jwtConfig.getExpiration() * 1000);

        // 生成JWT令牌
        return Jwts.builder()
                // 设置唯一ID
                .setId(UUID.randomUUID().toString())
                // 设置签发时间
                .setIssuedAt(now)
                // 设置过期时间
                .setExpiration(exp)
                // 设置载荷信息
                .setClaims(claims)
                // 设置签名算法和密钥
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param token JWT令牌
     * @return 载荷信息
     */
    public Claims parseJWT(String token) {
        // 得到DefaultJwtParser
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

    /**
     * 检查JWT令牌是否过期
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isExpired(String token) {
        Claims claims = parseJWT(token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从JWT令牌中获取指定的载荷信息
     * @param token JWT令牌
     * @param claimName 载荷名称
     * @return 载荷值
     */
    public Object getClaim(String token, String claimName) {
        Claims claims = parseJWT(token);
        return claims.get(claimName);
    }

    /**
     * 生成JWT令牌（静态方法，便于直接调用）
     * @param secret 密钥
     * @param expiration 过期时间（毫秒）
     * @param claims 载荷信息
     * @return JWT令牌
     */
    public static String createJWT(String secret, long expiration, Map<String, Object> claims) {
        // 生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // 设置过期时间
        Date exp = new Date(nowMillis + expiration);

        // 生成JWT令牌
        return Jwts.builder()
                // 设置唯一ID
                .setId(UUID.randomUUID().toString())
                // 设置签发时间
                .setIssuedAt(now)
                // 设置过期时间
                .setExpiration(exp)
                // 设置载荷信息
                .setClaims(claims)
                // 设置签名算法和密钥
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    /**
     * 解析JWT令牌（静态方法，便于直接调用）
     * @param secret 密钥
     * @param token JWT令牌
     * @return 载荷信息
     */
    public static Claims parseJWT(String secret, String token) {
        // 得到DefaultJwtParser
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

    /**
     * 检查JWT令牌是否过期（静态方法，便于直接调用）
     * @param secret 密钥
     * @param token JWT令牌
     * @return 是否过期
     */
    public static boolean isExpired(String secret, String token) {
        Claims claims = parseJWT(secret, token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从JWT令牌中获取指定的载荷信息（静态方法，便于直接调用）
     * @param secret 密钥
     * @param token JWT令牌
     * @param claimName 载荷名称
     * @return 载荷值
     */
    public static Object getClaim(String secret, String token, String claimName) {
        Claims claims = parseJWT(secret, token);
        return claims.get(claimName);
    }
}