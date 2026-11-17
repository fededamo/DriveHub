"""Tavole selezionate a mano con la skill drawio; non genera UML dal codice."""
from pathlib import Path
from copy import deepcopy
import xml.etree.ElementTree as E
import re

DOC=Path(__file__).resolve().parents[2]
OUT=DOC/'diagrammi'
GRAY='#596579'; RED='#F8CECC'; YELLOW='#FFF2CC'; BLUE='#DAE8FC'; GREEN='#D5E8D4'

class Drawing:
    def __init__(self,title,w=1200,h=850):
        self.tree=E.Element('mxfile',host='Electron',version='32.6.10')
        d=E.SubElement(self.tree,'diagram',name=title,id='ticket10')
        self.model=E.SubElement(d,'mxGraphModel',adaptiveColors='auto',page='1',pageScale='1',pageWidth=str(w),pageHeight=str(h),background='#ffffff')
        self.root=E.SubElement(self.model,'root');E.SubElement(self.root,'mxCell',id='0');E.SubElement(self.root,'mxCell',id='1',parent='0')
        self.n=0
        self.box('title',title,20,8,w-40,32,'text;align=center;fontSize=22;fontStyle=1;')
    def box(self,id,text,x,y,w,h,style='',parent='1'):
        c=E.SubElement(self.root,'mxCell',id=id,value=text,vertex='1',parent=parent,style='html=1;whiteSpace=wrap;fontFamily=Arial;fontSize=14;strokeColor='+GRAY+';'+style)
        E.SubElement(c,'mxGeometry',x=str(x),y=str(y),width=str(w),height=str(h),**{'as':'geometry'})
        return c
    def group(self,id,title,x,y,w,h,color):
        return self.box(id,title,x,y,w,h,'shape=folder;tabWidth=110;tabHeight=24;align=left;verticalAlign=top;spacingTop=3;spacingLeft=8;fillColor='+color+';')
    def cls(self,id,title,attrs,methods,x,y,w=220,h=100,parent='1'):
        text='<div style="text-align:center;font-size:14px"><b>'+title+'</b></div><hr style="margin:2px 0"><div style="text-align:left;font-size:12px;line-height:1.1">'+attrs+'<hr style="margin:2px 0">'+methods+'</div>'
        return self.box(id,text,x,y,w,h,'rounded=0;fillColor=#ffffff;verticalAlign=top;spacing=5;',parent)
    def edge(self,a,b,label='',kind='use',parent='1',ports='',points=()):
        self.n+=1
        st={'use':'dashed=1;endArrow=open;endFill=0;','assoc':'endArrow=open;endFill=0;','extends':'endArrow=block;endFill=0;','implements':'dashed=1;endArrow=block;endFill=0;'}[kind]
        c=E.SubElement(self.root,'mxCell',id='edge'+str(self.n),source=a,target=b,value=label,parent=parent,edge='1',style='edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;fontFamily=Arial;fontSize=13;labelBackgroundColor=#ffffff;strokeColor='+GRAY+';'+st)
        c.set('style',c.get('style')+ports)
        g=E.SubElement(c,'mxGeometry',relative='1',**{'as':'geometry'})
        if points:
            path=E.SubElement(g,'Array',**{'as':'points'})
            for x,y in points:E.SubElement(path,'mxPoint',x=str(x),y=str(y))
    def note(self,id,text,x,y,w,h):
        return self.box(id,text,x,y,w,h,'shape=note;fillColor=#FFFBE6;strokeColor=#D2B65A;align=left;spacing=8;fontSize=12;')
    def save(self,name):
        save(self.tree,name)

def save(tree,name):
    root=tree.getroot() if isinstance(tree,E.ElementTree) else tree
    for model in root.iter('mxGraphModel'):model.set('adaptiveColors','auto')
    for layer in root.iter('root'):
        cells=list(layer);ids={c.get('id') for c in cells};assert len(ids)==len(cells),name
        for c in cells:
            assert c.get('parent')!=c.get('id')
            assert all(c.get(k) is None or c.get(k) in ids for k in ('parent','source','target')),(name,c.attrib)
            if c.get('edge')=='1':assert c.find('mxGeometry') is not None
    E.indent(root);E.ElementTree(root).write(OUT/(name+'.drawio'),encoding='utf-8')

