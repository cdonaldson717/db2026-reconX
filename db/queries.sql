-- ============================================================================
-- TICKET-ADV010 — VWAP per instrument per day (window function)
-- ============================================================================
SELECT DISTINCT
    t.instrument_id,
    t.trade_date,
    SUM(t.price * t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date)
        / NULLIF(SUM(t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date), 0)
            AS vwap
FROM trades t
WHERE t.deleted_at IS NULL
  AND t.asset_class = 'EQUITY'
ORDER BY t.trade_date DESC, t.instrument_id;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle (execution -> confirmation -> settlement
--                -> recon_break -> resolution)
-- ============================================================================
WITH RECURSIVE trade_lifecycle AS (
    -- Base case: every active trade begins at execution.
    SELECT
        t.id                            AS trade_id,
        t.trade_ref,
        1                               AS stage,
        'EXECUTION'::text               AS stage_name,
        t.created_at                    AS event_at,
        'EXECUTED'::text                AS event_status,
        NULL::bigint                    AS settlement_id,
        NULL::bigint                    AS recon_break_id
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    -- Recursive case
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.stage + 1                    AS stage,
        next_event.stage_name,
        next_event.event_at,
        next_event.event_status,
        next_event.settlement_id,
        next_event.recon_break_id
    FROM trade_lifecycle tl
    JOIN LATERAL (
        -- Stage 1 -> Stage 2: confirmation
        SELECT
            'CONFIRMATION'::text        AS stage_name,
            s.settlement_date::timestamp AS event_at,
            'CONFIRMED'::text           AS event_status,
            s.id::bigint                AS settlement_id,
            NULL::bigint                AS recon_break_id
        FROM settlements s
        WHERE tl.stage = 1
          AND s.trade_id = tl.trade_id

        UNION ALL

        -- Stage 2 -> Stage 3: settlement
        SELECT
            'SETTLEMENT'::text          AS stage_name,
            s.settlement_date::timestamp AS event_at,
            s.status::text              AS event_status,
            s.id::bigint                AS settlement_id,
            NULL::bigint                AS recon_break_id
        FROM settlements s
        WHERE tl.stage = 2
          AND s.id = tl.settlement_id
          AND s.settlement_date IS NOT NULL

        UNION ALL

        -- Stage 3 -> Stage 4: reconciliation break
        SELECT
            'RECON_BREAK'::text         AS stage_name,
            rb.detected_at              AS event_at,
            rb.status::text             AS event_status,
            tl.settlement_id            AS settlement_id,
            rb.id::bigint               AS recon_break_id
        FROM recon_breaks rb
        WHERE tl.stage = 3
          AND rb.trade_id = tl.trade_id

        UNION ALL

        -- Stage 4 -> Stage 5: resolution
        SELECT
            'RESOLUTION'::text          AS stage_name,
            rb.resolved_at              AS event_at,
            'RESOLVED'::text            AS event_status,
            tl.settlement_id            AS settlement_id,
            rb.id::bigint               AS recon_break_id
        FROM recon_breaks rb
        WHERE tl.stage = 4
          AND rb.id = tl.recon_break_id
          AND rb.resolved_at IS NOT NULL
    ) AS next_event ON TRUE
    WHERE tl.stage < 5
)
SELECT
    trade_id,
    stage,
    stage_name,
    event_at,
    event_status
FROM trade_lifecycle
ORDER BY
    trade_id,
    stage,
    event_at;


-- ============================================================================
-- ADV008 — REFRESH the daily-summary materialised view (concurrent so it can
--         run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB lookup: which instruments have sector = 'Banking'?
-- ============================================================================
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"sector":"Banking"}'::jsonb;
