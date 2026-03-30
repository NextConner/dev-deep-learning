package com.jtcool.common.statistics.dto;

/**
 * 查询性能估算结果
 */
public class QueryEstimation {
    /** 预估行数 */
    private Long estimatedRows;

    /** 预估执行时间(毫秒) */
    private Long estimatedTimeMs;

    /** 预估数据大小(字节) */
    private Long estimatedSizeBytes;

    /** 是否超过限制 */
    private Boolean exceedsLimit;

    /** 警告信息 */
    private String warning;

    public Long getEstimatedRows() {
        return estimatedRows;
    }

    public void setEstimatedRows(Long estimatedRows) {
        this.estimatedRows = estimatedRows;
    }

    public Long getEstimatedTimeMs() {
        return estimatedTimeMs;
    }

    public void setEstimatedTimeMs(Long estimatedTimeMs) {
        this.estimatedTimeMs = estimatedTimeMs;
    }

    public Long getEstimatedSizeBytes() {
        return estimatedSizeBytes;
    }

    public void setEstimatedSizeBytes(Long estimatedSizeBytes) {
        this.estimatedSizeBytes = estimatedSizeBytes;
    }

    public Boolean getExceedsLimit() {
        return exceedsLimit;
    }

    public void setExceedsLimit(Boolean exceedsLimit) {
        this.exceedsLimit = exceedsLimit;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
