CREATE USER scicms WITH
    PASSWORD 'scicms'
    LOGIN
    NOSUPERUSER
    INHERIT
    NOCREATEDB
    NOCREATEROLE
    NOREPLICATION;

CREATE DATABASE scicms
    WITH
    OWNER = scicms
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE scicms TO scicms;
GRANT TEMPORARY, CONNECT ON DATABASE scicms TO PUBLIC;
