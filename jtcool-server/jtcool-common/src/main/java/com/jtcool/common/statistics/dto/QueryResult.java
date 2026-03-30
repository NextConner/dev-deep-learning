package com.jtcool.common.statistics.dto;

import java.util.List;
import java.util.Map;

/**
 * 查询执行结果
 */
public class QueryResult {
    /** 实际行数 */
    private Integer rowCount;

    /** 执行时间(毫秒) */
    private Long executionTimeMs;

    /** 数据列表 */
    private List<Map<String, Object>> data;

    /** 列名列表 */
    private List<String> columns;

    /** 是否有错误 */
    private Boolean hasError;

    /** 错误信息 */
    private String errorMessage;

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
