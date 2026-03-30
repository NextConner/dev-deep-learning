package com.jtcool.web.controller.statistics;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.statistics.UniversalStatisticsService;
import com.jtcool.common.statistics.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 万能统计控制器
 */
@RestController
@RequestMapping("/statistics/universal")
public class UniversalStatisticsController extends BaseController {

    @Autowired
    private UniversalStatisticsService statisticsService;

    /**
     * 解析查询
     */
    @PreAuthorize("@ss.hasPermi('statistics:universal:query')")
    @PostMapping("/parse")
    public AjaxResult parse(@RequestBody QueryRequest request) {
        try {
            ParsedQuery parsed = statisticsService.parseAndValidate(request);
            return AjaxResult.success(parsed);
        } catch (SecurityException e) {
            return AjaxResult.error("安全验证失败: " + e.getMessage());
        } catch (Exception e) {
            return AjaxResult.error("解析失败: " + e.getMessage());
        }
    }

    /**
     * 估算查询性能
     */
    @PreAuthorize("@ss.hasPermi('statistics:universal:query')")
    @PostMapping("/estimate")
    public AjaxResult estimate(@RequestBody ParsedQuery query) {
        try {
            QueryEstimation estimation = statisticsService.estimatePerformance(query);
            return AjaxResult.success(estimation);
        } catch (Exception e) {
            return AjaxResult.error("估算失败: " + e.getMessage());
        }
    }

    /**
     * 执行查询
     */
    @PreAuthorize("@ss.hasPermi('statistics:universal:query')")
    @PostMapping("/execute")
    public AjaxResult execute(@RequestBody ParsedQuery query) {
        try {
            QueryResult result = statisticsService.executeQuery(query);
            if (result.getHasError()) {
                return AjaxResult.error("查询执行失败: " + result.getErrorMessage());
            }
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 推荐图表类型
     */
    @PreAuthorize("@ss.hasPermi('statistics:universal:query')")
    @PostMapping("/recommend-chart")
    public AjaxResult recommendChart(@RequestBody Map<String, Object> params) {
        try {
            ParsedQuery query = (ParsedQuery) params.get("query");
            QueryResult result = (QueryResult) params.get("result");

            ChartRecommendation recommendation = statisticsService.recommendChartType(query, result);
            return AjaxResult.success(recommendation);
        } catch (Exception e) {
            return AjaxResult.error("推荐失败: " + e.getMessage());
        }
    }
}
