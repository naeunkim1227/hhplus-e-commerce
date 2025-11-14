-- test_coupons.sql
INSERT INTO coupons (code, name, total_quantity, issued_quantity, start_date, end_date, status, type, discount_rate, version, created_at, updated_at) VALUES
('WELCOME10', '신규가입 10% 할인', 100, 15, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), 'ACTIVE', 'RATE', 0.10, 0, NOW(), NOW()),
('SUMMER20', '여름맞이 20% 할인', 200, 15, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 'ACTIVE', 'RATE', 0.20, 0, NOW(), NOW()),
('VIP15', 'VIP 회원 15% 할인', 50, 15, NOW(), DATE_ADD(NOW(), INTERVAL 120 DAY), 'ACTIVE', 'RATE', 0.15, 0, NOW(), NOW()),
('FIXED20000', '20000원 할인', 300, 15, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', 'FIXED', 0.05, 0, NOW(), NOW()),
('FIXED10000', '10000원 할인', 100, 15, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'ACTIVE', 'FIXED', 0.30, 0, NOW(), NOW());
