from pathlib import Path
import json, os, secrets, shutil, subprocess

session = Path('/tmp/drivehub-ticket18-session')
tools = Path('/tmp/drivehub-ticket16-tools/root')
repo = session/'install/DriveHub'
evidence = Path('/home/federico/dev/Drivehub/DriveHub/docs/evidence/r11-integrated')
evidence.mkdir(parents=True, exist_ok=True)
shutil.copytree(session/'before/DriveHub', repo, ignore=shutil.ignore_patterns('target'))
env = os.environ.copy()
env['JAVA_HOME'] = str(tools/'usr/lib/jvm/java-21-openjdk-amd64')
env['PATH'] = env['JAVA_HOME']+'/bin:'+str(tools/'usr/lib/postgresql/16/bin')+':'+env['PATH']
env['LD_LIBRARY_PATH'] = str(tools/'usr/lib/x86_64-linux-gnu')
env['DISPLAY'] = ':90'
env['DRIVEHUB_UI_DIR'] = str(session/'ui')
for database, prefix in [('drivehub_test','DRIVEHUB_TEST'),('drivehub','DRIVEHUB_SENTINEL')]:
    env[prefix+'_DB_URL'] = 'jdbc:postgresql://127.0.0.1:55436/'+database
    env[prefix+'_DB_USER'] = database
    env[prefix+'_DB_PASSWORD'] = secrets.token_hex(24)
private = session/'env.json'
private.write_text(json.dumps(env))
private.chmod(0o600)
config = repo/'src/main/resources/application.properties'
config.write_text('db.url='+env['DRIVEHUB_SENTINEL_DB_URL']+'\ndb.user=drivehub\ndb.password='+env['DRIVEHUB_SENTINEL_DB_PASSWORD']+'\n')
config.chmod(0o600)
pgdata = session/'pgdata'
with (evidence/'initdb.log').open('w') as log:
    subprocess.run(['initdb','-D',str(pgdata),'--no-locale','-E','UTF8','--auth-local=trust','--auth-host=scram-sha-256'],env=env,stdout=log,stderr=subprocess.STDOUT,check=True)
with (pgdata/'postgresql.conf').open('a') as out:
    out.write("\nlisten_addresses = '127.0.0.1'\nport = 55436\nunix_socket_directories = '/tmp/drivehub-ticket18-session'\n")
print('Prepared current-worktree installation copy, private config0600 and new cluster127.0.0.1:55436')
