-- TPC CommerceInsight
-- Optional committed TPC-C demo transaction data.
-- Run after V1 -> V2 -> V3 -> V4 -> V8 -> V11.
--
-- V11 intentionally keeps tpcc_orders/new_order/order_line/history empty after
-- rollback-only checks. This script persists one New-Order and one Payment demo
-- so B/C can inspect committed transaction results in a local database.

BEGIN;
SELECT set_config('app.transaction_id', 'demo-new-order', true);
SELECT set_config('app.change_type', 'new_order', true);

DO $$
DECLARE
    v_order_id integer;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM transaction_log
        WHERE transaction_id = 'demo-new-order'
    ) THEN
        RAISE NOTICE 'demo-new-order already exists, skip committed New-Order demo';
        RETURN;
    END IF;

    UPDATE district
    SET d_next_o_id = d_next_o_id + 1
    WHERE d_w_id = 1
      AND d_id = 1
    RETURNING d_next_o_id - 1 INTO v_order_id;

    IF v_order_id IS NULL THEN
        RAISE EXCEPTION 'demo district not found: w_id=1, d_id=1';
    END IF;

    INSERT INTO tpcc_orders (
        o_id, o_d_id, o_w_id, o_c_id, o_entry_d, o_carrier_id, o_ol_cnt, o_all_local
    ) VALUES (
        v_order_id, 1, 1, 1, current_timestamp, NULL, 1, 1
    );

    INSERT INTO new_order (
        no_o_id, no_d_id, no_w_id
    ) VALUES (
        v_order_id, 1, 1
    );

    UPDATE stock
    SET
        s_quantity = s_quantity - 2,
        s_ytd = s_ytd + 2,
        s_order_cnt = s_order_cnt + 1
    WHERE s_w_id = 1
      AND s_i_id = 1001
      AND s_quantity >= 2;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'demo stock not found or insufficient: w_id=1, i_id=1001';
    END IF;

    INSERT INTO order_line (
        ol_o_id, ol_d_id, ol_w_id, ol_number, ol_i_id, ol_supply_w_id,
        ol_delivery_d, ol_quantity, ol_amount, ol_dist_info
    ) VALUES (
        v_order_id, 1, 1, 1, 1001, 1, NULL, 2, 50.20, 'dist-info-01'
    );

    INSERT INTO transaction_log (
        transaction_id, transaction_type, status, elapsed_ms, error_message, executed_by
    ) VALUES (
        'demo-new-order', 'new_order', 'committed', 0, NULL, 1
    );
END $$;

COMMIT;

BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM transaction_log
        WHERE transaction_id = 'demo-payment'
    ) THEN
        RAISE NOTICE 'demo-payment already exists, skip committed Payment demo';
        RETURN;
    END IF;

    UPDATE warehouse
    SET w_ytd = w_ytd + 25.00
    WHERE w_id = 1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'demo warehouse not found: w_id=1';
    END IF;

    UPDATE district
    SET d_ytd = d_ytd + 25.00
    WHERE d_w_id = 1
      AND d_id = 1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'demo district not found: w_id=1, d_id=1';
    END IF;

    UPDATE tpcc_customer
    SET
        c_balance = c_balance - 25.00,
        c_ytd_payment = c_ytd_payment + 25.00,
        c_payment_cnt = c_payment_cnt + 1
    WHERE c_w_id = 1
      AND c_d_id = 1
      AND c_id = 1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'demo customer not found: w_id=1, d_id=1, c_id=1';
    END IF;

    INSERT INTO history (
        h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, h_date, h_amount, h_data
    ) VALUES (
        1, 1, 1, 1, 1, current_timestamp, 25.00, 'payment demo'
    );

    INSERT INTO transaction_log (
        transaction_id, transaction_type, status, elapsed_ms, error_message, executed_by
    ) VALUES (
        'demo-payment', 'payment', 'committed', 0, NULL, 1
    );
END $$;

COMMIT;

SELECT
    o.o_w_id AS warehouse_id,
    o.o_d_id AS district_id,
    o.o_id AS order_id,
    o.o_c_id AS customer_id,
    ol.ol_i_id AS item_id,
    ol.ol_quantity AS quantity,
    ol.ol_amount AS amount,
    ol.ol_dist_info AS dist_info
FROM tpcc_orders o
JOIN order_line ol
    ON ol.ol_w_id = o.o_w_id
    AND ol.ol_d_id = o.o_d_id
    AND ol.ol_o_id = o.o_id
WHERE EXISTS (
    SELECT 1
    FROM transaction_log t
    WHERE t.transaction_id = 'demo-new-order'
      AND t.status = 'committed'
)
ORDER BY o.o_w_id, o.o_d_id, o.o_id, ol.ol_number;

SELECT
    c.c_w_id AS warehouse_id,
    c.c_d_id AS district_id,
    c.c_id AS customer_id,
    c.c_balance AS customer_balance,
    c.c_ytd_payment AS customer_ytd_payment,
    c.c_payment_cnt AS customer_payment_count
FROM tpcc_customer c
WHERE c.c_w_id = 1
  AND c.c_d_id = 1
  AND c.c_id = 1;
