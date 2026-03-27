-- 数据库查询优化 - 添加索引
-- 使用 CONCURRENTLY 避免锁表（PostgreSQL）

-- OMS订单表索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_oms_order_no ON oms_order(order_no);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_oms_order_customer ON oms_order(customer_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_oms_order_status ON oms_order(order_status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_oms_order_sales_user ON oms_order(sales_user_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_oms_order_date ON oms_order(order_date DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_oms_order_list ON oms_order(del_flag, order_date DESC);

-- WMS库存表索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_wms_inventory_product ON wms_inventory(product_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_wms_inventory_warehouse ON wms_inventory(warehouse_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_wms_inventory_composite ON wms_inventory(product_id, warehouse_id, area_id);

-- 产品表索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_prd_product_name ON prd_product(product_name);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_prd_product_code ON prd_product(product_code);
