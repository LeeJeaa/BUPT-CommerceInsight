# TPC CommerceInsight Report Outline

## 1. Project Environment And Deployment

- PostgreSQL Docker version and container configuration
- Database name, user, port, and volume strategy
- SQL asset rule: `sql/` is the only business SQL source

## 2. SQL Initialization Flow

- Development initialization order
- Formal import order
- Difference between web demo import and dbgen + COPY import

## 3. TPC-H Data Generation

- dbgen location
- Scale factors tested: SF=0.1, SF=0.6, SF=1
- Generation command and output directory
- Disk and time observations

## 4. Formal COPY Import

- COPY script description
- Import logs
- Row count verification
- Problems and fixes

## 5. EXPLAIN ANALYZE Design

- Query list: Q1, Q5, Q12, Q14
- Recorded fields: runtime, scan type, join type, sort/aggregate nodes
- Baseline result location: `report/explain_plans/`

## 6. Index Comparison

- Without-index runtime
- With-index runtime
- Plan changes
- Conclusion for each query

## 7. Concurrent Performance Test

- Python test script parameters
- Tested endpoints
- Thread counts
- Average latency, p95 latency, throughput, failure count

## 8. Integration And Delivery

- Inputs from A/B/C
- Outputs provided by D
- Final package checklist
