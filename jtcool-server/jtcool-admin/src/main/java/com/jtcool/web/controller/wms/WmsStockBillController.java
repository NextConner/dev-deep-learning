package com.jtcool.web.controller.wms;

import com.jtcool.common.annotation.RepeatSubmit;
import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.wms.domain.WmsStockBill;
import com.jtcool.wms.service.IWmsStockBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wms/stockBill")
public class WmsStockBillController extends BaseController {
    @Autowired
    private IWmsStockBillService wmsStockBillService;

    @GetMapping("/list")
    public TableDataInfo list(WmsStockBill wmsStockBill) {
        startPage();
        List<WmsStockBill> list = wmsStockBillService.selectWmsStockBillList(wmsStockBill);
        return getDataTable(list);
    }

    @GetMapping("/{billId}")
    public AjaxResult getInfo(@PathVariable Long billId) {
        return success(wmsStockBillService.selectWmsStockBillById(billId));
    }

    @RepeatSubmit(interval = 5000, message = "请勿重复提交出入库单")
    @PostMapping
    public AjaxResult add(@RequestBody WmsStockBill wmsStockBill) {
        return toAjax(wmsStockBillService.insertWmsStockBill(wmsStockBill));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody WmsStockBill wmsStockBill) {
        return toAjax(wmsStockBillService.updateWmsStockBill(wmsStockBill));
    }

    @DeleteMapping("/{billIds}")
    public AjaxResult remove(@PathVariable Long[] billIds) {
        return toAjax(wmsStockBillService.deleteWmsStockBillByIds(billIds));
    }

    @RepeatSubmit(interval = 3000, message = "操作进行中，请稍候")
    @PostMapping("/confirm/{billId}")
    public AjaxResult confirm(@PathVariable Long billId, @RequestParam(required = false) Long operatorId) {
        wmsStockBillService.confirmStockBill(billId, operatorId);
        return success();
    }
}
