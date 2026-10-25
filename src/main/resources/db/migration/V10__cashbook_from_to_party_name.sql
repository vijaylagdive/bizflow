ALTER TABLE cashbook ALTER COLUMN from_to TYPE VARCHAR(200);

UPDATE cashbook
SET from_to = party_name
WHERE party_name IS NOT NULL
  AND btrim(party_name) <> '';

ALTER TABLE cashbook DROP COLUMN party_name;
