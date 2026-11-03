from pathlib import Path
import hashlib, json, subprocess, sys

s=Path('/tmp/drivehub-ticket17-session')
e=Path('/home/federico/dev/Drivehub/DriveHub/docs/evidence/r09-diagrams')
env=json.loads((s/'env.json').read_text())
phase=sys.argv[1]
assert phase in ('before-suite','after-suite')
env.update(PGPASSWORD=env['DRIVEHUB_SENTINEL_DB_PASSWORD'],PGOPTIONS='-c default_transaction_read_only=on')
cmd=['psql','-X','-A','-t','-v','ON_ERROR_STOP=1','-h','127.0.0.1','-p','55435','-U','drivehub','-d','drivehub']
def query(sql):
    return subprocess.check_output(cmd+['-c',sql],env=env,text=True)
identity=query("SELECT current_database(),current_user,current_setting('server_version'),current_setting('transaction_read_only'); SELECT name FROM brand WHERE name='R00_SENTINEL';")
assert 'R00_SENTINEL' in identity and 'drivehub|drivehub|16.' in identity and '|on' in identity
tables=query("SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename").splitlines()
sequences=query("SELECT sequencename FROM pg_sequences WHERE schemaname='public' ORDER BY sequencename").splitlines()
hashes={}
for name in tables:
    hashes['table/'+name]=hashlib.sha256(query(f'SELECT row_to_json(r)::text FROM public."{name}" r ORDER BY row_to_json(r)::text').encode()).hexdigest()
for name in sequences:
    hashes['sequence/'+name]=hashlib.sha256(query(f'SELECT last_value,is_called FROM public."{name}"').encode()).hexdigest()
if phase=='after-suite':
    assert hashes==json.loads((e/'before-suite-application.sha256.json').read_text()),'Application copy changed during suite'
(e/(phase+'-application.sha256.json')).write_text(json.dumps(hashes,indent=2)+'\n')
adminenv=env.copy()
adminenv.pop('PGPASSWORD')
admin=['psql','-X','-A','-t','-v','ON_ERROR_STOP=1','-h',str(s),'-p','55435','-d','postgres']
privileges=subprocess.check_output(admin+['-c',"SELECT datname,pg_get_userbyid(datdba) FROM pg_database ORDER BY datname; SELECT rolname,rolsuper,rolcreatedb,rolcreaterole,rolreplication,rolbypassrls FROM pg_roles WHERE rolname IN ('drivehub','drivehub_test'); SELECT count(*) FROM pg_auth_members m JOIN pg_roles r ON m.member=r.oid WHERE r.rolname='drivehub_test';"],env=adminenv,text=True)
(e/(phase+'-environment.txt')).write_text(identity+'\n'+privileges+f'\nREAD ONLY application fingerprint: {len(tables)} tables, {len(sequences)} sequences\n')
print(phase,'sentinel and application fingerprint verified READ ONLY')
