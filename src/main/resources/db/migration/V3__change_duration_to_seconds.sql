-- Ajouter la nouvelle colonne en secondes
ALTER TABLE ligne ADD COLUMN duration_seconds BIGINT;

-- Convertir les données existantes (minutes → secondes)
UPDATE ligne SET duration_seconds = duration_minutes * 60;

-- Rendre la colonne non-nullable
ALTER TABLE ligne ALTER COLUMN duration_seconds SET NOT NULL;

-- Supprimer l'ancienne colonne
ALTER TABLE ligne DROP COLUMN duration_minutes;

-- Ajouter un commentaire
COMMENT ON COLUMN ligne.duration_seconds IS 'Durée du trajet en secondes (ex: 3600 = 1 heure, 86400 = 1 jour)';