# Le copie corrette sono confrontate con gli originali, non sostituite con disegni diversi.
for name,updates in {
 'casi-uso-autori':{'WRvqtLsy7lfnVHb6Kxkt-23':'Vendita al cliente:<br>acconto e saldo<br>[UC-03]', 'WRvqtLsy7lfnVHb6Kxkt-29':'Autorizzare vendita<br>al cliente [UC-04]<br>(sottofunzione F)'},
 'navigazione-autori':{'Ku0o8qk710Uc4jAowcEN-14':'Dispatch dal ruolo<br>autenticato<br>(nessuna scelta)', 'Ku0o8qk710Uc4jAowcEN-1':'LoginView: vista iniziale<br>tab Accedi / Registrati'},
 'database-er-autori':{}
}.items():
    t=E.parse(OUT/(name+'.drawio'))
    for c in t.iter('mxCell'):
        if c.get('id') in updates:c.set('value',updates[c.get('id')])
    if name=='database-er-autori':
        # La nota precisa il confine tra generalizzazione concettuale e vincoli SQL.
        for c in t.iter('mxCell'):
            v=c.get('value','')
            if 'UNION ALL' in v and 'user_id_seq' in v:
                c.set('value',v.replace('user_id_seq','user_id_seq'))
        for c in t.iter('mxCell'):
            if c.get('id')=='3XymO5eWcuveRKm0xSgM-3':
                c.set('value','<b>LEGENDA</b><br>PK: primaria; FK: riferimento; N: NOT NULL; U: UNIQUE.<br>Attributi privi di N o PK: NULL ammesso.<br>t, d: ruoli totali e disgiunti nel modello concettuale.<br>User non è una tabella padre: ID concettuale, nessuna PK globale.<br>Le tredici relazioni sono esplicitate anche nel testo di 2.8.2.')
        t.getroot().set('ticket10','DDL verificato: 13 tabelle, 15 FK, 4 enum; User concettuale; legenda nella seconda pagina')
    save(t,name)

# Package: copia nativa degli autori, stessi contenitori e forme, nomi e archi correnti.
t=E.parse(DOC.parent/'Package diagram.drawio');layer=t.find('.//root');cells={c.get('id'):c for c in layer}
p='cN9yhG_JliOIjsbcm2MH-';q='b96C6uQ24AyX2nrNgCQE-'
updates={q+'1':'dao',q+'4':'dao.postgres',q+'5':'dao (contratti)',p+'2':'PRESENTAZIONE / AVVIO',p+'8':'it.unifi.drivehub',p+'10':'presentation.view',p+'11':'JavaFX (esterno)',p+'4':'BUSINESS',p+'15':'security',p+'16':'event',p+'17':'service',p+'3':'DOMINIO',p+'19':'domain.user',p+'20':'domain.vehicle',p+'21':'domain.discount',p+'22':'domain.operation',p+'23':'nessun import<br>verso altri livelli'}
for id,v in updates.items():cells[id].set('value',v)
# Gli archi storici coinvolgevano core/controller/strategie: riscritti sui confini attuali.
removed={c.get('id') for c in layer if c.get('edge')=='1'}
for c in list(layer):
 if c.get('edge')=='1' or c.get('parent') in removed:layer.remove(c)
