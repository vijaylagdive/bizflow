CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    account_type VARCHAR(20) NOT NULL
);

INSERT INTO accounts (name, account_type) VALUES ('Cash', 'cash');

CREATE TABLE cashbook (
    id BIGSERIAL PRIMARY KEY,
    entry_date DATE NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    account_id BIGINT NOT NULL,
    account VARCHAR(200) NOT NULL,
    from_to VARCHAR(20) NOT NULL,
    party_id BIGINT,
    party_name VARCHAR(200),
    amount NUMERIC(14, 2) NOT NULL,
    balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    remark VARCHAR(500),
    CONSTRAINT fk_cashbook_account
        FOREIGN KEY (account_id)
        REFERENCES accounts (account_id)
);

CREATE INDEX idx_cashbook_account_id ON cashbook (account_id);
CREATE INDEX idx_cashbook_entry_date ON cashbook (entry_date);

ALTER TABLE vendor_ledger
    ADD COLUMN cashbook_id BIGINT;

CREATE UNIQUE INDEX uq_vendor_ledger_cashbook
    ON vendor_ledger (cashbook_id)
    WHERE cashbook_id IS NOT NULL;

ALTER TABLE customer_ledger
    ADD COLUMN cashbook_id BIGINT;

CREATE UNIQUE INDEX uq_customer_ledger_cashbook
    ON customer_ledger (cashbook_id)
    WHERE cashbook_id IS NOT NULL;
