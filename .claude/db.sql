-- 기존 테이블 (기존 구조 유지)
CREATE TABLE members (
                         id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         username VARCHAR(100) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL,
                         nickname VARCHAR(15) NOT NULL UNIQUE,
                         role VARCHAR(20) NOT NULL,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE products (
                          id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          member_id BIGINT UNSIGNED NOT NULL,  -- 판매자(관리자) ID
                          product_name VARCHAR(50) NOT NULL,
                          description TEXT NOT NULL,
                          category VARCHAR(30) NOT NULL,
                          price INT UNSIGNED NOT NULL CHECK (price > 0 AND price < 10000000),
                          stock_quantity INT NOT NULL CHECK (stock_quantity >= 0 AND stock_quantity < 100000000),
                          product_status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE',
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE orders (
                        id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        member_id BIGINT UNSIGNED NOT NULL,
                        order_number VARCHAR(50) NOT NULL UNIQUE,
                        total_price INT UNSIGNED NOT NULL CHECK (total_price > 0 AND total_price < 1000000000),
                        order_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        shipping_address VARCHAR(100) NOT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE order_item (
                            id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                            order_id BIGINT UNSIGNED NOT NULL,
                            product_id BIGINT UNSIGNED NOT NULL,
                            product_name VARCHAR(50) NOT NULL ,
                            price INT NOT NULL CHECK (price > 0 AND price < 10000000),
                            quantity INT UNSIGNED NOT NULL CHECK (quantity > 0 AND quantity < 10000),
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE carts (
                       id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       member_id BIGINT UNSIGNED NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE cart_item (
                           id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           cart_id BIGINT UNSIGNED NOT NULL,
                           product_id BIGINT UNSIGNED NOT NULL,
                           quantity INT NOT NULL CHECK (quantity > 0 AND quantity < 10000),
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 신규 테이블: 배송지 관리
CREATE TABLE shipping_addresses (
                                    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                    member_id BIGINT UNSIGNED NOT NULL,
                                    recipient_name VARCHAR(50) NOT NULL,
                                    phone VARCHAR(20) NOT NULL,
                                    postcode VARCHAR(10) NOT NULL,
                                    address1 VARCHAR(100) NOT NULL,
                                    address2 VARCHAR(50),
                                    is_default BOOLEAN NOT NULL DEFAULT FALSE,
                                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE wishlists (
                           id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           member_id BIGINT UNSIGNED NOT NULL,
                           product_id BIGINT UNSIGNED NOT NULL,
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           UNIQUE KEY uk_member_product (member_id, product_id)
);

-- 신규 테이블: 최근 본 상품 (로그인 회원만 DB 저장)
CREATE TABLE recent_views (
                              id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              member_id BIGINT UNSIGNED NOT NULL,
                              product_id BIGINT UNSIGNED NOT NULL,
                              viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              UNIQUE KEY uk_member_product (member_id, product_id),
                              INDEX idx_member_viewed (member_id, viewed_at)
);

-- 신규 테이블: 상품 리뷰
CREATE TABLE reviews (
                         id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         product_id BIGINT UNSIGNED NOT NULL,
                         member_id BIGINT UNSIGNED NOT NULL,
                         order_item_id BIGINT UNSIGNED NULL,
                         rating TINYINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         content VARCHAR(500) NOT NULL,
                         image_urls JSON NULL CHECK ( JSON_LENGTH(image_urls) <= 5),  -- ["url1", "url2", ...] 최대 5장 정도
                         is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 외래 키 제약 조건
ALTER TABLE products ADD CONSTRAINT fk_products_members
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE orders ADD CONSTRAINT fk_orders_members
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE order_item ADD CONSTRAINT fk_order_item_orders
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

ALTER TABLE order_item ADD CONSTRAINT fk_order_item_products
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

ALTER TABLE carts ADD CONSTRAINT fk_carts_members
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE cart_item ADD CONSTRAINT fk_cart_item_carts
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE;

ALTER TABLE cart_item ADD CONSTRAINT fk_cart_item_products
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

ALTER TABLE shipping_addresses ADD CONSTRAINT fk_shipping_members
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE wishlists ADD CONSTRAINT fk_wishlists_members
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE wishlists ADD CONSTRAINT fk_wishlists_products
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

ALTER TABLE recent_views ADD CONSTRAINT fk_recent_members
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE recent_views ADD CONSTRAINT fk_recent_products
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

ALTER TABLE reviews ADD CONSTRAINT fk_reviews_products
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

ALTER TABLE reviews ADD CONSTRAINT fk_reviews_members
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE reviews ADD CONSTRAINT fk_reviews_order_item
    FOREIGN KEY (order_item_id) REFERENCES order_item(id) ON DELETE SET NULL;

ALTER TABLE products MODIFY COLUMN description TEXT NOT NULL
