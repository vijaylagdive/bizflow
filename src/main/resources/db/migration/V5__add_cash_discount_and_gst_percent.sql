ALTER TABLE purchase_orders
    ADD COLUMN cash_discount NUMERIC(14, 2) NOT NULL DEFAULT 0.00;

ALTER TABLE purchase_order_items
    ADD COLUMN gst_percent NUMERIC(5, 2) NOT NULL DEFAULT 0.00;
