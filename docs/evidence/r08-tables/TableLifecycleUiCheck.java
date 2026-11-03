package it.unifi.drivehub;

import it.unifi.drivehub.domain.user.*;
import it.unifi.drivehub.domain.vehicle.*;
import it.unifi.drivehub.presentation.view.*;
import it.unifi.drivehub.service.CatalogService.VehicleRow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import java.math.BigDecimal;
import java.time.Year;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Caricamenti reali, callback osservabili, tastiera e dati aggiornati nelle viste pubbliche. */
public class TableLifecycleUiCheck extends ReactiveTablesUiCheck {
    static Button refresh(Parent view,String name) throws Exception {
        return fx(() -> (Button)view.lookupAll(".button").stream().filter(n -> ("Aggiorna o riprova: "+name).equals(n.getAccessibleText())).findFirst().orElseThrow());
    }
    static Label status(Button button) throws Exception {
        return fx(() -> (Label)((HBox)button.getParent()).getChildren().stream().filter(n->n instanceof Label).findFirst().orElseThrow());
    }
    static void text(Label label,String expected) throws Exception {
        CompletableFuture<Void> done=new CompletableFuture<>();
        ChangeListener<String> listener=(obs,old,value)->{if(expected.equals(value))done.complete(null);};
        fx(() -> {label.textProperty().addListener(listener);if(expected.equals(label.getText()))done.complete(null);return null;});
        try {done.get(15,TimeUnit.SECONDS);}finally{fx(() -> {label.textProperty().removeListener(listener);return null;});}
    }
    static TableView<?> table(TabPane tabs,int tab) throws Exception {
        return fx(() -> (TableView<?>)tabs.getTabs().get(tab).getContent().lookup(".table-view"));
    }
    static void allLoaded(Parent view) throws Exception {
        for(Node node:fx(()->view.lookupAll(".table-view")))loaded((TableView<?>)node);
    }
    static String value(TableView<?> table,int row,String column) throws Exception {
        return fx(() -> {
            var col=table.getColumns().stream().filter(c->c.getText().equals(column)).findFirst().orElseThrow();
            return String.valueOf(col.getCellObservableValue(row).getValue());
        });
    }
    static void expected(TableView<?> table,int row,String column,String expected) throws Exception {
        check(value(table,row,column).equals(expected),column+" = "+expected);
    }
    static void sql(String statement) throws Exception {
        try(var conn=ctx.getDatabaseManager().getConnection();var sql=conn.createStatement()){sql.setQueryTimeout(5);sql.execute(statement);}
    }
    static void screenshotReady(String name) throws Exception {frames();screenshot(name);}

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t,e)->{e.printStackTrace();System.exit(2);});
        try {
            out=args[1]+"/";
            ctx=new AppContext(TestDatabase.fromEnvironment().reset());
            sql("DELETE FROM sale;DELETE FROM rent;DELETE FROM test_drive;DELETE FROM request;DELETE FROM vehicle_discount;DELETE FROM vehicle;DELETE FROM model_discount;");
            int year=Year.now().getValue();
            var model=ctx.getCatalogService().addModel("Fiat","UI14",new BigDecimal("20000"),year);
            ctx.getCatalogService().setModelDiscount(model.getId(),10);
            var auto=ctx.getCatalogService().addVehicle(model.getId(),"T1400000000000100",null,0,null,year,VehicleState.SALE);
            var manual=ctx.getCatalogService().addVehicle(model.getId(),"T1400000000000101",null,0,new BigDecimal("10000"),year,VehicleState.SALE);
            ctx.getCatalogService().addVehicle(model.getId(),"T1400000000000102",null,0,new BigDecimal("10000"),year,VehicleState.RENT);
            for(int i=0;i<30;i++)ctx.getCatalogService().addVehicle(model.getId(),String.format("T14000000000002%02d",i),null,0,null,year,VehicleState.SALE);
            Manager manager=(Manager)ctx.getAuthService().login("vittorio.manager@drivehub.it","password");
            Client client=(Client)ctx.getAuthService().login("mario.rossi@email.it","password");
            Salesman salesman=(Salesman)ctx.getAuthService().login("paolo.costa@drivehub.it","password");
            probe();
            int beforeConnections=connections.get(),beforeQueries=queries.get();
            ctx.getCatalogService().getVehicleRows(null);
            check(connections.get()-beforeConnections==4 && queries.get()-beforeQueries==4,"listino intero: quattro query/connessioni indipendenti dal numero di righe");
            CountDownLatch ready=new CountDownLatch(1);Platform.startup(ready::countDown);check(ready.await(10,TimeUnit.SECONDS),"JavaFX avviato");
            fx(()->{Platform.setImplicitExit(false);stage=new javafx.stage.Stage();robot=new javafx.scene.robot.Robot();return null;});
            var managerView=fx(()->new ManagerDashboardView(manager,ctx.getAuthService(),ctx.getCatalogService(),ctx.getOperationQueryService(),()->{}));
            allLoaded(managerView);show(managerView);
            TabPane tabs=fx(()->(TabPane)managerView.getCenter());
            TableView<?> vehicles=table(tabs,2);
            fx(()->{tabs.requestFocus();return null;});key(KeyCode.HOME);key(KeyCode.RIGHT);key(KeyCode.RIGHT);
            check(fx(()->tabs.getSelectionModel().getSelectedIndex()==2),"scheda veicoli raggiunta con frecce");
            fx(()->{vehicles.requestFocus();return null;});key(KeyCode.HOME);key(KeyCode.DOWN);
            check(fx(()->vehicles.getSelectionModel().getSelectedIndex()==1),"selezione veicolo con tastiera");
            expected(vehicles,0,"Prezzo effettivo","18.000,00\u00a0€");
            expected(vehicles,1,"Prezzo effettivo","9.000,00\u00a0€");
            reach(n->n instanceof TextField && "Base manuale in euro, prima di sconti e svalutazioni".equals(n.getAccessibleText()));
            fx(()->{robot.keyPress(KeyCode.CONTROL);robot.keyType(KeyCode.A);robot.keyRelease(KeyCode.CONTROL);return null;});
            for(var key:new KeyCode[]{KeyCode.DIGIT1,KeyCode.DIGIT2,KeyCode.DIGIT0,KeyCode.DIGIT0,KeyCode.DIGIT0})key(key);
            reach(n->n instanceof Button b && b.getText().equals("Applica base alla selezione"));key(KeyCode.SPACE);closeAlert();loaded(vehicles);
            expected(vehicles,1,"Base applicata","12.000,00\u00a0€");expected(vehicles,1,"Prezzo effettivo","10.800,00\u00a0€");
            expected(vehicles,1,"Prezzo consigliato","18.000,00\u00a0€");
            screenshotReady("manager-prices");
            Button reload=refresh(managerView,"veicoli");Label state=status(reload);
            AtomicBoolean fxUpdates=new AtomicBoolean(true);
            fx(()->{vehicles.getItems().addListener((javafx.collections.ListChangeListener<Object>)c->{if(!Platform.isFxApplicationThread())fxUpdates.set(false);});return null;});
            // Una query veramente bloccata non deve impedire frame o uso da tastiera.
            try(var gate=ctx.getDatabaseManager().getConnection()) {
                gate.setAutoCommit(false);int pid;
                try(var q=gate.createStatement();var rs=q.executeQuery("SELECT pg_backend_pid()")){rs.next();pid=rs.getInt(1);}
                try(var q=gate.createStatement()){q.execute("LOCK TABLE model IN ACCESS EXCLUSIVE MODE");}
                fx(()->{reload.fire();return null;});
                long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);boolean observed=false;
                try(var observer=ctx.getDatabaseManager().getConnection();var q=observer.prepareStatement("SELECT pid FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid))")) {
                    q.setInt(1,pid);q.setQueryTimeout(2);
                    while(!observed && System.nanoTime()<deadline)try(var rs=q.executeQuery()){if(rs.next()){observed=true;System.out.println("OBSERVED slow catalog PID="+rs.getInt(1)+" blocker="+pid);}}
                }
                check(observed,"caricamento catalogo in attesa osservabile");
                check(fx(()->state.getText().equals("Caricamento in corso…") && vehicles.isDisabled()),"stato caricamento e dati temporaneamente non azionabili");
                fx(()->{tabs.requestFocus();return null;});key(KeyCode.RIGHT);check(fx(()->tabs.getSelectionModel().getSelectedIndex()==3),"tastiera reattiva durante query bloccata");
                fx(()->{tabs.getSelectionModel().select(2);return null;});screenshotReady("loading");gate.rollback();
            }
            text(state,"Aggiornato");
            sql("ALTER TABLE vehicle RENAME TO vehicle_unavailable14");
            try {
                fx(()->{reload.fire();return null;});text(state,"Caricamento non riuscito. Riprova.");
                check(fx(()->reload.getText().equals("Riprova") && vehicles.isDisabled()),"errore visibile, riprova disponibile e vecchi dati non azionabili");screenshotReady("retry");
            }finally{sql("ALTER TABLE vehicle_unavailable14 RENAME TO vehicle");}
            fx(()->{reload.requestFocus();return null;});key(KeyCode.SPACE);text(state,"Aggiornato");check(!fx(vehicles::isDisabled),"riprova da tastiera riuscita");
            for(boolean staleError:new boolean[]{false,true}) {
                QueryHold old=new QueryHold(staleError);hold.set(old);
                fx(()->{reload.fire();return null;});check(old.observed.await(10,TimeUnit.SECONDS),"vecchia query reale acquisita prima della seconda richiesta");
                try {
                    ctx.getCatalogService().updateVehiclePricing(manual.getId(),new BigDecimal(staleError?"14000":"13000"));
                    fx(()->{reload.fire();return null;});text(state,"Aggiornato");
                    expected(vehicles,1,"Prezzo effettivo",staleError?"12.600,00\u00a0€":"11.700,00\u00a0€");
                }finally{old.release.countDown();}
                check(old.closed.await(10,TimeUnit.SECONDS),"vecchia lettura terminata e connessione chiusa");frames();
                expected(vehicles,1,"Prezzo effettivo",staleError?"12.600,00\u00a0€":"11.700,00\u00a0€");
                check(fx(()->state.getText().equals("Aggiornato")),"risultato/errore obsoleto ignorato");
            }
            check(fxUpdates.get(),"aggiornamenti osservati sul thread JavaFX");
            // Modifica sconto dalla UI: la tabella veicoli deve aggiornarsi automaticamente.
            fx(()->{tabs.getSelectionModel().select(3);return null;});
            TextField discount=fx(()->(TextField)tabs.getTabs().get(3).getContent().lookupAll(".text-field").stream().filter(n->"% (0-50)".equals(((TextField)n).getPromptText())).findFirst().orElseThrow());
            ComboBox<?> models=fx(()->(ComboBox<?>)tabs.getTabs().get(3).getContent().lookup(".combo-box"));
            fx(()->{for(int i=0;i<models.getItems().size();i++)if(((Model)models.getItems().get(i)).getId().equals(model.getId()))models.getSelectionModel().select(i);discount.setText("20");return null;});
            Button setDiscount=fx(()->(Button)tabs.getTabs().get(3).getContent().lookupAll(".button").stream().filter(n->((Button)n).getText().equals("Salva")).findFirst().orElseThrow());
            Platform.runLater(setDiscount::fire);closeAlert();text(state,"Aggiornato");
            expected(vehicles,0,"Prezzo effettivo","16.000,00\u00a0€");expected(vehicles,1,"Prezzo effettivo","11.200,00\u00a0€");
            allLoaded(managerView);
            for(int i=0;i<5;i++){final int tab=i;fx(()->{tabs.getSelectionModel().select(tab);return null;});frames();for(Node n:fx(()->tabs.getTabs().get(tab).getContent().lookupAll(".table-view")))cells((TableView<?>)n);}
            var clientView=fx(()->new ClientDashboardView(client,ctx.getAuthService(),ctx.getPurchaseService(),ctx.getSaleToDealershipService(),ctx.getRentalService(),ctx.getTestDriveService(),ctx.getCatalogService(),ctx.getOperationQueryService(),ctx.getPriceCalculatorService(),()->{}));
            allLoaded(clientView);show(clientView);TabPane clientTabs=fx(()->(TabPane)clientView.getCenter());
            var sale=table(clientTabs,0);expected(sale,0,"Prezzo effettivo","16.000,00\u00a0€");expected(sale,1,"Prezzo effettivo","11.200,00\u00a0€");cells(sale);fx(()->{sale.scrollTo(0);return null;});screenshotReady("client-sale");
            fx(()->{clientTabs.requestFocus();return null;});key(KeyCode.HOME);key(KeyCode.RIGHT);
            var rent=table(clientTabs,1);expected(rent,0,"Prezzo effettivo","8.000,00\u00a0€");expected(rent,0,"Tariffa iniziale/giorno","8,00\u00a0€/giorno");cells(rent);screenshotReady("client-rent");
            ctx.getCatalogService().updateVehiclePricing(manual.getId(),null);
            Button clientRefresh=refresh(clientView,"catalogo vendita");
            fx(()->{clientTabs.getSelectionModel().select(0);clientRefresh.requestFocus();return null;});key(KeyCode.SPACE);text(status(clientRefresh),"Aggiornato");
            expected(sale,1,"Base applicata","20.000,00\u00a0€");expected(sale,1,"Prezzo effettivo","16.000,00\u00a0€");
            sql("UPDATE model SET release_price=30000,name='UI14 nuovo' WHERE id="+model.getId());
            fx(()->{clientRefresh.fire();return null;});text(status(clientRefresh),"Aggiornato");
            expected(sale,0,"Prezzo effettivo","24.000,00\u00a0€");expected(sale,1,"Prezzo effettivo","24.000,00\u00a0€");
            check(value(sale,0,"Modello").contains("UI14 nuovo"),"descrizione e base modello aggiornate nella vista già aperta");cells(sale);fx(()->{sale.scrollTo(0);return null;});screenshotReady("client-refreshed");
            var salesView=fx(()->new SalesmanDashboardView(salesman,ctx.getAuthService(),ctx.getPurchaseService(),ctx.getSaleToDealershipService(),ctx.getRentalService(),ctx.getCatalogService(),ctx.getOperationQueryService(),()->{}));
            allLoaded(salesView);show(salesView);TabPane salesTabs=fx(()->(TabPane)salesView.getCenter());fx(()->{salesTabs.getSelectionModel().select(1);return null;});cells(table(salesTabs,1));screenshotReady("salesman-inventory");
            System.out.println("UI CHECK SUCCESS");
        }catch(Throwable error){error.printStackTrace();System.exit(1);}
        Platform.exit();
    }
}
