package com.caresync.ai.handler;

import com.caresync.ai.utils.JsonUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * PostgreSQL JSONB类型处理器
 * 用于正确处理Java对象与PostgreSQL jsonb类型之间的转换
 */
@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes({Map.class, Object.class, String.class})
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(JsonbTypeHandler.class);
    private static final String JSONB_TYPE = "jsonb";
    private final Class<?> targetType;

    /**
     * 无参构造函数 - MyBatis默认使用
     */
    public JsonbTypeHandler() {
        this.targetType = Object.class;
    }
    
    /**
     * 带参构造函数 - 用于指定目标类型
     * @param targetType 目标类型
     */
    public JsonbTypeHandler(Class<?> targetType) {
        this.targetType = targetType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject jsonbObj = new PGobject();
            jsonbObj.setType(JSONB_TYPE);
            
            // 如果参数已经是字符串，直接使用；否则转换为JSON字符串
            String jsonString = parameter instanceof String ? (String) parameter : JsonUtil.toJson(parameter);
            jsonbObj.setValue(jsonString);
            
            ps.setObject(i, jsonbObj);
        } catch (Exception e) {
            logger.error("设置JSONB参数失败: {}", e.getMessage(), e);
            throw new SQLException("Failed to set JSONB parameter: " + e.getMessage(), e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String jsonValue = rs.getString(columnName);
        // 返回原始字符串，让业务层决定如何处理类型转换
        return jsonValue;
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonValue = rs.getString(columnIndex);
        // 返回原始字符串，让业务层决定如何处理类型转换
        return jsonValue;
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String jsonValue = cs.getString(columnIndex);
        // 返回原始字符串，让业务层决定如何处理类型转换
        return jsonValue;
    }

    /**
     * 辅助方法：将JSON字符串转换为Map对象
     * 这个方法可以在业务代码中明确调用，用于将字符串转换为Map
     */
    public static Map<String, Object> toMap(String jsonValue) {
        if (jsonValue == null || jsonValue.isEmpty()) {
            return null;
        }
        try {
            return JsonUtil.toMap(jsonValue, String.class, Object.class);
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(JsonbTypeHandler.class);
            logger.error("解析JSON值失败: {}", e.getMessage(), e);
            // 解析失败时返回null
            return null;
        }
    }

    /**
     * 辅助方法：将JSON字符串转换为指定类型的对象
     * @param jsonValue JSON字符串
     * @param clazz 目标类型
     * @return 指定类型的对象
     */
    public static <T> T toObject(String jsonValue, Class<T> clazz) {
        if (jsonValue == null || jsonValue.isEmpty()) {
            return null;
        }
        try {
            return JsonUtil.toObject(jsonValue, clazz);
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(JsonbTypeHandler.class);
            logger.error("解析JSON值失败: {}", e.getMessage(), e);
            // 解析失败时返回null
            return null;
        }
    }
}