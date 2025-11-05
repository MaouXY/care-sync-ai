package com.caresync.ai.handler;

import com.caresync.ai.utils.JsonUtil;
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
        return parseJsonValue(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJsonValue(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJsonValue(cs.getString(columnIndex));
    }

    /**
     * 解析JSON字符串为Map对象
     */
    private Object parseJsonValue(String jsonValue) {
        if (jsonValue == null || jsonValue.isEmpty()) {
            return null;
        }
        try {
            return JsonUtil.toMap(jsonValue, String.class, Object.class);
        } catch (Exception e) {
            logger.error("解析JSON值失败: {}", e.getMessage(), e);
            // 解析失败时返回原始字符串
            return jsonValue;
        }
    }
}