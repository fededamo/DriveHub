-- Solo test: contratto precedente a 01 e 02 (841d4bc), non una demo applicativa.
INSERT INTO brand VALUES ('Fiat');
INSERT INTO model (id,brand,name,release_price,release_year) VALUES (1,'Fiat','Legacy',20000,2020);
INSERT INTO client VALUES (1,'Mario','Rossi','mario.rossi@email.it','pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34='), (2,'Luigi','Verdi','luigi.verdi@email.it','pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34=');
INSERT INTO salesman VALUES (16,'Paolo','Costa','paolo.costa@drivehub.it','pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34=');
INSERT INTO manager VALUES (20,'Vittorio','Manager','vittorio.manager@drivehub.it','pbkdf2-sha256$210000$7IfCdVFi3d11S9d7NKz0RA==$EiGCP0RiDhnGIjBB2j3r3rg83St531wgatyge3dA+34=');
INSERT INTO bank_account VALUES ('IT0000000000000000000000001',1,26000),('IT0000000000000000000000002',2,1000);
INSERT INTO vehicle (id,model,vin,licence_plate,kilometers,price,price_mode,year,state)
SELECT n,1,'LEGACY' || lpad(n::text,11,'0'),'OLD' || n,0,20000,'MANUAL',2020,
       CASE WHEN n BETWEEN 1 AND 4 THEN 'BLOCKED' WHEN n IN (5,6) THEN 'WAITING' ELSE 'SOLD' END::vehicle_state
FROM generate_series(1,8) n;
INSERT INTO request (id,vehicle,client,salesman,deposit,agreed_total,exp_date,type) VALUES
(101,1,1,16,4000,20000,CURRENT_DATE + 30,'PURCHASE'),
(102,2,1,NULL,4000,20000,NULL,'PURCHASE'),
(103,3,1,16,4000,20000,CURRENT_DATE - 1,'PURCHASE'),
(104,4,1,16,0,0,CURRENT_DATE + 30,'PURCHASE'),
(105,5,1,NULL,-16000,NULL,NULL,'SALE'),
(106,6,1,NULL,0,NULL,NULL,'SALE');
INSERT INTO sale (id,vehicle,salesman,client,amount,date,type) VALUES
(201,5,16,1,20000,'2024-01-01 10:00','PURCHASE'),
(202,7,16,1,-16000,'2024-01-02 10:00','ACQUISITION'),
(203,NULL,NULL,1,800,'2024-01-03 10:00','PENALTY'),
(204,6,16,1,0,'2024-01-04 10:00','PURCHASE'),
(205,8,16,1,0,'2024-01-05 10:00','ACQUISITION'),
(206,NULL,NULL,1,0,'2024-01-06 10:00','PENALTY'),
(207,5,16,2,19000,'2023-01-01 10:00','PURCHASE'),
(208,5,16,2,-10000,'2024-02-01 10:00','ACQUISITION');
INSERT INTO rent (id,vehicle,client,amount,start,"end") VALUES (301,7,2,100,'2023-01-01','2023-01-02');
INSERT INTO test_drive (id,vehicle,client,date) VALUES (401,8,2,'2023-01-03 10:00');
SELECT setval('user_id_seq',77,true), setval('model_id_seq',31,true), setval('vehicle_id_seq',81,true),
       setval('request_id_seq',151,true), setval('sale_id_seq',251,true),
       setval('rent_id_seq',351,true), setval('test_drive_id_seq',451,false);
