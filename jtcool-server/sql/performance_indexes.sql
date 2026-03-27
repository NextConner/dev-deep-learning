-- =============================================
-- 性能优化索引脚本
-- 用途：为软删除标记、状态字段和常用查询组合添加索引
-- 执行方式：psql -U postgres -d jtcool < performance_indexes.sql
-- =============================================

-- OMS 订单管理模块索引
CREATE INDEX IF NOT EXISTS idx_oms_order_del_flag ON oms_order(del_flag);
CREATE INDEX IF NOT EXISTS idx_oms_order_status ON oms_order(order_status);
CREATE INDEX IF NOT EXISTS idx_oms_order_customer_status ON oms_order(customer_id, order_status, del_flag);
CREATE INDEX IF NOT EXISTS idx_oms_order_date ON oms_order(order_date);
CREATE INDEX IF NOT EXISTS idx_oms_customer_del_flag ON oms_customer(del_flag);
CREATE INDEX IF NOT EXISTS idx_oms_finance_payment_status ON oms_finance(payment_status);
CREATE INDEX IF NOT EXISTS idx_oms_finance_order ON oms_finance(order_id, del_flag);

-- WMS 仓库管理模块索引
CREATE INDEX IF NOT EXISTS idx_wms_inventory_del_flag ON wms_inventory(del_flag);
CREATE INDEX IF NOT EXISTS idx_wms_inventory_product_warehouse ON wms_inventory(product_id, warehouse_id, del_flag);
CREATE INDEX IF NOT EXISTS idx_wms_stock_bill_status ON wms_stock_bill(bill_status);
CREATE INDEX IF NOT EXISTS idx_wms_stock_bill_del_flag ON wms_stock_bill(del_flag);
CREATE INDEX IF NOT EXISTS idx_wms_stock_bill_type ON wms_stock_bill(bill_type, bill_date);
CREATE INDEX IF NOT EXISTS idx_wms_inventory_log_change_type ON wms_inventory_log(change_type);
CREATE INDEX IF NOT EXISTS idx_wms_inventory_log_product ON wms_inventory_log(product_id, change_type);

-- Product 产品档案模块索引
CREATE INDEX IF NOT EXISTS idx_prd_product_del_flag ON prd_product(del_flag);
CREATE INDEX IF NOT EXISTS idx_prd_product_category ON prd_product(category_id, del_flag);
CREATE INDEX IF NOT EXISTS idx_prd_brand_del_flag ON prd_brand(del_flag);
CREATE INDEX IF NOT EXISTS idx_prd_supplier_del_flag ON prd_supplier(del_flag);
CREATE INDEX IF NOT EXISTS idx_prd_category_parent ON prd_category(parent_id, del_flag);
