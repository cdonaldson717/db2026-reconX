# ADR-0002: Use JSONB for optional instrument metadata

## Status

Accepted — 2026-07-27

## Context

All instruments share stable fields such as symbol, asset class, currency, and
ISIN. Other attributes—issuer ratings, exchange details, contract terms, and
tags—vary substantially by asset class. Adding a column for every possibility
would produce a sparse table and recurring migrations. An
entity-attribute-value model was also considered, but it makes typed documents
and multi-attribute queries difficult to understand.

## Decision

Keep stable, constrained fields as relational columns and add
`instruments.metadata JSONB NOT NULL DEFAULT '{}'::JSONB` for optional
attributes. Metadata documents will use nested objects for related values and
arrays for labels. Validation of document shape belongs at the application
boundary; identifiers and fields used for joins remain relational.

## Consequences

ReconX can add asset-specific attributes without changing the table for each
new field. PostgreSQL can query nested values and index containment predicates.

JSONB does not provide the same per-property constraints as ordinary columns,
so inconsistent keys or value types are possible unless the application checks
them. Frequently joined, uniquely constrained, or universally required values
must be promoted to relational columns rather than hidden in metadata.