# Mantiene le geometrie e i gruppi originali, correggendo solo le dipendenze.
routes={
 0:(p+'9','exitX=1;exitY=0.5;entryX=0;entryY=0.5;',[]),
 1:(p+'9','exitX=0.5;exitY=1;entryX=0.5;entryY=1;',[(224,140),(71,140)]),
 2:('1','exitX=0.5;exitY=1;entryX=0.5;entryY=0;',[(-919,260),(-641,260)]),
 3:('1','exitX=0.5;exitY=0;entryX=0;entryY=0.5;',[(-919,15),(-450,15),(-450,685)]),
 4:('1','exitX=0.5;exitY=1;entryX=0.2;entryY=0;',[(-766,245),(-677,245)]),
 5:('1','exitX=1;exitY=0.5;entryX=0;entryY=0.5;',[(-470,570),(-470,745)]),
 6:(q+'3','exitX=0.5;exitY=1;entryX=0.5;entryY=0;',[]),
 8:(p+'14','exitX=0.5;exitY=1;entryX=0.5;entryY=1;',[(499,180),(145,180)]),
 9:(p+'14','exitX=0;exitY=0.5;entryX=1;entryY=0.5;',[]),
 10:(p+'9','exitX=1;exitY=0.5;entryX=0;entryY=0.5;',[])
}
for i,(a,b) in enumerate([(p+'8',p+'10'),(p+'10',p+'8'),(p+'8',p+'17'),(p+'8',q+'4'),(p+'10',p+'17'),(p+'17',q+'5'),(q+'4',q+'5'),(q+'4',p+'27'),(p+'17',p+'15'),(p+'17',p+'16'),(p+'10',p+'11')]):
 parent,ports,points=routes.get(i,('1','',[]))
 c=E.SubElement(layer,'mxCell',id='t10-dep-'+str(i),source=a,target=b,parent=parent,edge='1',style='edgeStyle=orthogonalEdgeStyle;html=1;dashed=1;endArrow=open;endFill=0;strokeColor=#526174;'+ports)
 g=E.SubElement(c,'mxGeometry',relative='1',**{'as':'geometry'})
 if points:
  path=E.SubElement(g,'Array',**{'as':'points'})
  for x,y in points:E.SubElement(path,'mxPoint',x=str(x),y=str(y))
# Nota mantiene leggibili le dipendenze ripetute invece di moltiplicare sedici archi.
c=E.SubElement(layer,'mxCell',id='t10-note',value='Dipendenze ulteriori (frecce verso il fornitore):<br>avvio → dao, event, security, domain.user.<br>presentation.view, service, dao, dao.postgres → i quattro package domain.<br>Il dominio non importa altri livelli; UserDashboardNavigator è un contratto interno.<br>Le quattro aree sono raggruppamenti: solo 11 package hanno tipi propri.<br>JDBC attraversa servizi, DAO ed event; non sono strati rigidamente isolati.',parent='1',vertex='1',style='shape=note;html=1;whiteSpace=wrap;align=left;spacing=8;fontSize=14;fillColor=#FFFBE6;strokeColor=#D2B65A;')
E.SubElement(c,'mxGeometry',x='-100',y='840',width='790',height='165',**{'as':'geometry'})
save(t,'package-autori')

# Quattro ricostruzioni: stessa notazione UML e palette dell'originale, classi correnti.
d=Drawing('DriveHub — Domain Model')
d.group('users','domain.user',20,55,555,365,RED)
d.cls('user','«interface» User','identità e credenziali','+ openDashboard(navigator)',30,45,parent='users')
d.cls('abstract','«abstract» AbstractUser','# id : Long<br># email : String','+ updateEmail(email)',290,45,parent='users')
d.cls('client','Client','- bankAccount : BankAccount [0..1]','+ openDashboard(navigator)',30,170,h=85,parent='users')
d.cls('bank','BankAccount','- clientId : Long (ID)<br>- balance : BigDecimal','+ deposit(...); withdraw(...)',30,270,h=85,parent='users')
d.edge('client','bank',kind='assoc',parent='users')
d.cls('salesman','Salesman','','+ openDashboard(navigator)',290,180,h=70,parent='users')
d.cls('manager','Manager','','+ openDashboard(navigator)',290,270,h=70,parent='users')
d.edge('abstract','user',kind='implements',parent='users')
d.edge('client','abstract',kind='extends',parent='users',ports='exitX=1;exitY=0.5;entryX=0.5;entryY=1;',points=[(270,215),(270,160),(400,160)])
d.edge('salesman','abstract',kind='extends',parent='users')
d.edge('manager','abstract',kind='extends',parent='users',ports='exitX=1;exitY=0.5;entryX=1;entryY=0.5;',points=[(535,305),(535,95)])
d.group('catalog','domain.vehicle',605,55,575,350,RED)
d.cls('brand','Brand','- name : String','+ create(name)',25,40,h=65,parent='catalog')
d.cls('model','Model','- brand : String (chiave)<br>- releasePrice; releaseYear','+ create(...)',25,125,h=100,parent='catalog')
d.cls('vehicle','Vehicle','- modelId : Long (ID); price<br>- priceMode : PriceMode<br>- state : VehicleState','+ getSelectedBasePrice(model)',305,40,h=130,parent='catalog')
d.cls('states','«enumeration» VehicleState','SALE / RENT / BLOCKED<br>WAITING / SOLD','',305,225,h=100,parent='catalog')
d.cls('mode','«enumeration» PriceMode','AUTOMATIC / MANUAL','',25,250,h=75,parent='catalog')
d.edge('vehicle','states',kind='assoc',parent='catalog')
d.edge('vehicle','model',parent='catalog',ports='exitX=0;exitY=0.5;entryX=1;entryY=0.5;',points=[(275,105),(275,175)])
d.group('discounts','domain.discount',20,430,555,290,RED)
d.cls('discount','«abstract» Discount','value : int [0..50]','+ getValue()',160,42,parent='discounts')
d.cls('md','ModelDiscount','modelId : Long (ID)','',25,180,w=220,h=75,parent='discounts')
d.cls('vd','VehicleDiscount','vehicleId : Long (ID)','',305,180,w=220,h=75,parent='discounts')
d.edge('md','discount',kind='extends',parent='discounts');d.edge('vd','discount',kind='extends',parent='discounts')
d.group('ops','domain.operation',605,430,575,290,RED)
d.cls('request','Request','vehicleId; clientId; salesmanId?<br>deposit; agreedTotal?; type','+ getRemainingAmount()',25,42,h=110,parent='ops')
d.cls('sale','Sale','vehicleId?; salesmanId?; clientId<br>amount ≥ 0; type : SaleType','+ createSaleRecord(...)',305,42,h=110,parent='ops')
d.cls('rent','Rent','- vehicleId; clientId (ID)<br>- amount; startDate; endDate','+ create(...)',25,177,h=90,parent='ops')
d.cls('td','TestDrive','- vehicleId; clientId (ID)<br>- date : LocalDateTime','+ create(...)',305,177,h=90,parent='ops')
d.note('note','Riferimenti per ID: nessuna associazione Java cliente–veicolo–operazione.<br>Client → BankAccount [0..1] è invece un riferimento reale; il conto conserva clientId e controlla deposit/withdraw.<br>Eventi e Observer non appartengono più al dominio: sono nel Business Layer (event / service).<br>Triangolo vuoto: ereditarietà (tratto continuo) / realizzazione (tratteggio); freccia aperta: riferimento / uso. Membri selezionati; dettagli CL-02–05.',20,745,1160,90)
d.save('classi-dominio-insieme')

