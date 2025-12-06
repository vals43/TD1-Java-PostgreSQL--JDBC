CREATE TABLE product
(
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(255),
    price             NUMERIC(10, 2),
    creation_datetime TIMESTAMP
);

CREATE TABLE product_category
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255),
    product_id INT REFERENCES product (id)
);