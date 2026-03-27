package com.jtcool.framework.service;

import com.jtcool.common.domain.ExportStatus;
import com.jtcool.common.utils.poi.ExcelUtil;
import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.service.IOmsOrderService;
import com.jtcool.wms.domain.WmsInventory;
import com.jtcool.wms.service.IWmsInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 异步导出执行服务
 */
@Service
public class AsyncExportService {

    private static final Logger log = LoggerFactory.getLogger(AsyncExportService.class);

    @Autowired
    private ExportJobService exportJobService;

    @Autowired(required = false)
    private IOmsOrderService omsOrderService;

    @Autowired(required = false)
    private IWmsInventoryService wmsInventoryService;

    @Value("${jtcool.profile}")
    private String downloadPath;

    /** 最多 20 个并发导出 */
    private final Semaphore exportSemaphore = new Semaphore(20);

    private static final int PAGE_SIZE = 10000;

    /**
     * 异步执行导出任务
     */
    @Async
    public void executeExport(String jobId, String exportType, Object queryParams) {
        if (!exportSemaphore.tryAcquire()) {
            exportJobService.failJob(jobId, "导出任务过多，请稍后重试");
            log.warn("导出任务过多，拒绝任务: jobId={}", jobId);
            return;
        }

        try {
            log.info("开始执行导出任务: jobId={}, exportType={}", jobId, exportType);
            exportJobService.updateStatus(jobId, ExportStatus.PROCESSING);

            // 生成文件路径（永久保留）
            String filePath = generateFilePath(jobId, exportType);

            // 执行分页导出
            exportWithPagination(jobId, exportType, queryParams, filePath);

            exportJobService.completeJob(jobId, filePath);
            log.info("导出任务完成: jobId={}, filePath={}", jobId, filePath);

        } catch (Exception e) {
            log.error("导出任务失败: jobId={}", jobId, e);
            exportJobService.failJob(jobId, e.getMessage());
        } finally {
            exportSemaphore.release();
        }
    }

    /**
     * 生成文件路径
     * 格式: {downloadPath}/exports/{date}/{jobId}_{exportType}_{timestamp}.xlsx
     */
    private String generateFilePath(String jobId, String exportType) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String dirPath = downloadPath + "/exports/" + dateStr;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = jobId + "_" + exportType + "_" + timestamp + ".xlsx";
        return dirPath + "/" + fileName;
    }

    /**
     * 分页导出核心逻辑
     */
    private void exportWithPagination(String jobId, String exportType, Object queryParams, String filePath) throws Exception {
        if ("OmsOrder".equals(exportType)) {
            exportOmsOrders(jobId, filePath);
        } else if ("WmsInventory".equals(exportType)) {
            exportWmsInventory(jobId, filePath);
        } else {
            throw new IllegalArgumentException("不支持的导出类型: " + exportType);
        }
    }

    private void exportOmsOrders(String jobId, String filePath) throws Exception {
        OmsOrder query = new OmsOrder();
        long total = omsOrderService.countOrderList(query);

        ExcelUtil<OmsOrder> util = new ExcelUtil<>(OmsOrder.class);
        util.initStreamingExport(filePath);

        try {
            int offset = 0;
            long processed = 0;

            while (offset < total) {
                List<OmsOrder> batch = omsOrderService.selectOrderListPaginated(query, PAGE_SIZE, offset);
                if (batch.isEmpty()) break;

                util.appendBatch(batch);
                processed += batch.size();
                offset += PAGE_SIZE;

                int progress = (int) ((processed * 100) / total);
                exportJobService.updateJobProgress(jobId, progress, processed);
            }

            util.finalizeExport();
        } catch (Exception e) {
            throw e;
        }
    }

    private void exportWmsInventory(String jobId, String filePath) throws Exception {
        WmsInventory query = new WmsInventory();
        long total = wmsInventoryService.countInventoryList(query);

        ExcelUtil<WmsInventory> util = new ExcelUtil<>(WmsInventory.class);
        util.initStreamingExport(filePath);

        try {
            int offset = 0;
            long processed = 0;

            while (offset < total) {
                List<WmsInventory> batch = wmsInventoryService.selectInventoryListPaginated(query, PAGE_SIZE, offset);
                if (batch.isEmpty()) break;

                util.appendBatch(batch);
                processed += batch.size();
                offset += PAGE_SIZE;

                int progress = (int) ((processed * 100) / total);
                exportJobService.updateJobProgress(jobId, progress, processed);
            }

            util.finalizeExport();
        } catch (Exception e) {
            throw e;
        }
    }
}
