package com.jtcool.common.statistics.dto;

import java.util.List;
import java.util.Map;

/**
 * 解析后的查询结构
 */
public class ParsedQuery {
    /** 目标表名 */
    private String tableName;

    /** 查询字段 */
    private List<String> selectFields;

    /** 聚合函数 (COUNT/SUM/AVG/MAX/MIN) */
    private String aggregation;

    /** 聚合字段 */
    private String aggregationField;

    /** 分组字段 */
    private String groupByField;

    /** 过滤条件 */
    private Map<String, Object> whereConditions;

    /** 排序字段 */
    private String orderByField;

    /** 排序方向 (ASC/DESC) */
    private String orderDirection;

    /** 限制行数 */
    private Integer limit;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(List<String> selectFields) {
        this.selectFields = selectFields;
    }

    public String getAggregation() {
        return aggregation;
    }

    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }

    public String getAggregationField() {
        return aggregationField;
    }

    public void setAggregationField(String aggregationField) {
        this.aggregationField = aggregationField;
    }

    public String getGroupByField() {
        return groupByField;
    }

    public void setGroupByField(String groupByField) {
        this.groupByField = groupByField;
    }

    public Map<String, Object> getWhereConditions() {
        return whereConditions;
    }

    public void setWhereConditions(Map<String, Object> whereConditions) {
        this.whereConditions = whereConditions;
    }

    public String getOrderByField() {
        return orderByField;
    }

    public void setOrderByField(String orderByField) {
        this.orderByField = orderByField;
    }

    public String getOrderDirection() {
        return orderDirection;
    }

    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
