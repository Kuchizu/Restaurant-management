CREATE TABLE IF NOT EXISTS tables (
    id BIGSERIAL PRIMARY KEY,
    table_number VARCHAR(50) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    location VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'FREE'
);

CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    table_id BIGINT NOT NULL,
    waiter_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    special_requests VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    dish_name VARCHAR(200),
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    special_request VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_orders_table_id ON orders(table_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- Insert sample data
INSERT INTO employees (first_name, last_name, email, phone, role) VALUES
    ('John', 'Doe', 'john.doe@restaurant.com', '+1234567890', 'WAITER'),
    ('Jane', 'Smith', 'jane.smith@restaurant.com', '+1234567891', 'WAITER'),
    ('Mike', 'Johnson', 'mike.j@restaurant.com', '+1234567892', 'CHEF')
ON CONFLICT DO NOTHING;

INSERT INTO tables (table_number, capacity, location, status) VALUES
    ('T1', 4, 'Main Hall', 'FREE'),
    ('T2', 2, 'Window Side', 'FREE'),
    ('T3', 6, 'Private Room', 'FREE'),
    ('T4', 4, 'Main Hall', 'FREE')
ON CONFLICT DO NOTHING;
