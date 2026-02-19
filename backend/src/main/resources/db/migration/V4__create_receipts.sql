CREATE TABLE IF NOT EXISTS shipments (
                                         id BIGSERIAL PRIMARY KEY,
                                         number VARCHAR(50) NOT NULL UNIQUE,
    client_id BIGINT NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    state VARCHAR(20) NOT NULL DEFAULT 'DRAFT',

    CONSTRAINT fk_shipments_client FOREIGN KEY (client_id) REFERENCES clients(id)
    );

CREATE TABLE IF NOT EXISTS shipment_items (
                                              id BIGSERIAL PRIMARY KEY,
                                              shipment_id BIGINT NOT NULL,
                                              resource_id BIGINT NOT NULL,
                                              unit_id BIGINT NOT NULL,
                                              quantity NUMERIC(19,3) NOT NULL,

    CONSTRAINT fk_shipment_item_shipment FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    CONSTRAINT fk_shipment_item_resource FOREIGN KEY (resource_id) REFERENCES resources(id),
    CONSTRAINT fk_shipment_item_unit FOREIGN KEY (unit_id) REFERENCES units(id),

    CONSTRAINT uq_shipment_item UNIQUE (shipment_id, resource_id, unit_id)
    );