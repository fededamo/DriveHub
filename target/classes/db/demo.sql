-- =============================================================================
-- DriveHub - Script di Reset e Popolamento Dati Dimostrativi (demo.sql)
-- Schema basato su init.sql (diagramma E-R concettuale)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Svuotamento di tutte le tabelle nel rispetto dell'integrità referenziale
-- -----------------------------------------------------------------------------
DELETE FROM "sale";
DELETE FROM "rent";
DELETE FROM "test_drive";
DELETE FROM "request";
DELETE FROM "bank_account";
DELETE FROM "client";
DELETE FROM "salesman";
DELETE FROM "manager";
DELETE FROM "vehicle_discount";
DELETE FROM "vehicle";
DELETE FROM "model_discount";
DELETE FROM "model";
DELETE FROM "brand";

-- -----------------------------------------------------------------------------
-- 2. Inserimento Brand (5 marchi)
-- -----------------------------------------------------------------------------
INSERT INTO "brand" ("name") VALUES
    ('Alfa Romeo'),
    ('BMW'),
    ('Audi'),
    ('Fiat'),
    ('Mercedes-Benz');

-- -----------------------------------------------------------------------------
-- 3. Inserimento Modelli (20 modelli totali, 4 per ciascun brand)
-- -----------------------------------------------------------------------------
INSERT INTO "model" ("id", "brand", "name", "release_price", "release_year") VALUES
    (1,  'Alfa Romeo',    'Giulia',          42000.00, 2023),
    (2,  'Alfa Romeo',    'Stelvio',         51000.00, 2023),
    (3,  'Alfa Romeo',    'Tonale',          36000.00, 2024),
    (4,  'Alfa Romeo',    'Giulietta',       21000.00, 2020),
    (5,  'BMW',           'Serie 1',         31000.00, 2023),
    (6,  'BMW',           'Serie 3',         45000.00, 2024),
    (7,  'BMW',           'X1',              39000.00, 2023),
    (8,  'BMW',           'X3',              56000.00, 2024),
    (9,  'Audi',          'A3 Sportback',    33000.00, 2023),
    (10, 'Audi',          'A4 Avant',        46000.00, 2024),
    (11, 'Audi',          'Q3',              41000.00, 2023),
    (12, 'Audi',          'Q5',              58000.00, 2024),
    (13, 'Fiat',          '500 Hybrid',      17500.00, 2023),
    (14, 'Fiat',          'Panda',           15000.00, 2023),
    (15, 'Fiat',          '500X',            24000.00, 2022),
    (16, 'Fiat',          'Tipo',            21500.00, 2023),
    (17, 'Mercedes-Benz', 'Classe A',        35000.00, 2023),
    (18, 'Mercedes-Benz', 'Classe C',        52000.00, 2024),
    (19, 'Mercedes-Benz', 'GLA',             43000.00, 2023),
    (20, 'Mercedes-Benz', 'GLC',             61000.00, 2024);

-- -----------------------------------------------------------------------------
-- 4. Sconti per Modello (solo 2 modelli scontati)
-- -----------------------------------------------------------------------------
INSERT INTO "model_discount" ("model", "value") VALUES
    (1,  10), -- Alfa Romeo Giulia scontata del 10%
    (13, 15); -- Fiat 500 Hybrid scontata del 15%

