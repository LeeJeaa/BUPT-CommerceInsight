-- TPC CommerceInsight
-- V4: add primary keys, foreign keys, unique constraints, and check constraints.
-- This script is idempotent: re-running it skips constraints that already exist
-- on the target table.

CREATE OR REPLACE FUNCTION public._ci_add_constraint_if_not_exists(
    p_table regclass,
    p_constraint_name text,
    p_constraint_definition text
)
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = p_table
          AND conname = p_constraint_name
    ) THEN
        EXECUTE format(
            'ALTER TABLE %s ADD CONSTRAINT %I %s',
            p_table,
            p_constraint_name,
            p_constraint_definition
        );
    END IF;
END;
$$;

SELECT public._ci_add_constraint_if_not_exists('region'::regclass, 'pk_region', 'PRIMARY KEY (r_regionkey)');
SELECT public._ci_add_constraint_if_not_exists('nation'::regclass, 'pk_nation', 'PRIMARY KEY (n_nationkey)');
SELECT public._ci_add_constraint_if_not_exists('supplier'::regclass, 'pk_supplier', 'PRIMARY KEY (s_suppkey)');
SELECT public._ci_add_constraint_if_not_exists('part'::regclass, 'pk_part', 'PRIMARY KEY (p_partkey)');
SELECT public._ci_add_constraint_if_not_exists('partsupp'::regclass, 'pk_partsupp', 'PRIMARY KEY (ps_partkey, ps_suppkey)');
SELECT public._ci_add_constraint_if_not_exists('customer'::regclass, 'pk_customer', 'PRIMARY KEY (c_custkey)');
SELECT public._ci_add_constraint_if_not_exists('orders'::regclass, 'pk_orders', 'PRIMARY KEY (o_orderkey)');
SELECT public._ci_add_constraint_if_not_exists('lineitem'::regclass, 'pk_lineitem', 'PRIMARY KEY (l_orderkey, l_linenumber)');

SELECT public._ci_add_constraint_if_not_exists('nation'::regclass, 'fk_nation_region', 'FOREIGN KEY (n_regionkey) REFERENCES region(r_regionkey)');
SELECT public._ci_add_constraint_if_not_exists('supplier'::regclass, 'fk_supplier_nation', 'FOREIGN KEY (s_nationkey) REFERENCES nation(n_nationkey)');
SELECT public._ci_add_constraint_if_not_exists('partsupp'::regclass, 'fk_partsupp_part', 'FOREIGN KEY (ps_partkey) REFERENCES part(p_partkey)');
SELECT public._ci_add_constraint_if_not_exists('partsupp'::regclass, 'fk_partsupp_supplier', 'FOREIGN KEY (ps_suppkey) REFERENCES supplier(s_suppkey)');
SELECT public._ci_add_constraint_if_not_exists('customer'::regclass, 'fk_customer_nation', 'FOREIGN KEY (c_nationkey) REFERENCES nation(n_nationkey)');
SELECT public._ci_add_constraint_if_not_exists('orders'::regclass, 'fk_orders_customer', 'FOREIGN KEY (o_custkey) REFERENCES customer(c_custkey)');
SELECT public._ci_add_constraint_if_not_exists('lineitem'::regclass, 'fk_lineitem_orders', 'FOREIGN KEY (l_orderkey) REFERENCES orders(o_orderkey)');
SELECT public._ci_add_constraint_if_not_exists('lineitem'::regclass, 'fk_lineitem_partsupp', 'FOREIGN KEY (l_partkey, l_suppkey) REFERENCES partsupp(ps_partkey, ps_suppkey)');

SELECT public._ci_add_constraint_if_not_exists('orders'::regclass, 'ck_orders_totalprice_non_negative', 'CHECK (o_totalprice >= 0)');
SELECT public._ci_add_constraint_if_not_exists('orders'::regclass, 'ck_orders_orderstatus_valid', 'CHECK (o_orderstatus IN (''O'', ''F'', ''P''))');

SELECT public._ci_add_constraint_if_not_exists('lineitem'::regclass, 'ck_lineitem_quantity_positive', 'CHECK (l_quantity > 0)');
SELECT public._ci_add_constraint_if_not_exists('lineitem'::regclass, 'ck_lineitem_extendedprice_non_negative', 'CHECK (l_extendedprice >= 0)');
SELECT public._ci_add_constraint_if_not_exists('lineitem'::regclass, 'ck_lineitem_discount_range', 'CHECK (l_discount >= 0 AND l_discount <= 1)');
SELECT public._ci_add_constraint_if_not_exists('lineitem'::regclass, 'ck_lineitem_tax_range', 'CHECK (l_tax >= 0 AND l_tax <= 1)');
SELECT public._ci_add_constraint_if_not_exists('lineitem'::regclass, 'ck_lineitem_receipt_after_ship', 'CHECK (l_receiptdate >= l_shipdate)');