d=Drawing('DriveHub — Business Layer')
d.group('svc','service',20,55,735,545,YELLOW)
for id,title,attrs,methods,x,y in [
('auth','AuthService','UserDao; BankAccountDao; db<br>PasswordHasher','+ login(...); registerClient(...)',25,45),
('test','TestDriveService','VehicleDao; UserDao; TestDriveDao','+ bookTestDrive(...)',25,205),
('query','OperationQueryService','DAO delle operazioni','+ getClientRequests(...)',25,365),
('catalog','CatalogService','catalogo e righe di lettura','+ getAllBrands(); getAllModels()',485,365),
('purchase','PurchaseService','DAO; db; calcolatore; publisher','+ initiatePurchase(...); completePurchase(...)',255,45),
('rental','RentalService','DAO; db; calcolatore; publisher','+ bookRental(...); changeVehicleRentalState(...)',255,205),
('cessione','SaleToDealershipService','DAO; db; calcolatore','+ submitSaleProposal(...); approveSaleProposal(...)',485,45),
('price','PriceCalculatorService','calcolo centralizzato concreto','+ calculateVehiclePrice(...); calculateTotalRentalCost(...)',485,205)]:
 d.cls(id,title,attrs,methods,x,y,w=220,h=125,parent='svc')
d.edge('purchase','price',kind='assoc',parent='svc',ports='exitX=1;exitY=0.5;entryX=0;entryY=0.5;',points=[(480,107),(480,267)])
d.edge('rental','price',kind='assoc',parent='svc');d.edge('cessione','price',kind='assoc',parent='svc')
d.group('sql','dao / JDBC',800,55,380,185,BLUE)
d.cls('db','DatabaseManager','apre connessioni; callback','+ executeInTransaction(...)<br>+ executeInTransactionWithResult(...)',20,40,w=340,h=120,parent='sql')
d.edge('svc','db','servizi di scrittura',ports='exitX=1;exitY=0.5;entryX=0;entryY=0.5;',points=[(780,327),(780,155)])
d.group('security','security',800,270,380,210,YELLOW)
d.cls('hasher','«interface» PasswordHasher','hash / verify','+ hash(password); verify(...)',20,40,w=155,h=125,parent='security')
d.cls('pbk','Pbkdf2Password<br>Hasher','PBKDF2','+ hash(...); verify(...)',200,40,w=160,h=125,parent='security');d.edge('pbk','hasher',kind='implements',parent='security')
d.group('event','event',20,615,735,150,RED)
d.cls('publisher','VehicleEventPublisher','- observers : List','+ attach(...)<br>+ publishCommitment(event,c)',20,30,w=220,h=110,parent='event')
d.cls('contract','«interface»<br>VehicleCommitmentObserver','','+ onVehicleCommitted(event,c)',270,30,w=235,h=110,parent='event')
d.cls('eventrecord','«record»<br>VehicleCommitment<br>Event','vehicleId : Long','',535,30,w=180,h=110,parent='event')
d.edge('publisher','contract',kind='assoc',parent='event')
d.cls('observer','TestDriveCancellationObserver','TestDriveDao','+ onVehicleCommitted(event,c)',815,535,w=350,h=110)
d.edge('observer','contract',kind='implements',ports='exitX=0;exitY=0.5;entryX=1;entryY=0.5;',points=[(790,590),(790,610),(540,610),(540,700)])
d.note('note','AppContext costruisce e inietta DAO, servizi, hasher e publisher; registra l’Observer una volta.<br>PurchaseService e RentalService usano VehicleEventPublisher; onVehicleCommitted riceve VehicleCommitmentEvent e Connection.<br>Il calcolatore non realizza una Strategy. DatabaseManager sostituisce TransactionRunner / UnitOfWork.<br>Observer sincrono sulla stessa Connection prima del commit; errore → rollback. Nessuna outbox. Membri e frecce selezionati; dettagli CL-01/06/07/11.',20,775,1160,65)
d.save('classi-business-insieme')

