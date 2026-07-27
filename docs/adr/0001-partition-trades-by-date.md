# ADR-0001: Partition trades by trade date

## Status

Accepted — 2026-07-27

## Context

ReconX expects about 50,000 trades each day, or approximately 91 million rows
after five years. Reconciliation runs, analyst searches, and retention jobs are
normally bounded by `trade_date`. We considered leaving `trades` unpartitioned,
hash partitioning by instrument, and range partitioning on a generated ID.
Neither hash nor ID ranges align with the dates used by queries and retention.

## Decision

Use PostgreSQL range partitioning on `trades.trade_date`, with calendar-month
partitions. Queries must supply a date range so PostgreSQL can prune unrelated
partitions. Partition creation and retention will be treated as scheduled
database operations rather than application-side row management.

## Consequences

Date-scoped reads touch fewer rows and an expired month can be detached without
deleting millions of records individually. Local indexes also remain smaller.

The partition key must participate in PostgreSQL primary and unique constraints,
which complicates global uniqueness for `trade_ref`. Operations must create
future partitions before they are needed, monitor the default partition, and
verify that application queries include a usable date predicate.
