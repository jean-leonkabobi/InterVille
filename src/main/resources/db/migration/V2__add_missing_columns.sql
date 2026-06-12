-- V2: Ajouter les colonnes manquantes aux tables existantes

-- Ajouter company_id à siege (LA CAUSE DE L'ERREUR)
ALTER TABLE siege ADD COLUMN IF NOT EXISTS company_id BIGINT;

-- Mettre à jour company_id depuis bus
UPDATE siege SET company_id = bus.company_id FROM bus WHERE siege.bus_id = bus.id;

-- Rendre NOT NULL après mise à jour
ALTER TABLE siege ALTER COLUMN company_id SET NOT NULL;

-- Ajouter deleted_at à company (déjà fait, mais sécurité)
ALTER TABLE company ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE company ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE company ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- Ajouter updated_at à incident
ALTER TABLE incident ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW();

-- S'assurer que toutes les tables ont les colonnes requises
DO $$
DECLARE
tables text[] := ARRAY['company', 'agence', 'users', 'bus', 'ligne', 'trajet', 'reservation', 'transaction', 'incident', 'siege'];
    t text;
BEGIN
    FOREACH t IN ARRAY tables
    LOOP
        EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP', t);
EXECUTE format('ALTER TABLE IF EXISTS %I ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT NOW()', t);
END LOOP;
END $$;

-- Index
CREATE INDEX IF NOT EXISTS idx_company_company_id ON company(company_id);
CREATE INDEX IF NOT EXISTS idx_siege_company_id ON siege(company_id);