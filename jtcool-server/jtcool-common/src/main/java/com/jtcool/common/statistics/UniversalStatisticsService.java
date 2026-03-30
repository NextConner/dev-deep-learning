package com.jtcool.common.statistics;

import com.jtcool.common.statistics.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 万能统计服务
 */
@Service
public class UniversalStatisticsService {

    @Autowired
    private QueryParser queryParser;

    @Autowired
    private QuerySafetyValidator safetyValidator;

    @Autowired
    private QueryEstimator queryEstimator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 解析并验证查询
     */
    public ParsedQuery parseAndValidate(QueryRequest request) {
        ParsedQuery parsed = queryParser.parse(request);
        safetyValidator.validate(parsed);
        return parsed;
    }

    /**
     * 估算查询性能
     */
    public QueryEstimation estimatePerformance(ParsedQuery query) {
        return queryEstimator.estimate(query);
    }

    /**
     * 执行查询
     */
    public QueryResult executeQuery(ParsedQuery query) {
        QueryResult result = new QueryResult();
        long startTime = System.currentTimeMillis();

        try {
            String sql = buildSQL(query);
            Object[] params = buildParams(query);

            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql, params);

            result.setData(data);
            result.setRowCount(data.size());
            result.setColumns(extractColumns(data));
            result.setHasError(false);

        } catch (Exception e) {
            result.setHasError(true);
            result.setErrorMessage(e.getMessage());
            result.setData(new ArrayList<>());
            result.setRowCount(0);
        }

        long endTime = System.currentTimeMillis();
        result.setExecutionTimeMs(endTime - startTime);

        return result;
    }

    /**
     * 推荐图表类型
     */
    public ChartRecommendation recommendChartType(ParsedQuery query, QueryResult result) {
        ChartRecommendation recommendation = new ChartRecommendation();

        if (result.getRowCount() == 0) {
            recommendation.setRecommendedType("table");
            recommendation.setReason("无数据");
            return recommendation;
        }

        String groupBy = query.getGroupByField();
        int rowCount = result.getRowCount();

        if (groupBy != null && groupBy.contains("DATE")) {
            recommendation.setRecommendedType("line");
            recommendation.setReason("时间序列数据适合折线图");
            recommendation.setAlternativeTypes(Arrays.asList("bar", "table"));
        } else if (rowCount <= 10) {
            recommendation.setRecommendedType("pie");
            recommendation.setReason("少量分类数据适合饼图");
            recommendation.setAlternativeTypes(Arrays.asList("bar", "table"));
        } else if (rowCount <= 20) {
            recommendation.setRecommendedType("bar");
            recommendation.setReason("分类对比数据适合柱状图");
            recommendation.setAlternativeTypes(Arrays.asList("table", "line"));
        } else {
            recommendation.setRecommendedType("table");
            recommendation.setReason("大量数据适合表格展示");
            recommendation.setAlternativeTypes(Arrays.asList("bar", "line"));
        }

        return recommendation;
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
     * 构建参数
     */
    private Object[] buildParams(ParsedQuery query) {
        List<Object> params = new ArrayList<>();

        if (query.getWhereConditions() != null) {
            if (query.getWhereConditions().containsKey("beginTime")) {
                params.add(query.getWhereConditions().get("beginTime"));
            }
            if (query.getWhereConditions().containsKey("endTime")) {
                params.add(query.getWhereConditions().get("endTime"));
            }
        }

        return params.toArray();
    }

    /**
     * 提取列名
     */
    private List<String> extractColumns(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(data.get(0).keySet());
    }
}
