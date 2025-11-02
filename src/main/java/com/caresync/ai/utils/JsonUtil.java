package com.caresync.ai.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * JSON工具类，封装Jackson的常用操作
 * 提供对象与JSON字符串之间的相互转换功能
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * Jackson的核心对象映射器
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 设置时区为东八区
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 配置其他可能需要的属性
        // objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true); // 格式化输出（可选）
        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性
    }

    /**
     * 将对象转换为JSON字符串
     * @param obj 要转换的对象
     * @return JSON字符串，如果转换失败返回null
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("对象转JSON失败", e);
            return null;
        }
    }

    /**
     * 将对象转换为JSON字符串（与toJson方法功能相同，提供兼容性支持）
     * @param obj 要转换的对象
     * @return JSON字符串，如果转换失败返回null
     */
    public static String toJsonString(Object obj) {
        return toJson(obj);
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     * @param json JSON字符串
     * @param clazz 目标类
     * @param <T> 泛型参数
     * @return 转换后的对象，如果转换失败返回null
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("JSON转对象失败: {}", json, e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为指定类型的List集合
     * @param json JSON字符串
     * @param elementType 集合元素类型
     * @param <T> 泛型参数
     * @return 转换后的List集合，如果转换失败返回null
     */
    public static <T> List<T> toList(String json, Class<T> elementType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            JavaType javaType = TypeFactory.defaultInstance().constructCollectionType(List.class, elementType);
            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            logger.error("JSON转List失败: {}", json, e);
            return null;
        }
    }

    /**
     * 将JSON字符串转换为Map对象
     * @param json JSON字符串
     * @param keyType 键类型
     * @param valueType 值类型
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return 转换后的Map对象，如果转换失败返回null
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyType, Class<V> valueType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            JavaType javaType = TypeFactory.defaultInstance().constructMapType(Map.class, keyType, valueType);
            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            logger.error("JSON转Map失败: {}", json, e);
            return null;
        }
    }

    /**
     * 获取ObjectMapper实例，用于复杂的JSON处理需求
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
