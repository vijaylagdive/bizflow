CREATE TABLE customers (
    customer_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    contact_number VARCHAR(30),
    current_balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE sales_orders (
    id BIGSERIAL PRIMARY KEY,
    sales_date DATE NOT NULL,
    invoice_number VARCHAR(100),
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(200),
    cash_discount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    sales_total_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION set_sales_orders_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sales_orders_updated_at
BEFORE UPDATE ON sales_orders
FOR EACH ROW
EXECUTE FUNCTION set_sales_orders_updated_at();

CREATE TABLE sales_order_items (
    id BIGSERIAL PRIMARY KEY,
    sales_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    qty NUMERIC(14, 3) NOT NULL,
    unit VARCHAR(30),
    rate NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    gst_percent NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_sales_order_items_sales_order
        FOREIGN KEY (sales_order_id)
        REFERENCES sales_orders (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_sales_order_items_sales_order_id ON sales_order_items (sales_order_id);

CREATE TABLE customer_ledger (
    id BIGSERIAL PRIMARY KEY,
    entry_date DATE NOT NULL,
    entry_type VARCHAR(20) NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(200),
    sale_order_no VARCHAR(100),
    debit NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    credit NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    sales_order_id BIGINT
);

CREATE INDEX idx_customer_ledger_customer_id ON customer_ledger (customer_id);
CREATE INDEX idx_customer_ledger_entry_date ON customer_ledger (entry_date);
CREATE UNIQUE INDEX uq_customer_ledger_sales_order
    ON customer_ledger (sales_order_id)
    WHERE sales_order_id IS NOT NULL;
