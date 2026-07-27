# Day 1 ADR prompt inputs

Use each block with the prompt template in the parent README.

## ADR-0001

- Decision: Range-partition `trades` by `trade_date` using monthly partitions.
- Alternatives: one unpartitioned table; hash partitioning by instrument;
  range partitioning by generated trade ID.
- Forces: roughly 91 million rows at steady state; date-bounded queries;
  five-year retention and archival; PostgreSQL partition-key restrictions.

## ADR-0002

- Decision: Store optional, asset-specific instrument attributes in a
  non-null `JSONB` metadata column while retaining core columns relationally.
- Alternatives: add a nullable column for every attribute; use an
  entity-attribute-value table; store an unindexed JSON string.
- Forces: attributes differ by asset class; core identifiers need constraints;
  new optional fields should not require frequent migrations.

## ADR-0003

- Decision: Index `instruments.metadata` with GIN and `jsonb_path_ops`.
- Alternatives: B-tree on the complete JSON document; default `jsonb_ops` GIN;
  expression indexes for individual JSON paths; no metadata index.
- Forces: containment (`@>`) is the primary access pattern; index size and
  write overhead matter; key-existence operators are secondary.
