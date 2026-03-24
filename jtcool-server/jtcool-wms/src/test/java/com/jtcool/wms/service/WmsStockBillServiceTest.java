package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsStockBill;
import com.jtcool.wms.domain.WmsStockBillItem;
import com.jtcool.wms.mapper.WmsStockBillMapper;
import com.jtcool.wms.service.impl.WmsStockBillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WmsStockBillServiceTest {

    @Mock
    private WmsStockBillMapper wmsStockBillMapper;

    @Mock
    private IWmsInventoryService wmsInventoryService;

    @InjectMocks
    private WmsStockBillServiceImpl wmsStockBillService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectWmsStockBillList() {
        WmsStockBill bill = new WmsStockBill();
        List<WmsStockBill> expected = Arrays.asList(bill);
        when(wmsStockBillMapper.selectWmsStockBillList(any())).thenReturn(expected);

        List<WmsStockBill> result = wmsStockBillService.selectWmsStockBillList(bill);

        assertEquals(expected, result);
        verify(wmsStockBillMapper).selectWmsStockBillList(bill);
    }

    @Test
    void testSelectWmsStockBillById() {
        WmsStockBill bill = new WmsStockBill();
        bill.setBillId(1L);
        when(wmsStockBillMapper.selectWmsStockBillWithItems(1L)).thenReturn(bill);

        WmsStockBill result = wmsStockBillService.selectWmsStockBillById(1L);

        assertEquals(bill, result);
        verify(wmsStockBillMapper).selectWmsStockBillWithItems(1L);
    }

    @Test
    void testInsertWmsStockBill() {
        WmsStockBill bill = new WmsStockBill();
        bill.setBillId(1L);
        WmsStockBillItem item = new WmsStockBillItem();
        bill.setItems(Arrays.asList(item));
        when(wmsStockBillMapper.insertWmsStockBill(any())).thenReturn(1);

        int result = wmsStockBillService.insertWmsStockBill(bill);

        assertEquals(1, result);
        verify(wmsStockBillMapper).insertWmsStockBill(bill);
        verify(wmsStockBillMapper).insertWmsStockBillItem(item);
    }

    @Test
    void testUpdateWmsStockBill() {
        WmsStockBill bill = new WmsStockBill();
        bill.setBillId(1L);
        WmsStockBillItem item = new WmsStockBillItem();
        bill.setItems(Arrays.asList(item));
        when(wmsStockBillMapper.updateWmsStockBill(any())).thenReturn(1);

        int result = wmsStockBillService.updateWmsStockBill(bill);

        assertEquals(1, result);
        verify(wmsStockBillMapper).updateWmsStockBill(bill);
        verify(wmsStockBillMapper).deleteWmsStockBillItemsByBillId(1L);
        verify(wmsStockBillMapper).insertWmsStockBillItem(item);
    }

    @Test
    void testDeleteWmsStockBillByIds() {
        Long[] ids = {1L, 2L};
        when(wmsStockBillMapper.deleteWmsStockBillByIds(ids)).thenReturn(2);

        int result = wmsStockBillService.deleteWmsStockBillByIds(ids);

        assertEquals(2, result);
        verify(wmsStockBillMapper).deleteWmsStockBillByIds(ids);
    }

    @Test
    void testConfirmStockBill_Success() {
        WmsStockBill bill = new WmsStockBill();
        bill.setBillId(1L);
        bill.setBillNo("IN001");
        bill.setBillType("IN_PURCHASE");
        bill.setBillStatus("PENDING");
        bill.setWarehouseId(1L);

        WmsStockBillItem item = new WmsStockBillItem();
        item.setProductId(1L);
        item.setAreaId(1L);
        item.setLocationId(1L);
        item.setShelfId(1L);
        item.setQuantity(100);
        bill.setItems(Arrays.asList(item));

        when(wmsStockBillMapper.selectWmsStockBillWithItems(1L)).thenReturn(bill);
        when(wmsStockBillMapper.updateWmsStockBill(any())).thenReturn(1);

        wmsStockBillService.confirmStockBill(1L, 1L);

        verify(wmsInventoryService).addInventory(1L, 1L, 1L, 1L, 1L, 100, 1L, "IN001", "IN_PURCHASE", 1L);
        verify(wmsStockBillMapper).updateWmsStockBill(argThat(b -> "COMPLETED".equals(b.getBillStatus())));
    }

    @Test
    void testConfirmStockBill_BillNotFound() {
        when(wmsStockBillMapper.selectWmsStockBillWithItems(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> wmsStockBillService.confirmStockBill(1L, 1L));

        assertEquals("出入库单不存在", exception.getMessage());
    }

    @Test
    void testConfirmStockBill_AlreadyCompleted() {
        WmsStockBill bill = new WmsStockBill();
        bill.setBillStatus("COMPLETED");
        when(wmsStockBillMapper.selectWmsStockBillWithItems(1L)).thenReturn(bill);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> wmsStockBillService.confirmStockBill(1L, 1L));

        assertEquals("出入库单已完成", exception.getMessage());
    }

    @Test
    void testConfirmStockBill_EmptyItems() {
        WmsStockBill bill = new WmsStockBill();
        bill.setBillStatus("PENDING");
        bill.setItems(Arrays.asList());
        when(wmsStockBillMapper.selectWmsStockBillWithItems(1L)).thenReturn(bill);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> wmsStockBillService.confirmStockBill(1L, 1L));

        assertEquals("出入库单明细不能为空", exception.getMessage());
    }
}