-- -----------------------------------------------------------------------------
-- 5. Inserimento Veicoli (56 veicoli totali: SALE, RENT, WAITING, BLOCKED)
-- -----------------------------------------------------------------------------
INSERT INTO "vehicle" ("id", "model", "vin", "licence_plate", "kilometers", "price", "year", "state") VALUES
    -- Veicoli in vendita (SALE)
    (1,  1,  'ZAR93900000000001', 'GA001AA', 25000, 34500.00, 2023, 'SALE'),
    (2,  1,  'ZAR93900000000002', 'GA002AB', 12000, 38900.00, 2023, 'SALE'),
    (3,  2,  'ZAR94900000000003', 'GA003AC', 30000, 42000.00, 2023, 'SALE'),
    (4,  3,  'ZAR96500000000004', 'GA004AD',  8500, 33500.00, 2024, 'SALE'),
    (5,  4,  'ZAR94000000000005', 'GA005AE', 65000, 14800.00, 2020, 'SALE'),
    (6,  5,  'WBA11AA0000000006', 'GA006AF', 18000, 26900.00, 2023, 'SALE'),
    (7,  6,  'WBA31AA0000000007', 'GA007AG', 22000, 39500.00, 2024, 'SALE'),
    (8,  7,  'WBA71AA0000000008', 'GA008AH', 15000, 34900.00, 2023, 'SALE'),
    (9,  8,  'WBA81AA0000000009', 'GA009AJ', 28000, 48500.00, 2024, 'SALE'),
    (10, 9,  'WAUZZZ8V000000010', 'GA010AK', 14000, 28900.00, 2023, 'SALE'),
    (11, 10, 'WAUZZZ8W000000011', 'GA011AL', 21000, 41000.00, 2024, 'SALE'),
    (12, 11, 'WAUZZZF3000000012', 'GA012AM', 19000, 36500.00, 2023, 'SALE'),
    (13, 12, 'WAUZZZFY000000013', 'GA013AN', 32000, 49800.00, 2024, 'SALE'),
    (14, 13, 'ZFA31200000000014', 'GA014AP', 11000, 14200.00, 2023, 'SALE'),
    (15, 14, 'ZFA31900000000015', 'GA015AR',  9000, 12900.00, 2023, 'SALE'),
    (16, 15, 'ZFA33400000000016', 'GA016AS', 45000, 18500.00, 2022, 'SALE'),
    (17, 16, 'ZFA35600000000017', 'GA017AT', 24000, 17900.00, 2023, 'SALE'),
    (18, 17, 'WDD17700000000018', 'GA018AU', 16000, 31500.00, 2023, 'SALE'),
    (19, 18, 'WDD20500000000019', 'GA019AV', 20000, 46900.00, 2024, 'SALE'),
    (20, 19, 'WDD24700000000020', 'GA020AW', 13500, 38500.00, 2023, 'SALE'),
    (21, 20, 'WDD25300000000021', 'GA021AX', 29000, 54500.00, 2024, 'SALE'),
    (22, 1,  'ZAR93900000000022', 'GA022AY', 41000, 31000.00, 2023, 'SALE'),
    (23, 6,  'WBA31AA0000000023', 'GA023AZ', 35000, 37000.00, 2024, 'SALE'),
    (24, 10, 'WAUZZZ8W000000024', 'GA024BA', 26000, 39500.00, 2024, 'SALE'),
    (25, 13, 'ZFA31200000000025', 'GA025BB', 15000, 13800.00, 2023, 'SALE'),
    (26, 17, 'WDD17700000000026', 'GA026BC', 19000, 29800.00, 2023, 'SALE'),

    -- Veicoli a noleggio (RENT)
    (27, 13, 'ZFA31200000000027', 'GA027BD', 15000, 15500.00, 2023, 'RENT'),
    (28, 14, 'ZFA31900000000028', 'GA028BE', 22000, 13500.00, 2023, 'RENT'),
    (29, 16, 'ZFA35600000000029', 'GA029BF', 18500, 19000.00, 2023, 'RENT'),
    (30, 4,  'ZAR94000000000030', 'GA030BG', 31000, 17000.00, 2020, 'RENT'),
    (31, 5,  'WBA11AA0000000031', 'GA031BH',  9500, 28000.00, 2023, 'RENT'),
    (32, 9,  'WAUZZZ8V000000032', 'GA032BJ', 12000, 29500.00, 2023, 'RENT'),
    (33, 17, 'WDD17700000000033', 'GA033BK',  8000, 32000.00, 2023, 'RENT'),
    (34, 3,  'ZAR96500000000034', 'GA034BL', 19000, 34000.00, 2024, 'RENT'),
    (35, 7,  'WBA71AA0000000035', 'GA035BM', 14500, 36500.00, 2023, 'RENT'),
    (36, 11, 'WAUZZZF3000000036', 'GA036BN', 26000, 38000.00, 2023, 'RENT'),
    (37, 19, 'WDD24700000000037', 'GA037BP', 16000, 40000.00, 2023, 'RENT'),
    (38, 1,  'ZAR93900000000038', 'GA038BR', 11000, 39000.00, 2023, 'RENT'),
    (39, 6,  'WBA31AA0000000039', 'GA039BS', 13000, 42000.00, 2024, 'RENT'),
    (40, 10, 'WAUZZZ8W000000040', 'GA040BT', 17000, 43000.00, 2024, 'RENT'),
    (41, 18, 'WDD2050000000041', 'GA041BU', 10500, 48000.00, 2024, 'RENT'),
    (42, 20, 'WDD2530000000042', 'GA042BV', 14000, 57000.00, 2024, 'RENT'),

    -- Proposte di vendita inserite da clienti (WAITING - 4 veicoli)
    (43, 4,  'ZAR94000000000043', 'GA043BW', 72000, 13500.00, 2020, 'WAITING'),
    (44, 15, 'ZFA33400000000044', 'GA044BX', 58000, 16800.00, 2022, 'WAITING'),
    (45, 5,  'WBA11AA0000000045', 'GA045BY', 42000, 23000.00, 2023, 'WAITING'),
    (46, 9,  'WAUZZZ8V000000046', 'GA046BZ', 38000, 25500.00, 2023, 'WAITING'),

    -- Veicoli bloccati per richieste d'acquisto attive (BLOCKED - 10 veicoli)
    (47, 1,  'ZAR93900000000047', 'GA047CA', 21000, 36000.00, 2023, 'BLOCKED'),
    (48, 2,  'ZAR94900000000048', 'GA048CB', 19000, 44000.00, 2023, 'BLOCKED'),
    (49, 6,  'WBA31AA0000000049', 'GA049CC', 14000, 41500.00, 2024, 'BLOCKED'),
    (50, 7,  'WBA71AA0000000050', 'GA050CD', 16500, 35800.00, 2023, 'BLOCKED'),
    (51, 8,  'WBA81AA0000000051', 'GA051CE', 23000, 51000.00, 2024, 'BLOCKED'),
    (52, 10, 'WAUZZZ8W000000052', 'GA052CF', 17500, 42900.00, 2024, 'BLOCKED'),
    (53, 11, 'WAUZZZF3000000053', 'GA053CG', 22000, 37500.00, 2023, 'BLOCKED'),
    (54, 13, 'ZFA31200000000054', 'GA054CH',  8000, 15000.00, 2023, 'BLOCKED'),
    (55, 17, 'WDD17700000000055', 'GA055CJ', 12500, 32500.00, 2023, 'BLOCKED'),
    (56, 18, 'WDD20500000000056', 'GA056CK', 15000, 48000.00, 2024, 'BLOCKED');

