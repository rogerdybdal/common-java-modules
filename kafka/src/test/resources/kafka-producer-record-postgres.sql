CREATE SEQUENCE KAFKA_PRODUCER_RECORD_ID_SEQ;

CREATE TABLE KAFKA_PRODUCER_RECORD (
    ID                      BIGINT NOT NULL PRIMARY KEY,
    TOPIC                   VARCHAR(100) NOT NULL,
    KEY                     BYTEA,
    VALUE                   BYTEA,
    HEADERS_JSON            TEXT,
    CREATED_AT              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);