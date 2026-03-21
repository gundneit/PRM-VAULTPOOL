INSERT INTO users (id, firebase_uid, full_name, email, phone, status)
VALUES
    (9001, 'dev-admin-uid', 'Dev Admin', 'admin.test@lavela.local', '0900000001', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    firebase_uid = VALUES(firebase_uid),
    full_name = VALUES(full_name),
    email = VALUES(email),
    phone = VALUES(phone),
    status = VALUES(status);

INSERT INTO user_roles (user_id, role)
VALUES
    (9001, 'ADMIN')
ON DUPLICATE KEY UPDATE
    role = VALUES(role);

INSERT INTO pools (id, name, address, geo_lat, geo_lng, description, open_hours, status)
VALUES
    (1, 'La Vela Pool - Ocean View', '280 Vo Nguyen Giap, Da Nang', 16.0544, 108.2022,
     'Ho boi view bien, phu hop gia dinh va nhom ban.', '06:00-21:00', 'ACTIVE'),
    (2, 'La Vela Pool - Garden Deck', '12 Tran Phu, Nha Trang', 12.2388, 109.1967,
     'Ho boi san vuon, khong gian yen tinh, phu hop nghi duong.', '06:30-20:30', 'ACTIVE'),
    (3, 'La Vela Pool - Skyline Rooftop', '45 Bach Dang, Ho Chi Minh City', 10.7769, 106.7009,
     'Ho boi rooftop voi suc chua lon, phu hop su kien cuoi tuan.', '07:00-22:00', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    address = VALUES(address),
    geo_lat = VALUES(geo_lat),
    geo_lng = VALUES(geo_lng),
    description = VALUES(description),
    open_hours = VALUES(open_hours),
    status = VALUES(status);

INSERT INTO pool_images (id, pool_id, image_url, sort_order)
VALUES
    (101, 1, 'https://images.unsplash.com/photo-1576013551627-0b744bca024c?auto=format&fit=crop&w=1200&q=80', 1),
    (102, 1, 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=1200&q=80', 2),
    (201, 2, 'https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=1200&q=80', 1),
    (202, 2, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80', 2),
    (301, 3, 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80', 1),
    (302, 3, 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1200&q=80', 2)
ON DUPLICATE KEY UPDATE
    pool_id = VALUES(pool_id),
    image_url = VALUES(image_url),
    sort_order = VALUES(sort_order);

INSERT INTO slots (id, pool_id, start_time, end_time, capacity_total, capacity_available, price, status, version)
VALUES
    (1001, 1, TIMESTAMP(CURDATE(), '06:00:00'), TIMESTAMP(CURDATE(), '08:00:00'), 30, 30, 150000.00, 'ACTIVE', 0),
    (1002, 1, TIMESTAMP(CURDATE(), '08:30:00'), TIMESTAMP(CURDATE(), '10:30:00'), 30, 24, 180000.00, 'ACTIVE', 0),
    (1003, 1, TIMESTAMP(CURDATE(), '16:00:00'), TIMESTAMP(CURDATE(), '18:00:00'), 30, 12, 220000.00, 'ACTIVE', 0),
    (1101, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '06:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00'), 30, 30, 160000.00, 'ACTIVE', 0),
    (1102, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:30:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00'), 30, 26, 190000.00, 'ACTIVE', 0),
    (1103, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '16:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00'), 30, 15, 230000.00, 'ACTIVE', 0),

    (2001, 2, TIMESTAMP(CURDATE(), '07:00:00'), TIMESTAMP(CURDATE(), '09:00:00'), 20, 20, 120000.00, 'ACTIVE', 0),
    (2002, 2, TIMESTAMP(CURDATE(), '09:30:00'), TIMESTAMP(CURDATE(), '11:30:00'), 20, 18, 140000.00, 'ACTIVE', 0),
    (2003, 2, TIMESTAMP(CURDATE(), '15:00:00'), TIMESTAMP(CURDATE(), '17:00:00'), 20, 9, 170000.00, 'ACTIVE', 0),
    (2101, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00'), 20, 20, 125000.00, 'ACTIVE', 0),
    (2102, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:30:00'), 20, 16, 145000.00, 'ACTIVE', 0),
    (2103, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '17:00:00'), 20, 11, 175000.00, 'ACTIVE', 0),

    (3001, 3, TIMESTAMP(CURDATE(), '08:00:00'), TIMESTAMP(CURDATE(), '10:00:00'), 40, 40, 250000.00, 'ACTIVE', 0),
    (3002, 3, TIMESTAMP(CURDATE(), '10:30:00'), TIMESTAMP(CURDATE(), '12:30:00'), 40, 31, 280000.00, 'ACTIVE', 0),
    (3003, 3, TIMESTAMP(CURDATE(), '18:00:00'), TIMESTAMP(CURDATE(), '20:00:00'), 40, 6, 320000.00, 'ACTIVE', 0),
    (3101, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), 40, 40, 255000.00, 'ACTIVE', 0),
    (3102, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '12:30:00'), 40, 28, 285000.00, 'ACTIVE', 0),
    (3103, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00'), TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '20:00:00'), 40, 10, 325000.00, 'ACTIVE', 0)
ON DUPLICATE KEY UPDATE
    pool_id = VALUES(pool_id),
    start_time = VALUES(start_time),
    end_time = VALUES(end_time),
    capacity_total = VALUES(capacity_total),
    capacity_available = VALUES(capacity_available),
    price = VALUES(price),
    status = VALUES(status),
    version = VALUES(version);