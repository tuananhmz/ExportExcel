CREATE DATABASE IF NOT EXISTS movie_system;

USE movie_system;

CREATE TABLE IF NOT EXISTS info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    employment_type VARCHAR(255),
    status VARCHAR(255),
    join_date VARCHAR(255),
    registered_department VARCHAR(255),
    new_department VARCHAR(255),
    position1 VARCHAR(255),
    position2 VARCHAR(255),
    job VARCHAR(255),
    employee_number VARCHAR(255),
    detail_work LONGTEXT
);
