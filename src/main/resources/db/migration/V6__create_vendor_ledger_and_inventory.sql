ALTER TABLE vendors
    ADD COLUMN current_balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00;

CREATE TABLE vendor_ledger (
    id BIGSERIAL PRIMARY KEY,
    entry_date DATE NOT NULL,
    entry_type VARCHAR(20) NOT NULL,
    vendor_id BIGINT NOT NULL,
    vendor_name VARCHAR(200),
    debit NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    credit NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    purchase_order_id BIGINT
);

CREATE INDEX idx_vendor_ledger_vendor_id ON vendor_ledger (vendor_id);
CREATE INDEX idx_vendor_ledger_entry_date ON vendor_ledger (entry_date);
CREATE UNIQUE INDEX uq_vendor_ledger_purchase_order
    ON vendor_ledger (purchase_order_id)
    WHERE purchase_order_id IS NOT NULL;

CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    product_name VARCHAR(200) NOT NULL,
    unit VARCHAR(30),
    current_stock NUMERIC(14, 3) NOT NULL DEFAULT 0,
    stock_value NUMERIC(14, 2) NOT NULL DEFAULT 0.00
);