SELECT public._ci_add_constraint_if_not_exists('partsupp'::regclass, 'ck_partsupp_availqty_non_negative', 'CHECK (ps_availqty >= 0)');
SELECT public._ci_add_constraint_if_not_exists('partsupp'::regclass, 'ck_partsupp_supplycost_non_negative', 'CHECK (ps_supplycost >= 0)');

SELECT public._ci_add_constraint_if_not_exists('part'::regclass, 'ck_part_size_positive', 'CHECK (p_size > 0)');
SELECT public._ci_add_constraint_if_not_exists('part'::regclass, 'ck_part_retailprice_non_negative', 'CHECK (p_retailprice >= 0)');

SELECT public._ci_add_constraint_if_not_exists('warehouse'::regclass, 'pk_warehouse', 'PRIMARY KEY (w_id)');
SELECT public._ci_add_constraint_if_not_exists('district'::regclass, 'pk_district', 'PRIMARY KEY (d_w_id, d_id)');
SELECT public._ci_add_constraint_if_not_exists('tpcc_customer'::regclass, 'pk_tpcc_customer', 'PRIMARY KEY (c_w_id, c_d_id, c_id)');
SELECT public._ci_add_constraint_if_not_exists('history'::regclass, 'pk_history', 'PRIMARY KEY (h_id)');
SELECT public._ci_add_constraint_if_not_exists('item'::regclass, 'pk_item', 'PRIMARY KEY (i_id)');
SELECT public._ci_add_constraint_if_not_exists('stock'::regclass, 'pk_stock', 'PRIMARY KEY (s_w_id, s_i_id)');
SELECT public._ci_add_constraint_if_not_exists('tpcc_orders'::regclass, 'pk_tpcc_orders', 'PRIMARY KEY (o_w_id, o_d_id, o_id)');
SELECT public._ci_add_constraint_if_not_exists('new_order'::regclass, 'pk_new_order', 'PRIMARY KEY (no_w_id, no_d_id, no_o_id)');
SELECT public._ci_add_constraint_if_not_exists('order_line'::regclass, 'pk_order_line', 'PRIMARY KEY (ol_w_id, ol_d_id, ol_o_id, ol_number)');

SELECT public._ci_add_constraint_if_not_exists('district'::regclass, 'fk_district_warehouse', 'FOREIGN KEY (d_w_id) REFERENCES warehouse(w_id)');
SELECT public._ci_add_constraint_if_not_exists('tpcc_customer'::regclass, 'fk_tpcc_customer_district', 'FOREIGN KEY (c_w_id, c_d_id) REFERENCES district(d_w_id, d_id)');
SELECT public._ci_add_constraint_if_not_exists('history'::regclass, 'fk_history_tpcc_customer', 'FOREIGN KEY (h_c_w_id, h_c_d_id, h_c_id) REFERENCES tpcc_customer(c_w_id, c_d_id, c_id)');
SELECT public._ci_add_constraint_if_not_exists('history'::regclass, 'fk_history_district', 'FOREIGN KEY (h_w_id, h_d_id) REFERENCES district(d_w_id, d_id)');
SELECT public._ci_add_constraint_if_not_exists('stock'::regclass, 'fk_stock_warehouse', 'FOREIGN KEY (s_w_id) REFERENCES warehouse(w_id)');
SELECT public._ci_add_constraint_if_not_exists('stock'::regclass, 'fk_stock_item', 'FOREIGN KEY (s_i_id) REFERENCES item(i_id)');
SELECT public._ci_add_constraint_if_not_exists('tpcc_orders'::regclass, 'fk_tpcc_orders_district', 'FOREIGN KEY (o_w_id, o_d_id) REFERENCES district(d_w_id, d_id)');
SELECT public._ci_add_constraint_if_not_exists('tpcc_orders'::regclass, 'fk_tpcc_orders_tpcc_customer', 'FOREIGN KEY (o_w_id, o_d_id, o_c_id) REFERENCES tpcc_customer(c_w_id, c_d_id, c_id)');
SELECT public._ci_add_constraint_if_not_exists('new_order'::regclass, 'fk_new_order_tpcc_orders', 'FOREIGN KEY (no_w_id, no_d_id, no_o_id) REFERENCES tpcc_orders(o_w_id, o_d_id, o_id)');
SELECT public._ci_add_constraint_if_not_exists('order_line'::regclass, 'fk_order_line_tpcc_orders', 'FOREIGN KEY (ol_w_id, ol_d_id, ol_o_id) REFERENCES tpcc_orders(o_w_id, o_d_id, o_id)');
SELECT public._ci_add_constraint_if_not_exists('order_line'::regclass, 'fk_order_line_stock', 'FOREIGN KEY (ol_supply_w_id, ol_i_id) REFERENCES stock(s_w_id, s_i_id)');

