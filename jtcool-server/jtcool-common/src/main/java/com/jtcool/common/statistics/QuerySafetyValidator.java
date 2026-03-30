package com.jtcool.common.statistics;

import com.jtcool.common.statistics.dto.ParsedQuery;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 查询安全验证器
 */
@Component
public class QuerySafetyValidator {

    private static final Set<String> ALLOWED_TABLES = new HashSet<>(Arrays.asList(
        "oms_order", "wms_inventory", "oms_customer", "product_info"
    ));

    private static final Map<String, Set<String>> ALLOWED_COLUMNS = new HashMap<>();
    static {
        ALLOWED_COLUMNS.put("oms_order", new HashSet<>(Arrays.asList(
            "id", "order_no", "customer_id", "total_amount", "status", "create_time"
        )));
        ALLOWED_COLUMNS.put("wms_inventory", new HashSet<>(Arrays.asList(
            "id", "product_id", "quantity", "type", "status", "create_time"
        )));
        ALLOWED_COLUMNS.put("oms_customer", new HashSet<>(Arrays.asList(
            "id", "customer_name", "customer_code", "create_time"
        )));
        ALLOWED_COLUMNS.put("product_info", new HashSet<>(Arrays.asList(
            "id", "product_name", "product_code", "category", "create_time"
        )));
    }

    private static final Set<String> ALLOWED_AGGREGATIONS = new HashSet<>(Arrays.asList(
        "COUNT", "SUM", "AVG", "MAX", "MIN"
    ));

    private static final int MAX_LIMIT = 10000;

    /**
     * 验证查询安全性
     */
    public void validate(ParsedQuery query) {
        validateTableName(query.getTableName());
        validateFields(query);
        validateAggregation(query.getAggregation());
        validateLimit(query.getLimit());
        validateNoInjection(query);
    }

    /**
     * 验证表名
     */
    private void validateTableName(String tableName) {
        if (tableName == null || !ALLOWED_TABLES.contains(tableName)) {
            throw new SecurityException("不允许访问的表: " + tableName);
        }
    }

    /**
     * 验证字段
     */
    private void validateFields(ParsedQuery query) {
        String tableName = query.getTableName();
        Set<String> allowedCols = ALLOWED_COLUMNS.get(tableName);

        if (query.getSelectFields() != null) {
            for (String field : query.getSelectFields()) {
                validateFieldSafety(field, allowedCols);
            }
        }

        if (query.getAggregationField() != null) {
            validateColumnName(query.getAggregationField(), allowedCols);
        }
    }

    /**
     * 验证字段安全性
     */
    private void validateFieldSafety(String field, Set<String> allowedCols) {
        // 允许聚合表达式和别名
        if (field.contains(" AS ")) {
            String[] parts = field.split(" AS ");
            String expr = parts[0].trim();
            validateExpression(expr, allowedCols);
        } else {
            validateColumnName(field, allowedCols);
        }
    }

    /**
     * 验证表达式
     */
    private void validateExpression(String expr, Set<String> allowedCols) {
        // 允许聚合函数
        if (expr.matches("^(COUNT|SUM|AVG|MAX|MIN)\\(.*\\)$")) {
            return;
        }
        // 允许日期函数
        if (expr.matches("^DATE\\(.*\\)$") || expr.matches("^DATE_TRUNC\\('\\w+',.*\\)$")) {
            return;
        }
        // 其他情况验证列名
        validateColumnName(expr, allowedCols);
    }

    /**
     * 验证列名
     */
    private void validateColumnName(String columnName, Set<String> allowedCols) {
        if (columnName == null) {
            return;
        }

        String cleanName = columnName.trim();
        if (!allowedCols.contains(cleanName) && !cleanName.equals("*")) {
            throw new SecurityException("不允许访问的字段: " + columnName);
        }
    }

    /**
     * 验证聚合函数
     */
    private void validateAggregation(String aggregation) {
        if (aggregation != null && !ALLOWED_AGGREGATIONS.contains(aggregation)) {
            throw new SecurityException("不允许的聚合函数: " + aggregation);
        }
    }

    /**
     * 验证限制
     */
    private void validateLimit(Integer limit) {
        if (limit != null && limit > MAX_LIMIT) {
            throw new SecurityException("查询限制超过最大值: " + MAX_LIMIT);
        }
    }

    /**
     * 验证SQL注入
     */
    private void validateNoInjection(ParsedQuery query) {
        // 检查分组字段
        if (query.getGroupByField() != null) {
            checkInjectionPattern(query.getGroupByField());
        }

        // 检查排序字段
        if (query.getOrderByField() != null) {
            checkInjectionPattern(query.getOrderByField());
        }

        // 检查过滤条件的值
        if (query.getWhereConditions() != null) {
            for (Object value : query.getWhereConditions().values()) {
                if (value instanceof String) {
                    checkInjectionPattern((String) value);
                }
            }
        }
    }

    /**
     * 检查注入模式
     */
    private void checkInjectionPattern(String input) {
        if (input == null) {
            return;
        }

        String lower = input.toLowerCase();
        String[] dangerousPatterns = {
            "drop ", "delete ", "truncate ", "insert ", "update ",
            "exec ", "execute ", "script ", "javascript:", "onerror=",
            "--", "/*", "*/", "xp_", "sp_", "union ", "select "
        };

        for (String pattern : dangerousPatterns) {
            if (lower.contains(pattern)) {
                throw new SecurityException("检测到潜在的SQL注入: " + pattern);
            }
        }
    }
}
