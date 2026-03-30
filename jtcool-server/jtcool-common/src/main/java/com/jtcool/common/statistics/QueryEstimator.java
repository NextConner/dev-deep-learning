package com.jtcool.common.statistics;

import com.jtcool.common.statistics.dto.ParsedQuery;
import com.jtcool.common.statistics.dto.QueryEstimation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 查询性能估算器
 */
@Component
public class QueryEstimator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long MAX_ROWS = 10000L;
    private static final long MAX_TIME_MS = 30000L;
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024L; // 5MB

    /**
     * 估算查询性能
     */
    public QueryEstimation estimate(ParsedQuery query) {
        QueryEstimation estimation = new QueryEstimation();

        try {
            String sql = buildSQL(query);
            String explainSql = "EXPLAIN (FORMAT JSON) " + sql;

            List<Map<String, Object>> result = jdbcTemplate.queryForList(explainSql);

            if (!result.isEmpty()) {
                String jsonPlan = (String) result.get(0).values().iterator().next();
                parseExplainResult(jsonPlan, estimation);
            }

            checkLimits(estimation);

        } catch (Exception e) {
            estimation.setEstimatedRows(0L);
            estimation.setEstimatedTimeMs(0L);
            estimation.setExceedsLimit(false);
            estimation.setWarning("无法估算查询性能: " + e.getMessage());
        }

        return estimation;
    }

    /**
     * 构建SQL
     */
    private String buildSQL(ParsedQuery query) {
        StringBuilder sql = new StringBuilder("SELECT ");

        if (query.getSelectFields() != null && !query.getSelectFields().isEmpty()) {
            sql.append(String.join(", ", query.getSelectFields()));
        } else {
            sql.append("*");
        }

        sql.append(" FROM ").append(query.getTableName());

        if (query.getWhereConditions() != null && !query.getWhereConditions().isEmpty()) {
            sql.append(" WHERE 1=1");
            if (query.getWhereConditions().containsKey("beginTime")) {
                sql.append(" AND create_time >= ?");
            }
            if (query.getWhereConditions().containsKey("endTime")) {
                sql.append(" AND create_time <= ?");
            }
        }

        if (query.getGroupByField() != null) {
            sql.append(" GROUP BY ").append(query.getGroupByField());
        }

        if (query.getOrderByField() != null) {
            sql.append(" ORDER BY ").append(query.getOrderByField());
            if (query.getOrderDirection() != null) {
                sql.append(" ").append(query.getOrderDirection());
            }
        }

        if (query.getLimit() != null) {
            sql.append(" LIMIT ").append(query.getLimit());
        }

        return sql.toString();
    }

    /**
     * 解析EXPLAIN结果
     */
    private void parseExplainResult(String jsonPlan, QueryEstimation estimation) {
        // 简化解析 - 提取行数估算
        // 实际应该解析JSON，这里用简单的字符串匹配
        try {
            if (jsonPlan.contains("Plan Rows")) {
                int start = jsonPlan.indexOf("\"Plan Rows\"") + 13;
                int end = jsonPlan.indexOf(",", start);
                if (end == -1) end = jsonPlan.indexOf("}", start);
                String rowsStr = jsonPlan.substring(start, end).trim();
                long rows = Long.parseLong(rowsStr);
                estimation.setEstimatedRows(rows);

                // 估算时间: 假设每1000行需要10ms
                long timeMs = (rows / 1000) * 10;
                estimation.setEstimatedTimeMs(Math.max(timeMs, 10L));

                // 估算大小: 假设每行500字节
                long sizeBytes = rows * 500;
                estimation.setEstimatedSizeBytes(sizeBytes);
            }
        } catch (Exception e) {
            estimation.setEstimatedRows(1000L);
            estimation.setEstimatedTimeMs(100L);
            estimation.setEstimatedSizeBytes(500000L);
        }
    }

    /**
     * 检查是否超过限制
     */
    private void checkLimits(QueryEstimation estimation) {
        boolean exceeds = false;
        StringBuilder warning = new StringBuilder();

        if (estimation.getEstimatedRows() > MAX_ROWS) {
            exceeds = true;
            warning.append("预估行数超过限制(").append(MAX_ROWS).append("); ");
        }

        if (estimation.getEstimatedTimeMs() > MAX_TIME_MS) {
            exceeds = true;
            warning.append("预估执行时间超过限制(").append(MAX_TIME_MS).append("ms); ");
        }

        if (estimation.getEstimatedSizeBytes() > MAX_SIZE_BYTES) {
            exceeds = true;
            warning.append("预估数据大小超过限制(5MB); ");
        }

        estimation.setExceedsLimit(exceeds);
        if (exceeds) {
            estimation.setWarning(warning.toString());
        }
    }
}
