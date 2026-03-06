-- Initialize database schemas
CREATE DATABASE IF NOT EXISTS ecommerce;
USE ecommerce;

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Order Items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity) VALUES
('Laptop Pro 15', 'High-performance laptop with 16GB RAM', 1299.99, 25),
('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 29.99, 100),
('Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 89.99, 50),
('USB-C Hub', '7-in-1 USB-C hub with HDMI and SD card reader', 45.99, 75),
('Laptop Stand', 'Adjustable aluminum laptop stand', 39.99, 60),
('Webcam HD', '1080p HD webcam with built-in microphone', 69.99, 40),
('Headphones Pro', 'Noise-cancelling over-ear headphones', 199.99, 30),
('Monitor 27"', '27-inch 4K UHD monitor', 399.99, 15);

-- Insert admin user (password: admin123)
-- Password is BCrypt hash of 'admin123'
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$pfxx6yCJxh1NXvQXjwZoY.jzK.HvK/OdtLQcwy/XkF07lr1D9SIzm', 'admin@example.com', 'ADMIN');

-- Insert regular user (password: user123)
INSERT INTO users (username, password, email, role) VALUES
('user', '$2a$10$4s9mpLNcufYBJ6nA/Y3kKe8cxdaI.eqOLxQn3fxWKMmUtWBJKWtFK', 'user@example.com', 'USER');