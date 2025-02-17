CREATE EXTENSION IF NOT EXISTS moddatetime;

CREATE TABLE dns_server
(
    id           SERIAL PRIMARY KEY,
    ip           inet UNIQUE NOT NULL,
    country      varchar(255),
    country_code varchar(255),
    region       varchar(255),
    region_name  varchar(255),
    city         varchar(255),
    isp          varchar(255),
    org          varchar(255),
    as_info      varchar(255),
    domain       varchar(128),
    longitude    real,
    latitude     real,
    fail_count   BIGINT,
    tag          varchar(256),
    update_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER dns_server_update_at
    BEFORE UPDATE ON dns_server
    FOR EACH ROW
    EXECUTE PROCEDURE moddatetime (update_at);