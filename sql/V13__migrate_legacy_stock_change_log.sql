-- TPC CommerceInsight
-- V13: migrate legacy Docker volumes from change_reason to change_type.
--
-- Execution contract (sql/ remains the only source of business SQL):
--   New database: apply V1 through V13 in ascending numeric order. V11 sample
--                 data and V12 EXPLAIN validation may be skipped when not needed;
--                 still apply V13 last as an idempotent schema check.
--   Existing database/volume: apply V13, then re-apply V8 so all trigger
--                 definitions are refreshed from the canonical SQL asset.
--   Verification: run \d stock_change_log and confirm change_type exists and
--                 change_reason does not; update one stock.s_quantity row and
--                 confirm the trigger inserts a stock_change_log row.
--   Non-destructive trigger check (replace the keys with an existing stock row):
--                 BEGIN;
--                 SELECT set_config('app.change_type', 'manual_adjustment', true);
--                 UPDATE stock SET s_quantity = s_quantity + 1
--                 WHERE s_w_id = 1 AND s_i_id = 1001;
--                 SELECT * FROM stock_change_log ORDER BY log_id DESC LIMIT 1;
--                 ROLLBACK;

BEGIN;

DO $$
DECLARE
    v_constraint record;
    v_has_change_type boolean;
    v_has_change_reason boolean;
BEGIN
    IF to_regclass('public.stock_change_log') IS NULL THEN
        RAISE EXCEPTION 'stock_change_log does not exist; apply V1 through V4 before V13';
    END IF;

    SELECT EXISTS (
        SELECT 1
        FROM pg_attribute
        WHERE attrelid = 'public.stock_change_log'::regclass
          AND attname = 'change_type'
          AND NOT attisdropped
    ) INTO v_has_change_type;

    SELECT EXISTS (
        SELECT 1
        FROM pg_attribute
        WHERE attrelid = 'public.stock_change_log'::regclass
          AND attname = 'change_reason'
          AND NOT attisdropped
    ) INTO v_has_change_reason;

    -- Drop the canonical constraint and any legacy CHECK that still references
    -- change_reason. Recreating it below makes the result independent of names
    -- used by older Docker volumes.
    FOR v_constraint IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'public.stock_change_log'::regclass
          AND contype = 'c'
          AND (
              conname = 'ck_stock_change_log_type_valid'
              OR position('change_type' in pg_get_constraintdef(oid)) > 0
              OR position('change_reason' in pg_get_constraintdef(oid)) > 0
          )
    LOOP
        EXECUTE format(
            'ALTER TABLE public.stock_change_log DROP CONSTRAINT %I',
            v_constraint.conname
        );
    END LOOP;

    IF v_has_change_reason AND NOT v_has_change_type THEN
        ALTER TABLE public.stock_change_log
            RENAME COLUMN change_reason TO change_type;
    ELSIF v_has_change_reason AND v_has_change_type THEN
        UPDATE public.stock_change_log
        SET change_type = change_reason
        WHERE change_type IS NULL;

        ALTER TABLE public.stock_change_log
            DROP COLUMN change_reason;
    ELSIF NOT v_has_change_type THEN
        ALTER TABLE public.stock_change_log
            ADD COLUMN change_type varchar(50);
    END IF;

    UPDATE public.stock_change_log
    SET change_type = 'new_order'
    WHERE change_type IS NULL;

    ALTER TABLE public.stock_change_log
        ALTER COLUMN change_type TYPE varchar(50),
        ALTER COLUMN change_type SET NOT NULL;

    ALTER TABLE public.stock_change_log
        ADD CONSTRAINT ck_stock_change_log_type_valid
        CHECK (change_type IN ('new_order', 'manual_adjustment', 'rollback'));
END;
$$;

-- Recreate the canonical trigger function as part of the standalone old-volume
-- upgrade. Only app.change_type and stock_change_log.change_type are supported.
CREATE OR REPLACE FUNCTION fn_log_stock_quantity_change()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_transaction_id varchar(64);
    v_change_type varchar(50);
BEGIN
    IF NEW.s_quantity IS DISTINCT FROM OLD.s_quantity THEN
        v_transaction_id := NULLIF(current_setting('app.transaction_id', true), '');
        v_change_type := COALESCE(
            NULLIF(current_setting('app.change_type', true), ''),
            'new_order'
        );

        INSERT INTO stock_change_log (
            warehouse_id,
            item_id,
            old_quantity,
            new_quantity,
            change_quantity,
            change_type,
            related_transaction_id
        ) VALUES (
            NEW.s_w_id,
            NEW.s_i_id,
            OLD.s_quantity,
            NEW.s_quantity,
            NEW.s_quantity - OLD.s_quantity,
            v_change_type,
            v_transaction_id
        );
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_stock_quantity_change ON stock;

CREATE TRIGGER trg_stock_quantity_change
AFTER UPDATE OF s_quantity ON stock
FOR EACH ROW
WHEN (OLD.s_quantity IS DISTINCT FROM NEW.s_quantity)
EXECUTE FUNCTION fn_log_stock_quantity_change();

COMMIT;
