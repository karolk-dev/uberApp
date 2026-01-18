CREATE TABLE ride (
   id BIGSERIAL PRIMARY KEY,
   uuid VARCHAR(255) NULL,
   client_uuid VARCHAR(255) NULL,
   driver_uuid VARCHAR(255) NULL,
   client_name VARCHAR(255) NULL,
   driver_name VARCHAR(255) NULL,
   pickup_location_latitude DOUBLE PRECISION NOT NULL,
   pickup_location_longitude DOUBLE PRECISION NOT NULL,
   destination_latitude DOUBLE PRECISION NOT NULL,
   destination_longitude DOUBLE PRECISION NOT NULL,
   status VARCHAR(50) NULL,
   product VARCHAR(50) NULL,
   created_at TIMESTAMP NULL,
   updated_at TIMESTAMP NULL,
   search_start_time TIMESTAMP NULL,
   amount INTEGER NULL,
   penalty_amount INTEGER NULL,
   payment_intend_id VARCHAR(255) NULL,
   customer_id VARCHAR(255) NULL,
   currency VARCHAR(3) NULL,
   is_paid BOOLEAN NOT NULL DEFAULT FALSE,
   payment_type VARCHAR(20) NULL,
   polyline VARCHAR NULL,
   polyline_to_client VARCHAR NULL
);

CREATE INDEX idx_ride_uuid ON ride(uuid);
CREATE INDEX idx_ride_driver_uuid ON ride(driver_uuid);
CREATE INDEX idx_ride_client_UUID ON ride(client_uuid);
CREATE INDEX idx_ride_created_at ON ride(created_at);
CREATE INDEX idx_ride_driver_name ON ride(driver_name);
CREATE INDEX idx_ride_client_name ON ride(client_name);