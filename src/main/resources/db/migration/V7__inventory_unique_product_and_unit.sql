ALTER TABLE inventory
    DROP CONSTRAINT IF EXISTS inventory_product_id_key;

CREATE UNIQUE INDEX uq_inventory_product_unit
    ON inventory (product_id, COALESCE(unit, ''));
