-- TPC CommerceInsight
-- V8: triggers for stock change logging and import task auditing.

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
            NULLIF(current_setting('app.change_reason', true), ''),
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

CREATE OR REPLACE FUNCTION fn_audit_import_task_status()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status = 'running' AND NEW.started_at IS NULL THEN
        NEW.started_at := current_timestamp;
    END IF;

    IF NEW.status IN ('success', 'failed') AND NEW.ended_at IS NULL THEN
        NEW.ended_at := current_timestamp;
    END IF;

    IF NEW.status = 'pending' THEN
        NEW.started_at := NULL;
        NEW.ended_at := NULL;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_import_task_audit ON import_task;

CREATE TRIGGER trg_import_task_audit
BEFORE INSERT OR UPDATE OF status ON import_task
FOR EACH ROW
EXECUTE FUNCTION fn_audit_import_task_status();
