UPDATE accounts
SET account_type = 'Saving'
WHERE lower(account_type) IN ('cash', 'saving', 'savings');

UPDATE accounts
SET account_type = 'Current'
WHERE lower(account_type) IN ('bank', 'current');
