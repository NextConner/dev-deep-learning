package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsWarehouse;
import com.jtcool.wms.mapper.WmsWarehouseMapper;
import com.jtcool.wms.service.impl.WmsWarehouseServiceImpl;
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

class WmsWarehouseServiceTest {

    @Mock
    private WmsWarehouseMapper wmsWarehouseMapper;

    @InjectMocks
    private WmsWarehouseServiceImpl wmsWarehouseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectWmsWarehouseList() {
        WmsWarehouse warehouse = new WmsWarehouse();
        List<WmsWarehouse> expected = Arrays.asList(warehouse);
        when(wmsWarehouseMapper.selectWmsWarehouseList(any())).thenReturn(expected);

        List<WmsWarehouse> result = wmsWarehouseService.selectWmsWarehouseList(warehouse);

        assertEquals(expected, result);
        verify(wmsWarehouseMapper).selectWmsWarehouseList(warehouse);
    }

    @Test
    void testSelectWmsWarehouseById() {
        WmsWarehouse warehouse = new WmsWarehouse();
        warehouse.setWarehouseId(1L);
        when(wmsWarehouseMapper.selectWmsWarehouseById(1L)).thenReturn(warehouse);

        WmsWarehouse result = wmsWarehouseService.selectWmsWarehouseById(1L);

        assertEquals(warehouse, result);
        verify(wmsWarehouseMapper).selectWmsWarehouseById(1L);
    }

    @Test
    void testInsertWmsWarehouse() {
        WmsWarehouse warehouse = new WmsWarehouse();
        when(wmsWarehouseMapper.insertWmsWarehouse(any())).thenReturn(1);

        int result = wmsWarehouseService.insertWmsWarehouse(warehouse);

        assertEquals(1, result);
        verify(wmsWarehouseMapper).insertWmsWarehouse(warehouse);
    }

    @Test
    void testUpdateWmsWarehouse() {
        WmsWarehouse warehouse = new WmsWarehouse();
        warehouse.setWarehouseId(1L);
        when(wmsWarehouseMapper.updateWmsWarehouse(any())).thenReturn(1);

        int result = wmsWarehouseService.updateWmsWarehouse(warehouse);

        assertEquals(1, result);
        verify(wmsWarehouseMapper).updateWmsWarehouse(warehouse);
    }

    @Test
    void testDeleteWmsWarehouseByIds() {
        Long[] ids = {1L, 2L};
        when(wmsWarehouseMapper.deleteWmsWarehouseByIds(ids)).thenReturn(2);

        int result = wmsWarehouseService.deleteWmsWarehouseByIds(ids);

        assertEquals(2, result);
        verify(wmsWarehouseMapper).deleteWmsWarehouseByIds(ids);
    }
}
