CREATE TABLE import_status (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NULL,
    creation_time TIMESTAMP NULL,
    start_time TIMESTAMP,
    finish_time TIMESTAMP,
    processed_rows BIGINT  NULL DEFAULT 0
);