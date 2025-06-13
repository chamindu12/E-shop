CREATE TABLE IF NOT EXISTS inventory_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id VARCHAR(100),
    item_name VARCHAR(255),
    item_image VARCHAR(255),
    item_category VARCHAR(100),
    item_qty VARCHAR(50),
    item_details TEXT
    );

-- UserModel table
CREATE TABLE IF NOT EXISTS user_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(200),
    email VARCHAR(150) UNIQUE,
    password VARCHAR(255),
    phone VARCHAR(50)
    );
