-- Tables principales

CREATE TABLE company (
                         id BIGSERIAL PRIMARY KEY,
                         company_id BIGINT NOT NULL,
                         name VARCHAR(100) NOT NULL,
                         created_at TIMESTAMP DEFAULT NOW(),
                         updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE agence (
                        id BIGSERIAL PRIMARY KEY,
                        company_id BIGINT NOT NULL REFERENCES company(id),
                        name VARCHAR(100) NOT NULL,
                        address TEXT,
                        phone VARCHAR(20),
                        created_at TIMESTAMP DEFAULT NOW(),
                        updated_at TIMESTAMP DEFAULT NOW(),
                        deleted_at TIMESTAMP
);

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       company_id BIGINT NOT NULL REFERENCES company(id),
                       agence_id BIGINT REFERENCES agence(id),  -- Pour AGENTS uniquement
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       phone VARCHAR(20),
                       role VARCHAR(20) NOT NULL,  -- CLIENT, AGENT, CHAUFFEUR, ADMIN
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT NOW(),
                       updated_at TIMESTAMP DEFAULT NOW(),
                       deleted_at TIMESTAMP  -- Soft delete
);

CREATE TABLE bus (
                     id BIGSERIAL PRIMARY KEY,
                     company_id BIGINT NOT NULL REFERENCES company(id),
                     registration VARCHAR(50) UNIQUE NOT NULL,
                     capacity INT NOT NULL,
                     seat_config JSONB,  -- Plan de sièges personnalisable
                     status VARCHAR(20) DEFAULT 'OPERATIONAL',
                     created_at TIMESTAMP DEFAULT NOW(),
                     updated_at TIMESTAMP DEFAULT NOW(),
                     deleted_at TIMESTAMP
);

CREATE TABLE siege (
                       id BIGSERIAL PRIMARY KEY,
                       bus_id BIGINT NOT NULL REFERENCES bus(id),
                       seat_number VARCHAR(10) NOT NULL,
                       seat_type VARCHAR(20) DEFAULT 'STANDARD',  -- STANDARD, VIP, PREFERENTIEL
                       position_x INT,
                       position_y INT,
                       UNIQUE(bus_id, seat_number)
);

CREATE TABLE ligne (
                       id BIGSERIAL PRIMARY KEY,
                       company_id BIGINT NOT NULL REFERENCES company(id),
                       departure_city VARCHAR(100) NOT NULL,
                       arrival_city VARCHAR(100) NOT NULL,
                       duration_minutes INT NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT NOW(),
                       updated_at TIMESTAMP DEFAULT NOW(),
                       deleted_at TIMESTAMP
);

CREATE TABLE trajet (
                        id BIGSERIAL PRIMARY KEY,
                        company_id BIGINT NOT NULL REFERENCES company(id),
                        ligne_id BIGINT NOT NULL REFERENCES ligne(id),
                        bus_id BIGINT NOT NULL REFERENCES bus(id),
                        chauffeur_id BIGINT REFERENCES users(id),  -- CHAUFFEUR assigné
                        departure_time TIMESTAMP NOT NULL,
                        arrival_time TIMESTAMP NOT NULL,
                        base_price DECIMAL(10,2) NOT NULL,
                        status VARCHAR(20) DEFAULT 'SCHEDULED',  -- SCHEDULED, DEPARTED, ARRIVED, CANCELLED
                        created_at TIMESTAMP DEFAULT NOW(),
                        updated_at TIMESTAMP DEFAULT NOW(),
                        deleted_at TIMESTAMP
);

-- Verrouillage temporaire des sièges (FT1, timeout 15 min)
CREATE TABLE verrou_siege (
                              id BIGSERIAL PRIMARY KEY,
                              trajet_id BIGINT NOT NULL REFERENCES trajet(id),
                              siege_id BIGINT NOT NULL REFERENCES siege(id),
                              reservation_session_id UUID NOT NULL,
                              locked_at TIMESTAMP DEFAULT NOW(),
                              expires_at TIMESTAMP NOT NULL,  -- NOW() + 15 minutes
                              UNIQUE(trajet_id, siege_id)
);

