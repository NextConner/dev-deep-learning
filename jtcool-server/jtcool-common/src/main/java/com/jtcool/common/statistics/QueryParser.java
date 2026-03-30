package com.jtcool.common.statistics;

import com.jtcool.common.statistics.dto.ParsedQuery;
import com.jtcool.common.statistics.dto.QueryRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 查询解析器 - 模板匹配
 */
@Component
public class QueryParser {

    private static final int DEFAULT_LIMIT = 1000;
    private static final int MAX_LIMIT = 10000;

    /**
     * 解析查询请求
     */
    public ParsedQuery parse(QueryRequest request) {
        String queryText = request.getQueryText();
        if (queryText == null || queryText.trim().isEmpty()) {
            throw new IllegalArgumentException("查询文本不能为空");
        }

        ParsedQuery parsed = new ParsedQuery();

        // 识别表名
        String tableName = extractTableName(queryText);
        parsed.setTableName(tableName);

        // 识别聚合类型
        String aggregation = extractAggregation(queryText);
        parsed.setAggregation(aggregation);

        // 识别聚合字段
        if (aggregation != null && !aggregation.equals("COUNT")) {
            String aggField = extractAggregationField(queryText, tableName);
            parsed.setAggregationField(aggField);
        }

        // 识别分组字段
        String groupBy = extractGroupBy(queryText, tableName);
        parsed.setGroupByField(groupBy);

        // 构建查询字段
        List<String> selectFields = buildSelectFields(parsed);
        parsed.setSelectFields(selectFields);

        // 构建过滤条件
        Map<String, Object> conditions = new HashMap<>();
        if (request.getBeginTime() != null) {
            conditions.put("beginTime", request.getBeginTime());
        }
        if (request.getEndTime() != null) {
            conditions.put("endTime", request.getEndTime());
        }
        if (request.getFilters() != null) {
            conditions.putAll(request.getFilters());
        }
        parsed.setWhereConditions(conditions);

        // 设置排序和限制
        parsed.setOrderByField(groupBy != null ? groupBy : "create_time");
        parsed.setOrderDirection("DESC");
        parsed.setLimit(DEFAULT_LIMIT);

        return parsed;
    }

    /**
     * 提取表名
     */
    private String extractTableName(String queryText) {
        if (queryText.contains("订单")) {
            return "oms_order";
        } else if (queryText.contains("库存") || queryText.contains("入库") || queryText.contains("出库")) {
            return "wms_inventory";
        } else if (queryText.contains("客户")) {
            return "oms_customer";
        } else if (queryText.contains("产品")) {
            return "product_info";
        }
        return "oms_order"; // 默认
    }

    /**
     * 提取聚合函数
     */
    private String extractAggregation(String queryText) {
        if (queryText.contains("总金额") || queryText.contains("金额总计") || queryText.contains("总额")) {
            return "SUM";
        } else if (queryText.contains("平均") || queryText.contains("均值")) {
            return "AVG";
        } else if (queryText.contains("最大") || queryText.contains("最高")) {
            return "MAX";
        } else if (queryText.contains("最小") || queryText.contains("最低")) {
            return "MIN";
        } else if (queryText.contains("数量") || queryText.contains("统计") || queryText.contains("多少")) {
            return "COUNT";
        }
        return "COUNT"; // 默认
    }

    /**
     * 提取聚合字段
     */
    private String extractAggregationField(String queryText, String tableName) {
        if ("oms_order".equals(tableName)) {
            if (queryText.contains("金额")) {
                return "total_amount";
            }
        } else if ("wms_inventory".equals(tableName)) {
            if (queryText.contains("数量")) {
                return "quantity";
            }
        }
        return "id";
    }

    /**
     * 提取分组字段
     */
    private String extractGroupBy(String queryText, String tableName) {
        if (queryText.contains("每日") || queryText.contains("按日") || queryText.contains("天")) {
            return "DATE(create_time)";
        } else if (queryText.contains("每周") || queryText.contains("按周")) {
            return "DATE_TRUNC('week', create_time)";
        } else if (queryText.contains("每月") || queryText.contains("按月") || queryText.contains("月")) {
            return "DATE_TRUNC('month', create_time)";
        } else if (queryText.contains("客户")) {
            return "customer_id";
        } else if (queryText.contains("产品")) {
            return "product_id";
        } else if (queryText.contains("状态")) {
            return "status";
        }
        return null;
    }

    /**
     * 构建查询字段列表
     */
    private List<String> buildSelectFields(ParsedQuery parsed) {
        List<String> fields = new ArrayList<>();

        if (parsed.getGroupByField() != null) {
            fields.add(parsed.getGroupByField() + " AS group_key");
        }

        String aggFunc = parsed.getAggregation();
        String aggField = parsed.getAggregationField();

        if ("COUNT".equals(aggFunc)) {
            fields.add("COUNT(*) AS value");
        } else if (aggField != null) {
            fields.add(aggFunc + "(" + aggField + ") AS value");
        }

        return fields;
    }
}