SELECT public._ci_add_constraint_if_not_exists('warehouse'::regclass, 'ck_warehouse_tax_range', 'CHECK (w_tax >= 0 AND w_tax <= 1)');
SELECT public._ci_add_constraint_if_not_exists('warehouse'::regclass, 'ck_warehouse_ytd_non_negative', 'CHECK (w_ytd >= 0)');

SELECT public._ci_add_constraint_if_not_exists('district'::regclass, 'ck_district_tax_range', 'CHECK (d_tax >= 0 AND d_tax <= 1)');
SELECT public._ci_add_constraint_if_not_exists('district'::regclass, 'ck_district_ytd_non_negative', 'CHECK (d_ytd >= 0)');
SELECT public._ci_add_constraint_if_not_exists('district'::regclass, 'ck_district_next_o_id_positive', 'CHECK (d_next_o_id > 0)');

SELECT public._ci_add_constraint_if_not_exists('tpcc_customer'::regclass, 'ck_tpcc_customer_credit_valid', 'CHECK (c_credit IN (''GC'', ''BC''))');
SELECT public._ci_add_constraint_if_not_exists('tpcc_customer'::regclass, 'ck_tpcc_customer_discount_range', 'CHECK (c_discount >= 0 AND c_discount <= 1)');
SELECT public._ci_add_constraint_if_not_exists('tpcc_customer'::regclass, 'ck_tpcc_customer_payment_cnt_non_negative', 'CHECK (c_payment_cnt >= 0)');

SELECT public._ci_add_constraint_if_not_exists('item'::regclass, 'ck_item_price_non_negative', 'CHECK (i_price >= 0)');

SELECT public._ci_add_constraint_if_not_exists('stock'::regclass, 'ck_stock_quantity_non_negative', 'CHECK (s_quantity >= 0)');
SELECT public._ci_add_constraint_if_not_exists('stock'::regclass, 'ck_stock_ytd_non_negative', 'CHECK (s_ytd >= 0)');
SELECT public._ci_add_constraint_if_not_exists('stock'::regclass, 'ck_stock_order_cnt_non_negative', 'CHECK (s_order_cnt >= 0)');
SELECT public._ci_add_constraint_if_not_exists('stock'::regclass, 'ck_stock_remote_cnt_non_negative', 'CHECK (s_remote_cnt >= 0)');

SELECT public._ci_add_constraint_if_not_exists('tpcc_orders'::regclass, 'ck_tpcc_orders_ol_cnt_positive', 'CHECK (o_ol_cnt > 0)');
SELECT public._ci_add_constraint_if_not_exists('tpcc_orders'::regclass, 'ck_tpcc_orders_all_local_valid', 'CHECK (o_all_local IN (0, 1))');

SELECT public._ci_add_constraint_if_not_exists('order_line'::regclass, 'ck_order_line_quantity_positive', 'CHECK (ol_quantity > 0)');
SELECT public._ci_add_constraint_if_not_exists('order_line'::regclass, 'ck_order_line_amount_non_negative', 'CHECK (ol_amount >= 0)');

SELECT public._ci_add_constraint_if_not_exists('history'::regclass, 'ck_history_amount_positive', 'CHECK (h_amount > 0)');

SELECT public._ci_add_constraint_if_not_exists('app_user'::regclass, 'pk_app_user', 'PRIMARY KEY (user_id)');
SELECT public._ci_add_constraint_if_not_exists('import_task'::regclass, 'pk_import_task', 'PRIMARY KEY (task_id)');
SELECT public._ci_add_constraint_if_not_exists('import_error_log'::regclass, 'pk_import_error_log', 'PRIMARY KEY (error_id)');
SELECT public._ci_add_constraint_if_not_exists('query_log'::regclass, 'pk_query_log', 'PRIMARY KEY (query_log_id)');
SELECT public._ci_add_constraint_if_not_exists('transaction_log'::regclass, 'pk_transaction_log', 'PRIMARY KEY (transaction_log_id)');
SELECT public._ci_add_constraint_if_not_exists('performance_result'::regclass, 'pk_performance_result', 'PRIMARY KEY (result_id)');
SELECT public._ci_add_constraint_if_not_exists('stock_change_log'::regclass, 'pk_stock_change_log', 'PRIMARY KEY (log_id)');

SELECT public._ci_add_constraint_if_not_exists('app_user'::regclass, 'uk_app_user_username', 'UNIQUE (username)');
SELECT public._ci_add_constraint_if_not_exists('transaction_log'::regclass, 'uk_transaction_log_transaction_id', 'UNIQUE (transaction_id)');

