-- TICKET-ADV009 — illustrative metadata documents for seeded instruments.
-- These updates are idempotent and can be rerun after the CSV seed is loaded.

UPDATE instruments
SET metadata = jsonb_build_object(
    'classification', jsonb_build_object('sector', 'Technology', 'industry', 'Software'),
    'listing', jsonb_build_object('venue', 'XETR', 'country', 'DE'),
    'issuer', jsonb_build_object('legalName', 'SAP SE', 'lei', '529900D6BF99LW9R2E68'),
    'creditRatings', jsonb_build_object('sAndP', 'AA-', 'moodys', 'Aa3'),
    'labels', jsonb_build_array('DAX40', 'large-cap', 'esg-screened')
)
WHERE symbol = 'SAP.DE';

UPDATE instruments
SET metadata = jsonb_build_object(
    'classification', jsonb_build_object('sector', 'Technology', 'industry', 'Software'),
    'listing', jsonb_build_object('venue', 'NASDAQ', 'country', 'US'),
    'issuer', jsonb_build_object('legalName', 'Microsoft Corporation'),
    'labels', jsonb_build_array('cloud', 'large-cap')
)
WHERE symbol = 'MSFT';

-- Containment: eligible for the jsonb_path_ops GIN index.
SELECT symbol, metadata #>> '{classification,sector}' AS sector
FROM instruments
WHERE metadata @> '{"classification":{"sector":"Technology"}}'::JSONB;

-- Nested scalar extraction.
SELECT symbol, metadata #>> '{listing,country}' AS listing_country
FROM instruments;

-- Array membership and top-level-key existence examples.
SELECT symbol FROM instruments WHERE metadata->'labels' ? 'DAX40';
SELECT symbol FROM instruments WHERE metadata ? 'creditRatings';