-- -----------------------------------------------------------------------------
-- 6. Sconti per Singolo Veicolo (5 veicoli scontati singolarmente)
-- -----------------------------------------------------------------------------
INSERT INTO "vehicle_discount" ("vehicle", "value") VALUES
    (3,   8),
    (7,  12),
    (12,  5),
    (18, 15),
    (25, 10);

-- -----------------------------------------------------------------------------
-- 7. Utenti (15 Clienti, 4 Salesman, 1 Manager)
-- Password per tutti gli utenti: 'password' (hash PBKDF2 valido)
-- -----------------------------------------------------------------------------

-- 7a. Clienti (15)
INSERT INTO "client" ("id", "name", "surname", "email", "hash_password") VALUES
    (1,  'Mario',     'Rossi',     'mario.rossi@email.it',     'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (2,  'Luigi',     'Verdi',     'luigi.verdi@email.it',     'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (3,  'Giovanni',  'Bianchi',   'giovanni.bianchi@email.it', 'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (4,  'Alessandro','Neri',      'alessandro.neri@email.it',  'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (5,  'Marco',     'Ferrari',   'marco.ferrari@email.it',   'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (6,  'Andrea',    'Russo',     'andrea.russo@email.it',    'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (7,  'Stefano',   'Romano',    'stefano.romano@email.it',   'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (8,  'Davide',    'Colombo',   'davide.colombo@email.it',  'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (9,  'Francesco', 'Ricci',     'francesco.ricci@email.it', 'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (10, 'Matteo',    'Marino',    'matteo.marino@email.it',   'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (11, 'Lorenzo',   'Greco',     'lorenzo.greco@email.it',   'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (12, 'Simone',    'Bruno',     'simone.bruno@email.it',    'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (13, 'Federico',  'Gallo',     'federico.gallo@email.it',  'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (14, 'Roberto',   'Conti',     'roberto.conti@email.it',   'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (15, 'Gianluca',  'De Luca',   'gianluca.deluca@email.it', 'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34=');

-- 7b. Conti Correnti dei Clienti
INSERT INTO "bank_account" ("iban", "client", "balance") VALUES
    ('IT19W0503401600000793728296', 1,  18500.00),
    ('IT82Q0503401600000880165370', 2,  12400.00),
    ('IT77F0503401600000452978608', 3,  29000.00),
    ('IT36E0503401600000611963341', 4,   8500.00),
    ('IT19K0503401600000460886482', 5,  34000.00),
    ('IT54A0503401600000655432503', 6,  16000.00),
    ('IT30W0503401600000778182781', 7,  22200.00),
    ('IT26P0503401600000564346916', 8,   9800.00),
    ('IT93H0503401600000708786864', 9,  45000.00),
    ('IT65Q0503401600000732414015', 10, 14600.00),
    ('IT59L0503401600000838840805', 11, 21500.00),
    ('IT92Z0503401600000629723866', 12,  7700.00),
    ('IT45Y0503401600000915556674', 13, 31800.00),
    ('IT78W0503401600000256385258', 14, 11200.00),
    ('IT91G0503401600000002922581', 15, 38500.00);

-- 7c. Salesman (4 venditori)
INSERT INTO "salesman" ("id", "name", "surname", "email", "hash_password") VALUES
    (16, 'Paolo',   'Costa',   'paolo.costa@drivehub.it',   'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (17, 'Laura',   'Gatti',   'laura.gatti@drivehub.it',   'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (18, 'Giorgio', 'Serra',   'giorgio.serra@drivehub.it', 'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='),
    (19, 'Chiara',  'Fontana', 'chiara.fontana@drivehub.it','pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34=');

-- 7d. Manager (1 dirigente)
INSERT INTO "manager" ("id", "name", "surname", "email", "hash_password") VALUES
    (20, 'Vittorio', 'Brambilla', 'vittorio.manager@drivehub.it', 'pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34=');

-- -----------------------------------------------------------------------------
-- 8. Richieste d'acquisto e Proposte di vendita ("request")
-- -----------------------------------------------------------------------------
INSERT INTO "request" ("id", "vehicle", "client", "salesman", "deposit", "exp_date", "type") VALUES
    -- 10 Richieste d'acquisto su veicoli BLOCKED (alcune approvate con exp_date, altre in attesa di approvazione)
    (1,  47, 1,  16,   3600.00, DATE '2026-10-25', 'PURCHASE'),
    (2,  48, 2,  17,   4400.00, DATE '2026-10-28', 'PURCHASE'),
    (3,  49, 3,  18,   4150.00, DATE '2026-10-22', 'PURCHASE'),
    (4,  50, 4,  19,   3580.00, DATE '2026-10-30', 'PURCHASE'),
    (5,  51, 5,  16,   5100.00, DATE '2026-10-18', 'PURCHASE'),
    (6,  52, 6,  17,   4290.00, DATE '2026-10-20', 'PURCHASE'),
    (7,  53, 7,  NULL, 3750.00, NULL,               'PURCHASE'),
    (8,  54, 8,  NULL, 1500.00, NULL,               'PURCHASE'),
    (9,  55, 9,  NULL, 3250.00, NULL,               'PURCHASE'),
    (10, 56, 10, NULL, 4800.00, NULL,               'PURCHASE'),
    -- 4 Proposte di vendita inserite dai clienti sui veicoli in stato WAITING (43, 44, 45, 46)
    (11, 43, 11, NULL, -13500.00, NULL,             'SALE'),
    (12, 44, 12, NULL, -16800.00, NULL,             'SALE'),
    (13, 45, 13, NULL, -23000.00, NULL,             'SALE'),
    (14, 46, 14, NULL, -25500.00, NULL,             'SALE');

-- -----------------------------------------------------------------------------
-- 9. Test Drive (10 prenotazioni in attesa)
-- -----------------------------------------------------------------------------
INSERT INTO "test_drive" ("id", "vehicle", "client", "date") VALUES
    (1,  1,  1,  TIMESTAMP '2026-10-05 10:00:00'),
    (2,  2,  2,  TIMESTAMP '2026-10-06 11:00:00'),
    (3,  3,  3,  TIMESTAMP '2026-10-07 14:00:00'),
    (4,  6,  4,  TIMESTAMP '2026-10-08 10:00:00'),
    (5,  7,  5,  TIMESTAMP '2026-10-09 15:00:00'),
    (6,  8,  6,  TIMESTAMP '2026-10-12 16:00:00'),
    (7,  10, 7,  TIMESTAMP '2026-10-13 11:00:00'),
    (8,  11, 8,  TIMESTAMP '2026-10-14 15:00:00'),
    (9,  17, 9,  TIMESTAMP '2026-10-15 09:00:00'),
    (10, 18, 10, TIMESTAMP '2026-10-16 16:00:00');

-- -----------------------------------------------------------------------------
-- 10. Noleggi (10 noleggi: 5 passati già effettuati, 5 programmati per il futuro)
-- -----------------------------------------------------------------------------
INSERT INTO "rent" ("id", "vehicle", "client", "amount", "start", "end") VALUES
    -- 5 noleggi già effettuati
    (1,  27, 1,  315.00, DATE '2026-07-01', DATE '2026-07-08'),
    (2,  28, 2,  240.00, DATE '2026-07-15', DATE '2026-07-21'),
    (3,  29, 3,  385.00, DATE '2026-08-01', DATE '2026-08-08'),
    (4,  30, 4,  250.00, DATE '2026-08-10', DATE '2026-08-15'),
    (5,  31, 5,  450.00, DATE '2026-08-20', DATE '2026-08-26'),
    -- 5 noleggi programmati
    (6,  32, 6,  480.00, DATE '2026-10-10', DATE '2026-10-16'),
    (7,  33, 7,  595.00, DATE '2026-10-15', DATE '2026-10-22'),
    (8,  34, 8,  450.00, DATE '2026-10-18', DATE '2026-10-23'),
    (9,  35, 9,  665.00, DATE '2026-11-01', DATE '2026-11-08'),
    (10, 36, 10, 475.00, DATE '2026-11-05', DATE '2026-11-10');

-- -----------------------------------------------------------------------------
-- 11. Operazioni di Compravendita (40 operazioni totali in "sale")
-- -----------------------------------------------------------------------------
INSERT INTO "sale" ("id", "vehicle", "salesman", "client", "amount", "date") VALUES
    (1,  1,  16, 1,  34500.00, TIMESTAMP '2025-02-10 10:00:00'),
    (2,  2,  17, 2,  38900.00, TIMESTAMP '2025-03-15 11:30:00'),
    (3,  3,  18, 3,  42000.00, TIMESTAMP '2025-04-20 14:00:00'),
    (4,  4,  19, 4,  33500.00, TIMESTAMP '2025-05-12 16:15:00'),
    (5,  5,  16, 5,  14800.00, TIMESTAMP '2025-06-08 09:45:00'),
    (6,  6,  17, 6,  26900.00, TIMESTAMP '2025-07-22 15:20:00'),
    (7,  7,  18, 7,  39500.00, TIMESTAMP '2025-08-30 11:00:00'),
    (8,  8,  19, 8,  48500.00, TIMESTAMP '2025-09-14 17:30:00'),
    (9,  9,  16, 9,  33000.00, TIMESTAMP '2025-10-05 10:10:00'),
    (10, 10, 17, 10, 28900.00, TIMESTAMP '2025-11-18 12:00:00'),
    (11, 11, 18, 11, 41000.00, TIMESTAMP '2025-12-02 16:40:00'),
    (12, 12, 19, 12, 36500.00, TIMESTAMP '2026-01-15 14:00:00'),
    (13, 13, 16, 13, 49800.00, TIMESTAMP '2026-02-20 10:30:00'),
    (14, 14, 17, 14, 14200.00, TIMESTAMP '2026-03-05 15:50:00'),
    (15, 15, 18, 15, 12900.00, TIMESTAMP '2026-03-25 11:15:00'),
    (16, 16, 19, 1,  18500.00, TIMESTAMP '2026-04-10 09:20:00'),
    (17, 17, 16, 2,  17900.00, TIMESTAMP '2026-04-28 16:00:00'),
    (18, 18, 17, 3,  31500.00, TIMESTAMP '2026-05-15 14:45:00'),
    (19, 19, 18, 4,  46900.00, TIMESTAMP '2026-05-30 10:00:00'),
    (20, 20, 19, 5,  38500.00, TIMESTAMP '2026-06-12 11:30:00'),
    (21, 21, 16, 6,  54500.00, TIMESTAMP '2026-06-25 17:00:00'),
    (22, 22, 17, 7,  31000.00, TIMESTAMP '2026-07-04 12:15:00'),
    (23, 23, 18, 8,  37000.00, TIMESTAMP '2026-07-19 15:40:00'),
    (24, 24, 19, 9,  39500.00, TIMESTAMP '2026-08-02 10:50:00'),
    (25, 25, 16, 10, 13800.00, TIMESTAMP '2026-08-16 16:30:00'),
    (26, 26, 17, 11, 29800.00, TIMESTAMP '2026-08-28 14:10:00'),
    (27, 1,  18, 12, 33900.00, TIMESTAMP '2026-09-02 09:30:00'),
    (28, 2,  19, 13, 37500.00, TIMESTAMP '2026-09-10 11:20:00'),
    (29, 6,  16, 14, 26000.00, TIMESTAMP '2026-09-15 15:00:00'),
    (30, 7,  17, 15, 38800.00, TIMESTAMP '2026-09-20 16:45:00'),
    (31, 10, 18, 1,  27500.00, TIMESTAMP '2026-09-25 10:00:00'),
    (32, 14, 19, 2,  12500.00, TIMESTAMP '2026-09-28 12:30:00'),

    -- 8 penali per mancato pagamento / recesso (20% della caparra trattenuto come penale all'azienda, vehicle e salesman NULL)
    (33, NULL, NULL, 3,  680.00, TIMESTAMP '2026-05-20 12:00:00'),
    (34, NULL, NULL, 5,  820.00, TIMESTAMP '2026-06-18 12:00:00'),
    (35, NULL, NULL, 7,  710.00, TIMESTAMP '2026-07-10 12:00:00'),
    (36, NULL, NULL, 9,  900.00, TIMESTAMP '2026-08-05 12:00:00'),
    (37, NULL, NULL, 11, 550.00, TIMESTAMP '2026-08-22 12:00:00'),
    (38, NULL, NULL, 12, 480.00, TIMESTAMP '2026-09-05 12:00:00'),
    (39, NULL, NULL, 14, 760.00, TIMESTAMP '2026-09-18 12:00:00'),
    (40, NULL, NULL, 15, 840.00, TIMESTAMP '2026-09-26 12:00:00');

-- -----------------------------------------------------------------------------
-- 12. Riallineamento sequenze ID per PostgreSQL
-- -----------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS "user_id_seq" START WITH 1 INCREMENT BY 1;
SELECT setval(pg_get_serial_sequence('"model"', 'id'), COALESCE((SELECT MAX("id") FROM "model"), 1));
SELECT setval(pg_get_serial_sequence('"vehicle"', 'id'), COALESCE((SELECT MAX("id") FROM "vehicle"), 1));
SELECT setval('"user_id_seq"', COALESCE((SELECT MAX("id") FROM public."user"), 1));
SELECT setval(pg_get_serial_sequence('"request"', 'id'), COALESCE((SELECT MAX("id") FROM "request"), 1));
SELECT setval(pg_get_serial_sequence('"test_drive"', 'id'), COALESCE((SELECT MAX("id") FROM "test_drive"), 1));
SELECT setval(pg_get_serial_sequence('"rent"', 'id'), COALESCE((SELECT MAX("id") FROM "rent"), 1));
SELECT setval(pg_get_serial_sequence('"sale"', 'id'), COALESCE((SELECT MAX("id") FROM "sale"), 1));