CREATE TABLE reservation (
                             id BIGSERIAL PRIMARY KEY,
                             company_id BIGINT NOT NULL REFERENCES company(id),
                             user_id BIGINT REFERENCES users(id),  -- NULL si réservation sans compte (agent)
                             trajet_id BIGINT NOT NULL REFERENCES trajet(id),
                             agence_id BIGINT REFERENCES agence(id),  -- Agence vendeuse
                             reservation_code UUID UNIQUE DEFAULT gen_random_uuid(),
                             passenger_name VARCHAR(100),
                             passenger_phone VARCHAR(20),
                             status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, PAID, CANCELLED, EXPIRED
                             total_price DECIMAL(10,2) NOT NULL,
                             expires_at TIMESTAMP,  -- Pour PENDING: 15 min
                             created_at TIMESTAMP DEFAULT NOW(),
                             updated_at TIMESTAMP DEFAULT NOW(),
                             deleted_at TIMESTAMP
);

CREATE TABLE reservation_siege (
                                   reservation_id BIGINT NOT NULL REFERENCES reservation(id),
                                   siege_id BIGINT NOT NULL REFERENCES siege(id),
                                   trajet_id BIGINT NOT NULL REFERENCES trajet(id),
                                   PRIMARY KEY (reservation_id, siege_id)
);

CREATE TABLE ticket (
                        id BIGSERIAL PRIMARY KEY,
                        reservation_id BIGINT NOT NULL REFERENCES reservation(id),
                        qr_code VARCHAR(255) UNIQUE NOT NULL,
                        validated_at TIMESTAMP,
                        validated_by BIGINT REFERENCES users(id),  -- Chauffeur ou agent
                        status VARCHAR(20) DEFAULT 'ISSUED',  -- ISSUED, VALIDATED, CANCELLED
                        created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transaction (
                             id BIGSERIAL PRIMARY KEY,
                             company_id BIGINT NOT NULL REFERENCES company(id),
                             reservation_id BIGINT NOT NULL REFERENCES reservation(id),
                             agence_id BIGINT REFERENCES agence(id),
                             amount DECIMAL(10,2) NOT NULL,
                             payment_mode VARCHAR(20) NOT NULL,  -- MOBILE_MONEY, ESPECES, CARTE
                             mobile_money_ref VARCHAR(100),
                             status VARCHAR(20) DEFAULT 'PENDING',
                             created_at TIMESTAMP DEFAULT NOW(),
                             updated_at TIMESTAMP DEFAULT NOW(),
                             deleted_at TIMESTAMP
);

CREATE TABLE bagage (
                        id BIGSERIAL PRIMARY KEY,
                        reservation_id BIGINT NOT NULL REFERENCES reservation(id),
                        passenger_index INT DEFAULT 1,  -- Pour plusieurs passagers par réservation
                        quantity INT NOT NULL,
                        label_printed BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE incident (
                          id BIGSERIAL PRIMARY KEY,
                          company_id BIGINT NOT NULL REFERENCES company(id),
                          trajet_id BIGINT NOT NULL REFERENCES trajet(id),
                          reported_by BIGINT NOT NULL REFERENCES users(id),
                          type VARCHAR(50) NOT NULL,
                          description TEXT,
                          status VARCHAR(20) DEFAULT 'OPEN',
                          created_at TIMESTAMP DEFAULT NOW(),
                          resolved_at TIMESTAMP,
                          deleted_at TIMESTAMP
);

-- Pour synchronisation hors ligne (FT2)
CREATE TABLE sync_log (
                          id BIGSERIAL PRIMARY KEY,
                          agence_id BIGINT NOT NULL REFERENCES agence(id),
                          operation_type VARCHAR(20) NOT NULL,  -- SALE, CANCELLATION, REFUND
                          payload JSONB NOT NULL,
                          local_timestamp TIMESTAMP NOT NULL,
                          synced_at TIMESTAMP,
                          conflict_resolved BOOLEAN DEFAULT FALSE
);

-- Index critiques
CREATE INDEX idx_trajet_departure ON trajet(departure_time);
CREATE INDEX idx_reservation_trajet ON reservation(trajet_id);
CREATE INDEX idx_reservation_user ON reservation(user_id);
CREATE INDEX idx_ticket_qr ON ticket(qr_code);
CREATE INDEX idx_verrou_expires ON verrou_siege(expires_at);
CREATE INDEX idx_sync_unsynced ON sync_log(agence_id) WHERE synced_at IS NULL;