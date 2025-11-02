package com.caresync.ai.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * 密码编码器，用于对密码进行加密存储和验证
 */
@Component
public class PasswordEncoderUtil {
    @Value("${password.secret}")
    private String secret;

    @Getter
    @Value("${password.default-password}")
    private String defaultPassword;

    /**
     * 对密码进行加密
     * @param password 原始密码
     * @return 加密后的密码
     */
    public String encode(String password) {
        return DigestUtils.md5DigestAsHex((password + secret).getBytes());
    }

    /**
     * 验证原始密码是否与加密后的密码匹配
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 如果匹配则返回true，否则返回false
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }

}
