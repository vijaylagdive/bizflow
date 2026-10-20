CREATE TABLE purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    qty NUMERIC(14, 3) NOT NULL,
    unit VARCHAR(30),
    rate NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_purchase_order_items_purchase_order
        FOREIGN KEY (purchase_order_id)
        REFERENCES purchase_orders (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_purchase_order_items_purchase_order_id
    ON purchase_order_items (purchase_order_id);
