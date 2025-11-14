-- test_user_coupons.sql
-- 유저 1 (김민수) - 쿠폰 3개
INSERT INTO user_coupons (user_id, coupon_id, issued_at, used_at, expired_at) VALUES
(1, 1, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 90 DAY)),
(1, 2, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 60 DAY)),
(1, 3, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 120 DAY)),

-- 유저 2 (이영희) - 쿠폰 3개
(2, 1, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 90 DAY)),
(2, 4, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 30 DAY)),
(2, 5, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 7 DAY)),

-- 유저 3 (박철수) - 쿠폰 3개
(3, 2, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 60 DAY)),
(3, 3, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 120 DAY)),
(3, 4, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 30 DAY)),

-- 유저 4 (정지훈) - 쿠폰 3개
(4, 1, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 90 DAY)),
(4, 3, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 120 DAY)),
(4, 5, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 7 DAY)),

-- 유저 5 (최수진) - 쿠폰 3개
(5, 2, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 60 DAY)),
(5, 4, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 30 DAY)),
(5, 5, NOW(), NULL, DATE_ADD(NOW(), INTERVAL 7 DAY));
