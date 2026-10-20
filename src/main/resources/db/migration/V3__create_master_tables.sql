CREATE TABLE vendors (
    vendor_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    contact_number VARCHAR(30)
);

CREATE TABLE product_categories (
    category_id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(200) NOT NULL
);

CREATE TABLE units (
    unit_id BIGSERIAL PRIMARY KEY,
    unit_name VARCHAR(30) NOT NULL
);

CREATE TABLE products (
    product_id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES product_categories (category_id)
);

CREATE INDEX idx_products_category_id ON products (category_id);
