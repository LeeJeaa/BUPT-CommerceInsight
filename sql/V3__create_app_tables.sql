-- TPC CommerceInsight
-- V3: create application auxiliary tables.

CREATE TABLE IF NOT EXISTS app_user (
    user_id bigserial NOT NULL,
    username varchar(50) NOT NULL,
    password_hash varchar(255) NOT NULL,
    real_name varchar(100),
    email varchar(120),
    role varchar(20) NOT NULL DEFAULT 'user',
    status varchar(20) NOT NULL DEFAULT 'pending',
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS import_task (
    task_id bigserial NOT NULL,
    table_name varchar(50) NOT NULL,
    file_name varchar(255) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'pending',
    total_rows bigint NOT NULL DEFAULT 0,
    success_rows bigint NOT NULL DEFAULT 0,
    failed_rows bigint NOT NULL DEFAULT 0,
    started_at timestamp,
    ended_at timestamp,
    created_by bigint
);

CREATE TABLE IF NOT EXISTS import_error_log (
    error_id bigserial NOT NULL,
    task_id bigint NOT NULL,
    line_number bigint NOT NULL,
    field_name varchar(100) NOT NULL,
    field_value text,
    error_reason text NOT NULL,
    created_at timestamp NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS query_log (
    query_log_id bigserial NOT NULL,
    query_name varchar(100) NOT NULL,
    query_params text,
    elapsed_ms bigint,
    row_count bigint,
    executed_by bigint,
    executed_at timestamp NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS transaction_log (
    transaction_log_id bigserial NOT NULL,
    transaction_id varchar(64) NOT NULL,
    transaction_type varchar(50) NOT NULL,
    status varchar(20) NOT NULL,
    elapsed_ms bigint,
    error_message text,
    executed_by bigint,
    executed_at timestamp NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS performance_result (
    result_id bigserial NOT NULL,
    test_name varchar(100) NOT NULL,
    test_type varchar(50) NOT NULL,
    thread_count integer NOT NULL,
    total_requests integer NOT NULL,
    success_count integer NOT NULL,
    fail_count integer NOT NULL,
    avg_latency_ms numeric(12, 2),
    max_latency_ms numeric(12, 2),
    min_latency_ms numeric(12, 2),
    throughput numeric(12, 2),
    created_at timestamp NOT NULL DEFAULT current_timestamp
);

CREATE TABLE IF NOT EXISTS stock_change_log (
    log_id bigserial NOT NULL,
    warehouse_id integer NOT NULL,
    item_id integer NOT NULL,
    old_quantity integer NOT NULL,
    new_quantity integer NOT NULL,
    change_quantity integer NOT NULL,
    change_type varchar(50) NOT NULL,
    related_transaction_id varchar(64),
    changed_at timestamp NOT NULL DEFAULT current_timestamp
);
