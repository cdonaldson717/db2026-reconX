# ADR-0003: Index metadata with GIN jsonb_path_ops

## Status

Accepted — 2026-07-27

## Context

ReconX needs to find instruments whose metadata contains a supplied document,
for example all instruments classified in a particular sector. The dominant
operator is JSONB containment (`@>`). A B-tree over the entire document does
not support this access pattern. We also evaluated the general-purpose
`jsonb_ops` GIN class, one expression index per JSON path, and no index.

## Decision

Create a GIN index on `instruments.metadata` using `jsonb_path_ops`. Write the
primary metadata filters as containment predicates. Add a targeted expression
index later only if measurements show that a frequently used scalar-path query
needs one.

## Consequences

Containment searches can use a compact inverted index rather than scanning all
instrument documents. Compared with the default GIN operator class,
`jsonb_path_ops` is focused on this workload and generally requires less index
space.

The index adds storage and write-maintenance cost. It does not accelerate every
JSONB operator, including all key-existence patterns. Those secondary queries
may scan the small instruments table unless a later workload justifies a
different or additional index. Query plans must be checked with realistic row
counts because PostgreSQL can reasonably choose a sequential scan for tiny
seed data.
