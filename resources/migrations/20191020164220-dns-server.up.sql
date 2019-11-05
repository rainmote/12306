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
    tag_id       integer DEFAULT NULL,
    longitude    real,
    latitude     real,
    last_access  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_useable TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);