d=Drawing('DriveHub — DAO Layer')
d.group('dao','dao',600,55,580,650,BLUE)
d.cls('db','DatabaseManager','configurazione JDBC','+ getConnection()<br>+ executeInTransaction(callback)',30,50,w=245,h=130,parent='dao')
d.cls('consumer','«interface»<br>TransactionConsumer','','+ execute(Connection) : void',310,35,w=245,h=85,parent='dao')
d.cls('function','«interface»<br>TransactionFunction&lt;T&gt;','','+ execute(Connection) : T',310,135,w=245,h=85,parent='dao')
d.edge('db','consumer',parent='dao',ports='exitX=1;exitY=0.25;entryX=0;entryY=0.5;',points=[(292,82),(292,77)])
d.edge('db','function',parent='dao',ports='exitX=1;exitY=0.9;entryX=0;entryY=0.5;',points=[(292,167),(292,177)])
d.group('pg','dao.postgres',20,55,540,650,BLUE)
for i,(a,m) in enumerate([('User','findByEmail(...)'),('Vehicle','findByIdForUpdate(...)'),('Rent','save(rent,c)'),('Request','findByIdForUpdate(...)')]):
 y=230+i*95
 d.cls('i'+a,'«interface» '+a+'Dao','', '+ '+m,315,y,w=240,h=75,parent='dao')
 d.cls('p'+a,'Postgres'+a+'Dao','- db : DatabaseManager','+ '+m,30,y,w=450,h=75,parent='pg')
 d.edge('p'+a,'i'+a,kind='implements')
d.edge('pg','db','gestore condiviso')
d.note('note','Le altre sei porte seguono lo stesso schema: BankAccountDao, BrandDao, ModelDao, DiscountDao, TestDriveDao, SaleDao, con le rispettive Postgres*Dao.<br>Le firme con c : Connection partecipano alla transazione del servizio; letture autonome aprono e chiudono connessioni proprie. I mapper sono nei DAO concreti.<br>Commit / rollback appartengono al gestore. Non esistono DaoFactory, UnitOfWork, JdbcEntityLoader né bootstrap Java.<br>Realizzazione: tratteggio con triangolo vuoto; uso: freccia tratteggiata. La tavola seleziona quattro coppie come nell’originale; CL-08a/08b/09 coprono tutte le porte e implementazioni.',20,735,1160,105)
d.save('classi-dao-insieme')

