package com.jtcool.common.statistics.dto;

import java.util.List;

/**
 * 图表推荐结果
 */
public class ChartRecommendation {
    /** 推荐的图表类型 */
    private String recommendedType;

    /** 推荐理由 */
    private String reason;

    /** 可选的图表类型列表 */
    private List<String> alternativeTypes;

    public String getRecommendedType() {
        return recommendedType;
    }

    public void setRecommendedType(String recommendedType) {
        this.recommendedType = recommendedType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getAlternativeTypes() {
        return alternativeTypes;
    }

    public void setAlternativeTypes(List<String> alternativeTypes) {
        this.alternativeTypes = alternativeTypes;
    }
}
