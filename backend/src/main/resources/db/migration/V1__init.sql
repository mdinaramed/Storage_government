CREATE TABLE IF NOT EXISTS units (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL UNIQUE,
    state VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    );

CREATE TABLE IF NOT EXISTS resources (
                                         id BIGSERIAL PRIMARY KEY,
                                         name VARCHAR(255) NOT NULL UNIQUE,
    state VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    );

CREATE TABLE IF NOT EXISTS clients (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255),
    state VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    );

CREATE TABLE IF NOT EXISTS balances (
                                        id BIGSERIAL PRIMARY KEY,
                                        resource_id BIGINT NOT NULL,
                                        unit_id BIGINT NOT NULL,
                                        amount NUMERIC(19,3) NOT NULL DEFAULT 0,

    CONSTRAINT uq_balance_resource_unit UNIQUE (resource_id, unit_id),
    CONSTRAINT fk_balance_resource FOREIGN KEY (resource_id) REFERENCES resources(id),
    CONSTRAINT fk_balance_unit FOREIGN KEY (unit_id) REFERENCES units(id)
    );