d=Drawing('DriveHub — Presentation Layer')
d.group('presentation','presentation.view',20,55,755,605,GREEN)
for id,title,attr,meth,x,y in [
('login','LoginView','AuthService; Consumer&lt;User&gt;','tab Accedi / Registrati',25,50),
('client','ClientDashboardView','Client; servizi; Runnable onLogout','cataloghi / operazioni / proposta',400,50),
('salesman','SalesmanDashboardView','Salesman; servizi; Runnable onLogout','richieste / inventario',25,225),
('manager','ManagerDashboardView','Manager; servizi; Runnable onLogout','marchi / modelli / veicoli / sconti / registro',400,225),
('account','AccountManagementDialog','utility: metodi statici','+ show(owner,user,auth,callback,...)',25,425),
('async','AsyncLoad&lt;T&gt; (package)','Task; worker virtuali','load / risultato corrente / dispose',400,425)]:
 d.cls(id,title,attr,meth,x,y,w=330,h=125,parent='presentation')
d.cls('border','«JavaFX» BorderPane','', '',825,55,w=330,h=65)
for id,ports,points in [
 ('login','exitX=0.5;exitY=0;entryX=0;entryY=0.4;',[(210,90),(805,90)]),
 ('client','exitX=0.5;exitY=0;entryX=0;entryY=0.2;',[(585,80),(805,80)]),
 ('salesman','exitX=1;exitY=0.5;entryX=0;entryY=0;',[(395,342),(395,60),(805,60)]),
 ('manager','exitX=1;exitY=0.5;entryX=0;entryY=0.85;',[(790,342),(790,110)])]:
 d.edge(id,'border',kind='extends',ports=ports,points=points)
d.group('root','it.unifi.drivehub',810,180,370,225,GREEN)
d.cls('app','App','Stage; AppContext','+ start(Stage)<br>showDashboardForUser(user)',20,40,w=330,h=140,parent='root')
d.group('domain','domain.user',810,435,370,215,RED)
d.cls('nav','«interface» UserDashboardNavigator','callback per Client / Salesman / Manager','+ showClientDashboard(client)<br>+ showSalesmanDashboard(salesman)<br>+ showManagerDashboard(manager)',20,40,w=330,h=155,parent='domain')
d.edge('app','nav','crea implementazione anonima')
d.group('svc','service / AppContext',20,695,1160,65,YELLOW)
d.box('services','Viste → servizi concreti iniettati; overload di comodo → AppContext.getInstance(). Nessun gateway intermedio.',35,30,1120,30,'strokeColor=none;fillColor=none;',parent='svc')
d.edge('presentation','svc','invoca')
d.note('note','User.openDashboard(navigator) → callback specifica del ruolo → App costruisce la dashboard (double dispatch). App non realizza direttamente il navigator.<br>AccountManagementDialog costruisce uno Stage modale; non estende Dialog. AsyncLoad riguarda le letture, non tutte le scritture.<br>Nessun WelcomeController, FXML, Route, UiGateway o SessionContext. Freccia aperta tratteggiata: uso / costruzione. Dettagli CL-01/02/10.',20,780,1160,60)
d.save('classi-presentazione-insieme')

# Sequenze: linea temporale, attori e partecipanti, divisori e palette originali.
def sequence(name,title,parts,events,notes):
    h=150+len(events)*40+100;w=1450
    d=Drawing(title,w,h);xs={id:70+i*(w-140)/(len(parts)-1) for i,(id,_,_) in enumerate(parts)}
    for id,label,color in parts:
        x=xs[id]
        if color=='#ffffff':
            d.box(id,label,x-20,40,40,55,'shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;fillColor=#ffffff;fontSize=14;')
        else:
            d.box(id,label,x-65,55,130,55,'rounded=0;fillColor='+color+';fontSize=14;')
        c=E.SubElement(d.root,'mxCell',id='life-'+id,parent='1',edge='1',style='html=1;dashed=1;strokeColor=#89939F;endArrow=none;')
        g=E.SubElement(c,'mxGeometry',relative='1',**{'as':'geometry'});E.SubElement(g,'mxPoint',x=str(x),y='110',**{'as':'sourcePoint'});E.SubElement(g,'mxPoint',x=str(x),y=str(h-105),**{'as':'targetPoint'})
    for i,event in enumerate(events):
        y=140+i*40
        if len(event)==1:
            d.box('phase'+str(i),event[0],20,y-14,w-40,30,'fillColor=#EAF2F8;strokeColor=#517A95;fontStyle=1;align=center;');continue
        a,b,label=event
        if a==b:
            d.note('m'+str(i),label,xs[a]-135,y-14,270,32);continue
        c=E.SubElement(d.root,'mxCell',id='msg'+str(i),value=label,parent='1',edge='1',style='html=1;fontFamily=Arial;fontSize=14;labelBackgroundColor=#ffffff;endArrow=open;endFill=0;strokeColor=#526174;')
        g=E.SubElement(c,'mxGeometry',relative='1',**{'as':'geometry'});E.SubElement(g,'mxPoint',x=str(xs[a]),y=str(y),**{'as':'sourcePoint'});E.SubElement(g,'mxPoint',x=str(xs[b]),y=str(y),**{'as':'targetPoint'})
    d.note('notes',notes,20,h-90,w-40,65)
    for cell in d.root:
        st=cell.get('style','')
        cell.set('style',st.replace('fontSize=14;','fontSize=16;').replace('fontSize=12;','fontSize=14;'))
    d.save(name)
