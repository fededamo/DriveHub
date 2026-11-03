from pathlib import Path
import json, subprocess

s = Path('/tmp/drivehub-ticket16-session')
e = Path('/home/federico/dev/Drivehub/DriveHub/docs/evidence/r09-instructions')
env = json.loads((s/'env.json').read_text())
repo = s/'install/DriveHub'
psql = ['psql','-X','-v','ON_ERROR_STOP=1','-h',str(s),'-p','55434']
with (e/'database-setup.log').open('w') as log:
    for db, prefix in [('drivehub_test','DRIVEHUB_TEST'),('drivehub','DRIVEHUB_SENTINEL')]:
        password=env[prefix+'_DB_PASSWORD']
        sql=f"CREATE ROLE {db} LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS PASSWORD '{password}';\nCREATE DATABASE {db} OWNER {db};\nREVOKE CONNECT ON DATABASE {db} FROM PUBLIC;\nGRANT CONNECT ON DATABASE {db} TO {db};\n"
        subprocess.run(psql+['-d','postgres'],input=sql,text=True,env=env,stdout=log,stderr=subprocess.STDOUT,check=True)
    appenv=env|{'PGPASSWORD':env['DRIVEHUB_SENTINEL_DB_PASSWORD']}
    app=['psql','-X','-v','ON_ERROR_STOP=1','--single-transaction','-h','127.0.0.1','-p','55434','-U','drivehub','-d','drivehub']
    subprocess.run(app+['-f','src/main/resources/db/schema.sql'],cwd=repo,env=appenv,stdout=log,stderr=subprocess.STDOUT,check=True)
    subprocess.run(app+['-c',"SELECT count(*) AS clients_before_optional_demo FROM client; SELECT extname FROM pg_extension WHERE extname='btree_gist';"],cwd=repo,env=appenv,stdout=log,stderr=subprocess.STDOUT,check=True)
    subprocess.run(app+['-f','src/main/resources/db/demo.sql'],cwd=repo,env=appenv,stdout=log,stderr=subprocess.STDOUT,check=True)
    subprocess.run(app+['-c',"INSERT INTO brand(name) VALUES ('R00_SENTINEL'); SELECT current_database(), current_user, (SELECT count(*) FROM client) AS clients, (SELECT count(*) FROM sale) AS operations;"],env=appenv,stdout=log,stderr=subprocess.STDOUT,check=True)
with (e/'versions.txt').open('w') as log:
    for command in [['java','-version'],['javac','-version'],['mvn','-version'],['postgres','--version'],['psql','--version']]:
        subprocess.run(command,env=env,stdout=log,stderr=subprocess.STDOUT,check=True)
print('Dedicated nonadmin roles/databases created; empty schema then optional demo and sentinel loaded')
