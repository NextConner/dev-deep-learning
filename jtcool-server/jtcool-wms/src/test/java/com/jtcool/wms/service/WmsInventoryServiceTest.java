package com.jtcool.wms.service;

import com.jtcool.common.utils.RedissonLockUtil;
import com.jtcool.wms.domain.WmsInventory;
import com.jtcool.wms.mapper.WmsInventoryLogMapper;
import com.jtcool.wms.mapper.WmsInventoryMapper;
import com.jtcool.wms.service.impl.WmsInventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WmsInventoryServiceTest {

    @Mock
    private WmsInventoryMapper wmsInventoryMapper;

    @Mock
    private WmsInventoryLogMapper wmsInventoryLogMapper;

    @Mock
    private RedissonLockUtil redissonLockUtil;

    @InjectMocks
    private WmsInventoryServiceImpl wmsInventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectWmsInventoryList() {
        WmsInventory inventory = new WmsInventory();
        List<WmsInventory> expected = Arrays.asList(inventory);
        when(wmsInventoryMapper.selectWmsInventoryList(any())).thenReturn(expected);

        List<WmsInventory> result = wmsInventoryService.selectWmsInventoryList(inventory);

        assertEquals(expected, result);
        verify(wmsInventoryMapper).selectWmsInventoryList(inventory);
    }

    @Test
    void testSelectWmsInventoryById() {
        WmsInventory inventory = new WmsInventory();
        inventory.setInventoryId(1L);
        when(wmsInventoryMapper.selectWmsInventoryById(1L)).thenReturn(inventory);

        WmsInventory result = wmsInventoryService.selectWmsInventoryById(1L);

        assertEquals(inventory, result);
        verify(wmsInventoryMapper).selectWmsInventoryById(1L);
    }

    @Test
    void testInsertWmsInventory() {
        WmsInventory inventory = new WmsInventory();
        inventory.setQuantity(100);
        when(wmsInventoryMapper.insertWmsInventory(any())).thenReturn(1);

        int result = wmsInventoryService.insertWmsInventory(inventory);

        assertEquals(1, result);
        assertEquals(100, inventory.getAvailableQuantity());
        verify(wmsInventoryMapper).insertWmsInventory(inventory);
    }

    @Test
    void testUpdateWmsInventory() {
        WmsInventory inventory = new WmsInventory();
        inventory.setInventoryId(1L);
        when(wmsInventoryMapper.updateWmsInventory(any())).thenReturn(1);

        int result = wmsInventoryService.updateWmsInventory(inventory);

        assertEquals(1, result);
        verify(wmsInventoryMapper).updateWmsInventory(inventory);
    }
}
