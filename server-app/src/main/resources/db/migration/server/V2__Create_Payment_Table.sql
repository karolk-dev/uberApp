CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT NOT NULL,
    payment_intent_id VARCHAR(255),
    amount BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50),
    payment_method VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_ride
        FOREIGN KEY (ride_id)
        REFERENCES ride(id)
        ON DELETE CASCADE
);