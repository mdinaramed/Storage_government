INSERT INTO units (name, state) VALUES
                                    ('kg', 'ACTIVE'),
                                    ('pcs', 'ACTIVE'),
                                    ('liters', 'ACTIVE')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO resources (name, state) VALUES
                                        ('Sugar', 'ACTIVE'),
                                        ('Paper', 'ACTIVE'),
                                        ('Water', 'ACTIVE')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO clients (name, address, state) VALUES
                                               ('Client A', 'Astana', 'ACTIVE'),
                                               ('Client B', 'Almaty', 'ACTIVE')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO balances (resource_id, unit_id, amount)
SELECT r.id, u.id,
       CASE
           WHEN r.name = 'Sugar' AND u.name = 'kg' THEN 100
           WHEN r.name = 'Paper' AND u.name = 'pcs' THEN 500
           WHEN r.name = 'Water' AND u.name = 'liters' THEN 300
           ELSE 0
           END
FROM resources r
         JOIN units u ON (
    (r.name = 'Sugar' AND u.name = 'kg') OR
    (r.name = 'Paper' AND u.name = 'pcs') OR
    (r.name = 'Water' AND u.name = 'liters')
    )
    ON CONFLICT (resource_id, unit_id) DO NOTHING;