sequence('sequenza-vendita-insieme','Vendita al cliente — acconto, autorizzazione e saldo',
 [('client','Cliente','#ffffff'),('seller','Venditore','#ffffff'),('ui','Viste JavaFX',GREEN),('svc','PurchaseService',YELLOW),('db','DatabaseManager',YELLOW),('dao','DAO PostgreSQL',BLUE),('price','Calcolatore',YELLOW),('account','BankAccount',RED),('ob','Publisher / Observer',RED)], [
 ('Acconto — T1: una nuova transazione',),('client','ui','seleziona veicolo e conferma'),('ui','svc','initiatePurchase(vehicleId,clientId)'),('svc','db','executeInTransactionWithResult(callback)'),('svc','dao','SALE → BLOCKED condizionale; rileggi Vehicle (c1)'),('svc','dao','modello e sconti: letture autonome, fuori da c1'),('svc','price','prezzo effettivo e acconto 20%'),('svc','dao','conto FOR UPDATE (c1)'),('svc','account','withdraw(acconto)'),('svc','dao','saldo; Request SALE con agreed_total (c1)'),('db','dao','commit c1'),
 ('Autorizzazione — T2: sessione del venditore',),('seller','ui','seleziona richiesta SALE e approva'),('ui','svc','approvePurchase(requestId,salesmanId)'),('svc','db','executeInTransaction(callback)'),('svc','dao','Request FOR UPDATE; tipo SALE; venditore + un mese (c2)'),('db','dao','commit c2'),
 ('Saldo — T3: successiva sessione del cliente',),('client','ui','conferma pagamento residuo'),('ui','svc','completePurchase(requestId)'),('svc','db','executeInTransaction(callback)'),('svc','dao','Request FOR UPDATE; tipo, approvazione e scadenza (c3)'),('svc','account','withdraw(agreed_total − deposit); conto bloccato (c3)'),('svc','dao','saldo; Sale SALE intera; elimina Request; BLOCKED → SOLD (c3)'),('svc','ob','publishCommitment(event,c3): cancella tutti i test drive futuri'),('ob','dao','deleteFutureByVehicleId(...,c3)'),('db','dao','commit c3; solo dopo ritorno normale alla vista')],
 'Ogni callback ha Connection propria; DatabaseManager apre, disabilita autocommit e tenta commit / rollback. Le chiamate DAO nelle callback partono dal servizio, il commit dal gestore.<br>Errore: rollback della sola fase corrente, non degli episodi già confermati. Rifiuto: rimborso integrale; scadenza: penale 20% dell’acconto (dettagli SQ-01b/d). Nessun nuovo listino al saldo.')
