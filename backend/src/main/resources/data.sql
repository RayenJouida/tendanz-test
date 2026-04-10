-- ============================================================
-- Initial Data for Insurance Pricing Engine
-- ============================================================

-- Insert Zones (code, name, risk_coefficient)
-- Formula: Prix Final = Taux de Base × Facteur Âge × Coefficient Zone
INSERT INTO zone (code, name, risk_coefficient) VALUES
('TUN', 'Grand Tunis', 1.20),
('SFX', 'Sfax', 1.00),
('SOU', 'Sousse', 1.10);

-- Insert Products (name, description, created_at)
INSERT INTO product (name, description, created_at) VALUES
('Assurance Auto', 'Couverture automobile complète', CURRENT_TIMESTAMP),
('Assurance Habitation', 'Protection habitation et biens', CURRENT_TIMESTAMP),
('Assurance Santé', 'Couverture santé et frais médicaux', CURRENT_TIMESTAMP);

-- Insert Pricing Rules (product_id, base_rate, age factors, created_at)
-- Age factors are identical for all products:
-- YOUNG (18-24): 1.30 | ADULT (25-45): 1.00 | SENIOR (46-65): 1.20 | ELDERLY (66-99): 1.50

-- Assurance Auto: base_rate = 500.00 TND
INSERT INTO pricing_rule (product_id, base_rate, age_factor_young, age_factor_adult, age_factor_senior, age_factor_elderly, created_at)
VALUES (1, 500.00, 1.30, 1.00, 1.20, 1.50, CURRENT_TIMESTAMP);

-- Assurance Habitation: base_rate = 300.00 TND
INSERT INTO pricing_rule (product_id, base_rate, age_factor_young, age_factor_adult, age_factor_senior, age_factor_elderly, created_at)
VALUES (2, 300.00, 1.30, 1.00, 1.20, 1.50, CURRENT_TIMESTAMP);

-- Assurance Santé: base_rate = 800.00 TND
INSERT INTO pricing_rule (product_id, base_rate, age_factor_young, age_factor_adult, age_factor_senior, age_factor_elderly, created_at)
VALUES (3, 800.00, 1.30, 1.00, 1.20, 1.50, CURRENT_TIMESTAMP);


-- Assurance Voyage: base_rate = 400.00 TND (4th product - bonus)
INSERT INTO product (name, description, created_at) VALUES
('Assurance Voyage', 'Travel insurance coverage for international trips', CURRENT_TIMESTAMP);

INSERT INTO pricing_rule (product_id, base_rate, age_factor_young, age_factor_adult, age_factor_senior, age_factor_elderly, created_at)
VALUES (4, 400.00, 1.20, 1.00, 1.10, 1.40, CURRENT_TIMESTAMP);