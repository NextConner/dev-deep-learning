package com.jtcool.wms.integration;

import com.jtcool.wms.domain.WmsInventory;
import com.jtcool.wms.mapper.WmsInventoryMapper;
import com.jtcool.wms.service.IWmsInventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired
    private IWmsInventoryService inventoryService;

    @Autowired
    private WmsInventoryMapper inventoryMapper;

    @Test
    void testConcurrentInventoryDeduction() throws InterruptedException {
        // 准备测试数据：创建初始库存
        WmsInventory inventory = new WmsInventory();
        inventory.setProductId(1L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(100);
        inventory.setAvailableQuantity(100);
        inventoryMapper.insertWmsInventory(inventory);

        Long inventoryId = inventory.getInventoryId();
        int threadCount = 50;
        int deductQuantity = 1;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 启动50个并发线程扣减库存
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    inventoryService.deductInventory(
                        inventory.getProductId(),
                        inventory.getWarehouseId(),
                        null, null, null,
                        deductQuantity,
                        null, "TEST_BILL", "TEST",
                        1L
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 同时启动所有线程
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // 验证结果
        WmsInventory result = inventoryMapper.selectWmsInventoryById(inventoryId);

        // 验证：最终库存 = 初始库存 - 成功扣减次数
        int expectedQuantity = 100 - (successCount.get() * deductQuantity);
        assertEquals(expectedQuantity, result.getQuantity(),
            "Final inventory should match: initial - successful deductions");

        // 验证：没有超卖（库存不能为负数）
        assertTrue(result.getQuantity() >= 0, "Inventory should not be negative (no overselling)");

        // 验证：成功次数 + 失败次数 = 总线程数
        assertEquals(threadCount, successCount.get() + failCount.get(),
            "Total attempts should equal thread count");
    }

    @Test
    void testConcurrentInventoryDeductionWithInsufficientStock() throws InterruptedException {
        // 准备测试数据：创建库存不足的场景
        WmsInventory inventory = new WmsInventory();
        inventory.setProductId(2L);
        inventory.setWarehouseId(1L);
        inventory.setQuantity(10);
        inventory.setAvailableQuantity(10);
        inventoryMapper.insertWmsInventory(inventory);

        int threadCount = 20;
        int deductQuantity = 1;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    inventoryService.deductInventory(
                        inventory.getProductId(),
                        inventory.getWarehouseId(),
                        null, null, null,
                        deductQuantity,
                        null, "TEST_BILL", "TEST",
                        1L
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 库存不足时会抛出异常
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        // 验证：成功扣减次数不超过初始库存
        assertTrue(successCount.get() <= 10,
            "Successful deductions should not exceed initial stock");

        WmsInventory result = inventoryMapper.selectWmsInventoryById(inventory.getInventoryId());
        assertEquals(0, result.getQuantity(),
            "All stock should be depleted but not negative");
    }
}