sequence('sequenza-noleggio-insieme','Prenotazione di un noleggio',
 [('client','Cliente','#ffffff'),('ui','ClientDashboard<br>View',GREEN),('svc','RentalService',YELLOW),('db','DatabaseManager',YELLOW),('dao','DAO PostgreSQL',BLUE),('price','Calcolatore',YELLOW),('account','BankAccount',RED),('ob','Publisher / Observer',RED)], [
 ('Preventivo separato — non congela il prezzo',),('client','ui','sceglie veicolo e periodo incluso'),('ui','svc','calculateRentalCost(...) per il dialogo'),
 ('Conferma — T5: una transazione di scrittura',),('client','ui','conferma prenotazione'),('ui','svc','bookRental(vehicleId,clientId,start,end)'),('svc','svc','valida inizio non passato e fine ≥ inizio'),('svc','db','executeInTransactionWithResult(callback)'),('svc','dao','Vehicle FOR UPDATE; richiede RENT (c5)'),('svc','dao','lockClient: FOR NO KEY UPDATE (c5)'),('svc','dao','isVehicleAvailableForRent: estremi inclusi (c5)'),('svc','dao','modello e sconti riletti sulla stessa c5'),('svc','price','calculateTotalRentalCost: formula geometrica'),('svc','dao','BankAccount FOR UPDATE (c5)'),('svc','account','withdraw(costo intero)'),('svc','dao','aggiorna saldo; salva Rent (c5); stato resta RENT'),('svc','ob','publishCommitment(event,c5)'),('ob','dao','cancella tutti i test drive futuri (c5)'),('db','dao','commit c5'),('svc','ui','Rent salvato; aggiorna operazioni e saldo')],
 'Disponibilità applicativa + EXCLUDE gist su intervalli inclusi dello stesso veicolo. Conflitto, saldo insufficiente o errore Observer: rollback anche dell’addebito.<br>Il publisher invoca sincronicamente l’Observer sulla stessa connessione; cancellazioni anche fuori dal periodo noleggiato. Dettagli SQ-02/SQ-OB; nessuna Strategy o coda eventi.')
sequence('sequenza-cessione-insieme','Acquisto dal cliente — proposta ed esito',
 [('client','Cliente','#ffffff'),('seller','Venditore','#ffffff'),('ui','Viste JavaFX',GREEN),('svc','SaleToDealership<br>Service',YELLOW),('db','DatabaseManager',YELLOW),('dao','DAO PostgreSQL',BLUE),('price','Calcolatore',YELLOW),('account','BankAccount',RED)], [
 ('Proposta — T6: nessun pagamento',),('client','ui','invia dati del veicolo; preventivo facoltativo'),('ui','svc','submitSaleProposal(clientId,modelId,VIN,...)'),('svc','db','executeInTransactionWithResult(callback)'),('svc','dao','cerca VIN FOR UPDATE (c6): lock solo se già esiste'),('svc','dao','se noto: SOLD, ultima SALE al cliente, nessuna Request (c6)'),('svc','dao','VIN nuovo: crea Vehicle WAITING; noto: aggiorna WAITING (c6)'),('svc','dao','modello e sconto modello: letture autonome'),('svc','price','estimateVehicleValue(...) ricalcola stima'),('svc','dao','salva Request PURCHASE; deposit=stima, agreed_total=NULL (c6)'),('db','dao','commit c6'),
 ('Accettazione — T7: sessione del venditore',),('seller','ui','seleziona PURCHASE e approva'),('ui','svc','approveSaleProposal(requestId,salesmanId)'),('svc','db','executeInTransaction(callback)'),('svc','dao','Request e Vehicle FOR UPDATE: PURCHASE / WAITING (c7)'),('svc','svc','richiede salesmanId non nullo'),('svc','dao','BankAccount FOR UPDATE (c7)'),('svc','account','deposit(stima persistita)'),('svc','dao','aggiorna saldo; Sale PURCHASE; WAITING → SALE; elimina Request (c7)'),('db','dao','commit c7'),('svc','ui','esito; ricarica richieste e inventario'),
 ('Alternativa: rifiuto in una transazione distinta da un’accettazione',),('ui','svc','rejectSaleProposal(requestId)'),('svc','dao','blocca e ricontrolla; elimina Request; storico → SOLD, altrimenti delete Vehicle')],
 'DatabaseManager coordina una sola connessione per ciascun esito; errore → rollback dell’esito. Rifiuto: nessun accredito e storico conservato; commit prima del ritorno alla vista.<br>Nessuna UnitOfWork annidata, nessun venditore costante, nessun Observer. Stima zero persistibile ma accredito zero rifiutato dal conto: rollback. Dettagli SQ-03a/b.')

# Controllo minimo ripetibile della struttura nativa, compresi riferimenti degli archi.
assert len(list(OUT.glob('*-insieme.drawio')))==7
print('11 tavole riadattate; 7 ricostruzioni UML e 4 basi degli autori conservate.')
