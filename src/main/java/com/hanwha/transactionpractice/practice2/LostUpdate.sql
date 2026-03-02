-- ==============================================
-- 실습 2: Lost Update 재현 및 방어 (SQL)
-- ==============================================

-- 테이블 준비
CREATE TABLE product (
    id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    name  VARCHAR(100),
    stock INT NOT NULL
);
INSERT INTO product (name, stock) VALUES ('노트북', 10);

-- ==============================================
-- [문제 재현] 일반 SELECT → Lost Update 발생
-- ==============================================

-- TX-A (세션 1)
BEGIN;
SELECT stock FROM product WHERE id = 1;  -- 결과: 10
-- (TX-B가 먼저 커밋될 때까지 대기한다고 가정)
UPDATE product SET stock = 10 - 5 WHERE id = 1;  -- 10→5
COMMIT;

-- TX-B (세션 2, 동시에 실행)
BEGIN;
SELECT stock FROM product WHERE id = 1;  -- 결과: 10 (같은 값!)
UPDATE product SET stock = 10 - 3 WHERE id = 1;  -- 10→7
COMMIT;

-- 최종 결과: stock = 5 또는 7 (나중에 커밋한 쪽이 이김)
-- 기대값: 10 - 5 - 3 = 2

-- ==============================================
-- [해결] SELECT ... FOR UPDATE → X-Lock 획득
-- ==============================================

-- TX-A (세션 1)
BEGIN;
SELECT stock FROM product WHERE id = 1 FOR UPDATE;  -- X-Lock 획득, 결과: 10
UPDATE product SET stock = 10 - 5 WHERE id = 1;
COMMIT;  -- X-Lock 해제

-- TX-B (세션 2, TX-A의 FOR UPDATE 때문에 여기서 대기)
BEGIN;
SELECT stock FROM product WHERE id = 1 FOR UPDATE;  -- TX-A 커밋 후 결과: 5
UPDATE product SET stock = 5 - 3 WHERE id = 1;  -- 5→2
COMMIT;

-- 최종 결과: stock = 2 (정확!)
