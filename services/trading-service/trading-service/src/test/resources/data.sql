INSERT INTO instruments (
    id,
    symbol,
    name,
    asset_type,
    tick_size,
    lot_size,
    status
)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'AAPL',
    'Apple Inc.',
    'EQUITY',
    0.01,
    1,
    'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;
