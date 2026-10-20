CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,

    purchase_date DATE NOT NULL,
    invoice_number VARCHAR(100),

    vendor_id BIGINT NOT NULL,
    vendor_name VARCHAR(200),

    purchase_total_amount NUMERIC(14, 2) NOT NULL
        DEFAULT 0.00,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION set_purchase_orders_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_purchase_orders_updated_at
BEFORE UPDATE ON purchase_orders
FOR EACH ROW
EXECUTE FUNCTION set_purchase_orders_updated_at();
