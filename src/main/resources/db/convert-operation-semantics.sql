-- DriveHub: 841d4bc (a260598^) -> contratto finale dei ticket 01 + 02 + 03.
-- Eseguire con applicazione ferma, backup verificato e psql -X -v ON_ERROR_STOP=1 -f.
-- Non usare schema.sql/demo.sql per aggiornare un database popolato.
BEGIN;
SET LOCAL search_path = pg_catalog, public;
SET LOCAL lock_timeout = '5s';

DO $conversion$
DECLARE
    expected_columns jsonb := $signature${
    "sale.id": [
        "bigint",
        true,
        "d",
        ""
    ],
    "sale.date": [
        "timestamp without time zone",
        true,
        "",
        ""
    ],
    "sale.type": [
        "character varying(16)",
        true,
        "",
        ""
    ],
    "request.id": [
        "bigint",
        true,
        "d",
        ""
    ],
    "sale.amount": [
        "numeric(10,2)",
        true,
        "",
        ""
    ],
    "sale.client": [
        "bigint",
        true,
        "",
        ""
    ],
    "request.type": [
        "request_type",
        true,
        "",
        ""
    ],
    "sale.vehicle": [
        "bigint",
        false,
        "",
        ""
    ],
    "sale.salesman": [
        "bigint",
        false,
        "",
        ""
    ],
    "request.client": [
        "bigint",
        true,
        "",
        ""
    ],
    "request.deposit": [
        "numeric(10,2)",
        true,
        "",
        ""
    ],
    "request.vehicle": [
        "bigint",
        true,
        "",
        ""
    ],
    "request.exp_date": [
        "date",
        false,
        "",
        ""
    ],
    "request.salesman": [
        "bigint",
        false,
        "",
        ""
    ],
    "request.agreed_total": [
        "numeric(10,2)",
        false,
        "",
        ""
    ]
}$signature$::jsonb;
    common_constraints jsonb := $signature${
    "sale.pk_sale": "PRIMARY KEY (id)",
    "request.pk_request": "PRIMARY KEY (id)",
    "sale.fk_sale_client": "FOREIGN KEY (client) REFERENCES client(id) ON DELETE RESTRICT",
    "sale.fk_sale_vehicle": "FOREIGN KEY (vehicle) REFERENCES vehicle(id) ON DELETE SET NULL",
    "sale.fk_sale_salesman": "FOREIGN KEY (salesman) REFERENCES salesman(id) ON DELETE RESTRICT",
    "request.fk_request_client": "FOREIGN KEY (client) REFERENCES client(id) ON DELETE RESTRICT",
    "request.fk_request_vehicle": "FOREIGN KEY (vehicle) REFERENCES vehicle(id) ON DELETE RESTRICT",
    "request.uq_request_vehicle": "UNIQUE (vehicle)",
    "request.fk_request_salesman": "FOREIGN KEY (salesman) REFERENCES salesman(id) ON DELETE RESTRICT"
}$signature$::jsonb;
    source_checks jsonb := $signature${
    "sale.sale_type_check": "CHECK (((type)::text = ANY ((ARRAY['PURCHASE'::character varying, 'ACQUISITION'::character varying, 'PENALTY'::character varying])::text[])))",
    "sale.ck_sale_amount_type": "CHECK (((((type)::text = 'ACQUISITION'::text) AND (amount <= (0)::numeric)) OR (((type)::text = ANY ((ARRAY['PURCHASE'::character varying, 'PENALTY'::character varying])::text[])) AND (amount >= (0)::numeric))))",
    "request.ck_request_agreement": "CHECK ((((type = 'PURCHASE'::request_type) AND (agreed_total IS NOT NULL) AND (agreed_total >= (0)::numeric) AND (deposit >= (0)::numeric) AND (deposit <= agreed_total)) OR ((type = 'SALE'::request_type) AND (agreed_total IS NULL))))"
}$signature$::jsonb;
    final_checks jsonb := $signature${
    "request.ck_request_agreement": "CHECK ((((type = 'SALE'::request_type) AND (agreed_total IS NOT NULL) AND (agreed_total >= (0)::numeric) AND (deposit >= (0)::numeric) AND (deposit <= agreed_total)) OR ((type = 'PURCHASE'::request_type) AND (agreed_total IS NULL) AND (deposit >= (0)::numeric))))",
    "sale.ck_sale_amount_type": "CHECK ((amount >= (0)::numeric))",
    "sale.sale_type_check": "CHECK (((type)::text = ANY ((ARRAY['SALE'::character varying, 'PURCHASE'::character varying, 'PENALTY'::character varying])::text[])))"
}$signature$::jsonb;
    actual_columns jsonb;
    actual_constraints jsonb;
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_stat_activity
        WHERE datname = current_database() AND pid <> pg_backend_pid()
    ) THEN
        RAISE EXCEPTION 'Conversione rifiutata: chiudere tutte le altre connessioni al database';
    END IF;

    LOCK TABLE public.request, public.sale, public.bank_account IN ACCESS EXCLUSIVE MODE NOWAIT;
    IF EXISTS (
        SELECT 1 FROM pg_class WHERE oid IN ('public.request'::regclass, 'public.sale'::regclass,
            'public.bank_account'::regclass) AND (relkind <> 'r' OR relowner <> current_user::regrole
                OR relrowsecurity OR relforcerowsecurity)
    ) THEN
        RAISE EXCEPTION 'Conversione rifiutata: occorrono le tabelle ordinarie possedute dal ruolo corrente';
    END IF;
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgrelid IN ('public.request'::regclass,
        'public.sale'::regclass, 'public.bank_account'::regclass) AND NOT tgisinternal)
        OR EXISTS (SELECT 1 FROM pg_rewrite WHERE ev_class IN ('public.request'::regclass,
            'public.sale'::regclass, 'public.bank_account'::regclass)) THEN
        RAISE EXCEPTION 'Origine non riconosciuta: trigger o regole personalizzati';
    END IF;

    SELECT jsonb_object_agg(c.relname || '.' || a.attname,
        jsonb_build_array(format_type(a.atttypid, a.atttypmod), a.attnotnull, a.attidentity, a.attgenerated))
    INTO actual_columns
    FROM pg_attribute a JOIN pg_class c ON c.oid = a.attrelid
    WHERE a.attrelid IN ('public.request'::regclass, 'public.sale'::regclass)
        AND a.attnum > 0 AND NOT a.attisdropped;
    SELECT jsonb_object_agg(c.relname || '.' || k.conname, pg_get_constraintdef(k.oid))
    INTO actual_constraints
    FROM pg_constraint k JOIN pg_class c ON c.oid = k.conrelid
    WHERE k.conrelid IN ('public.request'::regclass, 'public.sale'::regclass);

    IF actual_columns IS DISTINCT FROM expected_columns
        OR (SELECT array_agg(enumlabel::text ORDER BY enumsortorder) FROM pg_enum
            WHERE enumtypid = 'public.request_type'::regtype) IS DISTINCT FROM ARRAY['PURCHASE','SALE']
        OR EXISTS (SELECT 1 FROM public.request
            WHERE deposit::text = 'NaN' OR agreed_total::text = 'NaN')
        OR EXISTS (SELECT 1 FROM public.sale WHERE amount::text = 'NaN') THEN
        RAISE EXCEPTION 'Origine non riconosciuta: colonne, precisione, enum o importi incompatibili';
    END IF;
    IF actual_constraints = common_constraints || final_checks THEN
        RAISE NOTICE 'Contratto finale già presente: nessuna modifica';
        RETURN;
    END IF;
    IF actual_constraints IS DISTINCT FROM common_constraints || source_checks
        OR EXISTS (SELECT 1 FROM public.request WHERE type = 'SALE' AND deposit > 0) THEN
        RAISE EXCEPTION 'Origine non riconosciuta: richiesto il contratto 841d4bc precedente ai ticket 01 e 02';
    END IF;

    ALTER TABLE public.request DROP CONSTRAINT ck_request_agreement;
    ALTER TABLE public.sale DROP CONSTRAINT sale_type_check, DROP CONSTRAINT ck_sale_amount_type;

    -- Le espressioni leggono entrambe il tipo originario della riga, anche a zero.
    UPDATE public.request
    SET type = CASE type WHEN 'PURCHASE' THEN 'SALE'::public.request_type ELSE 'PURCHASE'::public.request_type END,
        deposit = CASE type WHEN 'SALE' THEN -deposit ELSE deposit END;
    UPDATE public.sale
    SET type = CASE type WHEN 'PURCHASE' THEN 'SALE' WHEN 'ACQUISITION' THEN 'PURCHASE' ELSE type END,
        amount = CASE type WHEN 'ACQUISITION' THEN -amount ELSE amount END;

    ALTER TABLE public.request ADD CONSTRAINT ck_request_agreement CHECK (
        (type = 'SALE' AND agreed_total IS NOT NULL AND agreed_total >= 0
            AND deposit >= 0 AND deposit <= agreed_total)
        OR (type = 'PURCHASE' AND agreed_total IS NULL AND deposit >= 0));
    ALTER TABLE public.sale ADD CONSTRAINT sale_type_check CHECK (type IN ('SALE','PURCHASE','PENALTY')),
        ADD CONSTRAINT ck_sale_amount_type CHECK (amount >= 0);
    RAISE NOTICE 'Conversione 841d4bc -> contratto finale completata; nessun movimento bancario';
END;
$conversion$;
COMMIT;
