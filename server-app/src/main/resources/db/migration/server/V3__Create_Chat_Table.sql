CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    ride_id VARCHAR(36) NOT NULL,
    sender VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_chat_ride ON chat_messages(ride_id);
CREATE INDEX idx_chat_sender ON chat_messages(sender);