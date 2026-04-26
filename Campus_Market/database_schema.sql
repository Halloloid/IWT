-- =============================================
-- Campus Second-Hand Marketplace - DB Schema
-- =============================================

-- Step 1: Create the database
CREATE DATABASE campus_market;

-- Step 2: Connect to it
-- \c campus_market;

-- Step 3: Users table
CREATE TABLE users (
    sic           CHAR(7) PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    phone         VARCHAR(15)  NOT NULL,
    hostel_block  VARCHAR(50),
    year_of_study INTEGER,
    branch        VARCHAR(50),
	occupation    VARCHAR(10) DEFAULT 'seller',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE items (
    item_id        SERIAL PRIMARY KEY,
    seller_email   VARCHAR(100) NOT NULL,
    title          VARCHAR(150) NOT NULL,
    description    TEXT NOT NULL,
    price          NUMERIC(10,2) NOT NULL,
    category       VARCHAR(50)  NOT NULL,
    condition_type VARCHAR(20),
    image_path     VARCHAR(255) NOT NULL,
    status         VARCHAR(20)  DEFAULT 'available',
    posted_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_email) REFERENCES users(email) ON DELETE CASCADE
);

CREATE INDEX idx_items_seller   ON items(seller_email);
CREATE INDEX idx_items_category ON items(category);
CREATE INDEX idx_items_status   ON items(status);

-- =============================================
-- Quick verification queries
-- =============================================
-- \dt                         -- list tables
-- SELECT * FROM users;
-- SELECT * FROM items;
