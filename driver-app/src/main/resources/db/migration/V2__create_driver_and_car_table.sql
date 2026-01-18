CREATE TABLE driver (
   id BIGSERIAL PRIMARY KEY,
   uuid VARCHAR(255),
   name VARCHAR(255) NOT NULL,
   nip VARCHAR(255) NOT NULL,
   company_name VARCHAR(255) NOT NULL,
   company_status VARCHAR(255) NOT NULL,
   location_latitude NUMERIC(10, 8),
   location_longitude NUMERIC(11, 8),
   is_available BOOLEAN DEFAULT TRUE,
   status VARCHAR(255) NOT NULL DEFAULT 'OFFLINE',
   car_id BIGINT UNIQUE,

   CONSTRAINT fk_car_driver
            FOREIGN KEY (car_id)
            REFERENCES car(id)
            ON DELETE CASCADE
);

