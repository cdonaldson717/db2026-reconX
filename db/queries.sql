-- ============================================================================
-- TICKET-ADV010 — Per-trade detail with daily instrument VWAP
-- ============================================================================
SELECT
    tr.trade_ref,
    tr.trade_date,
    ins.symbol,
    tr.quantity,
    tr.price,
    tr.price * tr.quantity AS trade_notional,
    SUM(tr.price * tr.quantity) OVER daily_instrument
        / NULLIF(SUM(tr.quantity) OVER daily_instrument, 0) AS daily_vwap,
    ROW_NUMBER() OVER (
        PARTITION BY tr.instrument_id, tr.trade_date
        ORDER BY tr.created_at, tr.id
    ) AS trade_number,
    SUM(tr.quantity) OVER (
        PARTITION BY tr.instrument_id, tr.trade_date
        ORDER BY tr.created_at, tr.id
        ROWS UNBOUNDED PRECEDING
    ) AS running_quantity
FROM trades AS tr
INNER JOIN instruments AS ins ON ins.id = tr.instrument_id
WHERE tr.deleted_at IS NULL
  AND tr.trade_date >= DATE '2026-06-01'
  AND tr.trade_date < DATE '2026-07-01'
WINDOW daily_instrument AS (
    PARTITION BY tr.instrument_id, tr.trade_date
)
ORDER BY ins.symbol, tr.trade_date, tr.created_at, tr.id;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle (execution -> settlement
--                -> recon_break -> resolution)
-- ============================================================================
WITH RECURSIVE trade_lifecycle AS (
    -- anchor: every trade in its execution state
    SELECT
        t.id           AS trade_id,
        t.trade_ref,
        1              AS step,
        'EXECUTED'     AS state,
        t.created_at   AS at_ts,
        NULL::text     AS detail
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    -- recursive: each subsequent state derived from the previous step
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.step + 1,
        CASE tl.step
            WHEN 1 THEN 'CONFIRMED'
            WHEN 2 THEN 'SETTLED'
            WHEN 3 THEN 'RECONCILED'
        END                                          AS state,
        s.settlement_date::timestamp                  AS at_ts,
        s.status                                      AS detail
    FROM trade_lifecycle tl
    JOIN settlements s ON s.trade_id = tl.trade_id
    WHERE tl.step < 4
)
SELECT * FROM trade_lifecycle
ORDER BY trade_id, step;


-- ============================================================================
-- ADV008 — REFRESH the daily-summary materialised view (concurrent so it can
--         run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB containment lookup (supported by jsonb_path_ops)
-- ============================================================================
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"classification":{"sector":"Technology"}}'::jsonb;
