CREATE DATABASE product_management_db;

CREATE USER product_manager_user WITH PASSWORD '123456';

GRANT ALL PRIVILEGES ON DATABASE product_management_db TO product_manager_user;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO product_manager_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO product_manager_user;

ALTER TABLE product OWNER TO product_manager_user;
ALTER TABLE product_category OWNER TO product_manager_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO product_manager_user;