SELECT public._ci_add_constraint_if_not_exists('import_task'::regclass, 'fk_import_task_app_user', 'FOREIGN KEY (created_by) REFERENCES app_user(user_id)');
SELECT public._ci_add_constraint_if_not_exists('import_error_log'::regclass, 'fk_import_error_log_import_task', 'FOREIGN KEY (task_id) REFERENCES import_task(task_id)');
SELECT public._ci_add_constraint_if_not_exists('query_log'::regclass, 'fk_query_log_app_user', 'FOREIGN KEY (executed_by) REFERENCES app_user(user_id)');
SELECT public._ci_add_constraint_if_not_exists('transaction_log'::regclass, 'fk_transaction_log_app_user', 'FOREIGN KEY (executed_by) REFERENCES app_user(user_id)');
SELECT public._ci_add_constraint_if_not_exists('stock_change_log'::regclass, 'fk_stock_change_log_stock', 'FOREIGN KEY (warehouse_id, item_id) REFERENCES stock(s_w_id, s_i_id)');

SELECT public._ci_add_constraint_if_not_exists('app_user'::regclass, 'ck_app_user_role_valid', 'CHECK (role IN (''admin'', ''user''))');
SELECT public._ci_add_constraint_if_not_exists('app_user'::regclass, 'ck_app_user_status_valid', 'CHECK (status IN (''pending'', ''approved'', ''disabled''))');

SELECT public._ci_add_constraint_if_not_exists('import_task'::regclass, 'ck_import_task_status_valid', 'CHECK (status IN (''pending'', ''running'', ''success'', ''failed''))');
SELECT public._ci_add_constraint_if_not_exists('import_task'::regclass, 'ck_import_task_total_rows_non_negative', 'CHECK (total_rows >= 0)');
SELECT public._ci_add_constraint_if_not_exists('import_task'::regclass, 'ck_import_task_success_rows_non_negative', 'CHECK (success_rows >= 0)');
SELECT public._ci_add_constraint_if_not_exists('import_task'::regclass, 'ck_import_task_failed_rows_non_negative', 'CHECK (failed_rows >= 0)');
SELECT public._ci_add_constraint_if_not_exists('import_task'::regclass, 'ck_import_task_row_count_consistent', 'CHECK (success_rows + failed_rows <= total_rows)');

SELECT public._ci_add_constraint_if_not_exists('import_error_log'::regclass, 'ck_import_error_log_line_number_positive', 'CHECK (line_number > 0)');

SELECT public._ci_add_constraint_if_not_exists('query_log'::regclass, 'ck_query_log_elapsed_ms_non_negative', 'CHECK (elapsed_ms IS NULL OR elapsed_ms >= 0)');
SELECT public._ci_add_constraint_if_not_exists('query_log'::regclass, 'ck_query_log_row_count_non_negative', 'CHECK (row_count IS NULL OR row_count >= 0)');

SELECT public._ci_add_constraint_if_not_exists('transaction_log'::regclass, 'ck_transaction_log_type_valid', 'CHECK (transaction_type IN (''new_order'', ''payment''))');
SELECT public._ci_add_constraint_if_not_exists('transaction_log'::regclass, 'ck_transaction_log_status_valid', 'CHECK (status IN (''running'', ''committed'', ''rolled_back'', ''failed''))');
SELECT public._ci_add_constraint_if_not_exists('transaction_log'::regclass, 'ck_transaction_log_elapsed_ms_non_negative', 'CHECK (elapsed_ms IS NULL OR elapsed_ms >= 0)');

SELECT public._ci_add_constraint_if_not_exists('performance_result'::regclass, 'ck_performance_result_thread_count_positive', 'CHECK (thread_count > 0)');
SELECT public._ci_add_constraint_if_not_exists('performance_result'::regclass, 'ck_performance_result_requests_non_negative', 'CHECK (total_requests >= 0 AND success_count >= 0 AND fail_count >= 0)');
SELECT public._ci_add_constraint_if_not_exists('performance_result'::regclass, 'ck_performance_result_latency_non_negative', 'CHECK ((avg_latency_ms IS NULL OR avg_latency_ms >= 0) AND (max_latency_ms IS NULL OR max_latency_ms >= 0) AND (min_latency_ms IS NULL OR min_latency_ms >= 0))');
SELECT public._ci_add_constraint_if_not_exists('performance_result'::regclass, 'ck_performance_result_throughput_non_negative', 'CHECK (throughput IS NULL OR throughput >= 0)');

SELECT public._ci_add_constraint_if_not_exists('stock_change_log'::regclass, 'ck_stock_change_log_type_valid', 'CHECK (change_type IN (''new_order'', ''manual_adjustment'', ''rollback''))');
SELECT public._ci_add_constraint_if_not_exists('stock_change_log'::regclass, 'ck_stock_change_log_quantity_delta', 'CHECK (change_quantity = new_quantity - old_quantity)');

DROP FUNCTION public._ci_add_constraint_if_not_exists(regclass, text, text);
