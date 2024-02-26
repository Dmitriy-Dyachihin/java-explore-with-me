DROP TABLE IF EXISTS hits;

CREATE TABLE IF NOT EXISTS hits (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app VARCHAR(32) NOT NULL,
    uri VARCHAR(128) NOT NULL,
    ip VARCHAR(16) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);