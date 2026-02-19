CREATE TABLE IF NOT EXISTS receipts (
                                        id BIGSERIAL PRIMARY KEY,
                                        number VARCHAR(50) NOT NULL UNIQUE,
    date DATE NOT NULL DEFAULT CURRENT_DATE
    );

CREATE TABLE IF NOT EXISTS receipt_items (
                                             id BIGSERIAL PRIMARY KEY,
                                             receipt_id BIGINT NOT NULL,
                                             resource_id BIGINT NOT NULL,
                                             unit_id BIGINT NOT NULL,
                                             quantity NUMERIC(19,3) NOT NULL,

    CONSTRAINT fk_receipt_item_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(id) ON DELETE CASCADE,
    CONSTRAINT fk_receipt_item_resource FOREIGN KEY (resource_id) REFERENCES resources(id),
    CONSTRAINT fk_receipt_item_unit FOREIGN KEY (unit_id) REFERENCES units(id),

    CONSTRAINT uq_receipt_item UNIQUE (receipt_id, resource_id, unit_id)
    );