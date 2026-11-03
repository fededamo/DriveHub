package it.unifi.drivehub;

import it.unifi.drivehub.dao.DatabaseManager;
import it.unifi.drivehub.domain.operation.Request;
import it.unifi.drivehub.domain.operation.Sale;
import it.unifi.drivehub.domain.operation.SaleType;
import it.unifi.drivehub.domain.user.*;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import it.unifi.drivehub.presentation.view.*;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.Year;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Controllo delle viste pubbliche con Robot e PostgreSQL dedicato; riusa il ticket 06. */
public class SaleOutcomeUiCheck extends AgreementUiCheck {
    static final String APPROVE = "T1000000000000091";
    static final String REJECT_NEW = "T1000000000000092";
    static final String REJECT_KNOWN = "T1000000000000093";

    static void seed() throws Exception {
        ctx = new AppContext(TestDatabase.fromEnvironment().reset());
        try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
            sql.execute("DELETE FROM sale; DELETE FROM rent; DELETE FROM test_drive; DELETE FROM request;"
                    + "DELETE FROM vehicle_discount; DELETE FROM vehicle; UPDATE bank_account SET balance=50000 WHERE client=1");
        }
        int year = Year.now().getValue();
        var model = ctx.getCatalogService().addModel("Fiat", "UI Outcome10", new BigDecimal("20000"), year);
        var known = ctx.getCatalogService().addVehicle(model.getId(), REJECT_KNOWN, null, 0, null, year, VehicleState.SALE);
        var purchase = ctx.getPurchaseService().initiatePurchase(known.getId(), 1L);
        ctx.getPurchaseService().approvePurchase(purchase.getId(), 16L);
        ctx.getPurchaseService().completePurchase(purchase.getId());
        for (String vin : new String[]{APPROVE, REJECT_NEW, REJECT_KNOWN}) {
            String plate = vin.equals(APPROVE) ? "AB123CD" : vin.equals(REJECT_NEW) ? "EF456GH" : "MN789PQ";
            ctx.getSaleToDealershipService().submitSaleProposal(1L, model.getId(), vin, plate, 0, year);
        }
        try (var conn = ctx.getDatabaseManager().getConnection(); var sql = conn.createStatement()) {
            sql.execute("UPDATE bank_account SET balance=1000 WHERE client=1;"
                    + "INSERT INTO sale(vehicle,salesman,client,amount,date,type) VALUES"
                    + "(NULL,NULL,2,0,CURRENT_DATE-2,'ACQUISITION'),"
                    + "(NULL,NULL,2,0,CURRENT_DATE-1,'PURCHASE'),"
                    + "(NULL,NULL,2,800,CURRENT_DATE,'PENALTY')");
        }
        System.out.println("SEED SUCCESS: tre cessioni 16000, storico effettivo, tipi espliciti anche a zero");
    }

    static SalesmanDashboardView seller(Salesman user) {
        return new SalesmanDashboardView(user, ctx.getAuthService(), ctx.getPurchaseService(),
                ctx.getSaleToDealershipService(), ctx.getRentalService(), ctx.getCatalogService(), ctx.getOperationQueryService(), () -> {});
    }

    static long vehicle(String vin) {
        return ctx.getCatalogService().getAllVehicles().stream().filter(v -> v.getVin().equals(vin)).findFirst().orElseThrow().getId();
    }

    static void selectProposal(String vin) throws Exception {
        long id = vehicle(vin);
        var table = fx(() -> requests(stage.getScene().getRoot()));
        int index = fx(() -> {
            for (int i=0; i<table.getItems().size(); i++) if (table.getItems().get(i).getVehicleId().equals(id)) return i;
            throw new AssertionError("Proposta assente " + vin);
        });
        focusTable(table);
        for (int i=0; i<index; i++) key(KeyCode.DOWN);
        check(fx(() -> table.getSelectionModel().getSelectedItem().getVehicleId()).equals(id), "cessione selezionata con frecce " + vin);
    }

    static void press(String label) throws Exception {
        reach(n -> n instanceof Button b && b.getText().equals(label));
        key(KeyCode.SPACE);
    }

    @SuppressWarnings("unchecked")
    static TableView<Sale> ledger(Parent root) {
        return (TableView<Sale>) root.lookupAll(".table-view").stream().filter(n -> ((TableView<?>) n).getColumns().stream()
                .anyMatch(c -> c.getText().equals("Importo concessionaria"))).findFirst().orElseThrow();
    }

    static String saleCell(TableView<Sale> table, Sale sale, String column) {
        return table.getColumns().stream().filter(c -> c.getText().equals(column)).findFirst().orElseThrow().getCellData(sale).toString();
    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("seed")) { seed(); return; }
        out = args[1] + "/";
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> { error.printStackTrace(); System.exit(2); });
        TestDatabase.fromEnvironment();
        ctx = new AppContext(new DatabaseManager(System.getenv("DRIVEHUB_TEST_DB_URL"),
                System.getenv("DRIVEHUB_TEST_DB_USER"), System.getenv("DRIVEHUB_TEST_DB_PASSWORD")));
        var ready = new CountDownLatch(1);
        Platform.startup(ready::countDown);
        check(ready.await(10, TimeUnit.SECONDS), "JavaFX avviato nella seconda JVM");
        Platform.setImplicitExit(false);
        try {
            fx(() -> { stage = new Stage(); robot = new Robot(); return null; });
            var user = (Salesman) ctx.getAuthService().login("paolo.costa@drivehub.it", "password");
            var stale = fx(() -> seller(user));
            show(fx(() -> seller(user)));
            selectProposal(APPROVE);
            press("Approva Richiesta");
            check(dialog().contains("pagamento accreditato al cliente"), "messaggio di accredito positivo");
            closeAlert(); show(fx(() -> seller(user))); screenshot("seller-approved");
            check(ctx.getAuthService().getClientBankAccount(1L).getBalance().equals(new BigDecimal("17000.00")), "conto cliente +16000");
            show(stale); selectProposal(APPROVE); press("Approva Richiesta");
            check(dialog().contains("rollback"), "sessione obsoleta segnala errore senza secondo pagamento");
            screenshot("seller-stale"); closeAlert();
            check(ctx.getAuthService().getClientBankAccount(1L).getBalance().equals(new BigDecimal("17000.00")), "nessun doppio accredito UI");

            show(fx(() -> seller(user))); selectProposal(REJECT_NEW); press("Rifiuta Richiesta");
            check(dialog().contains("senza pagamento"), "messaggio rifiuto senza accredito"); closeAlert();
            check(ctx.getCatalogService().getAllVehicles().stream().noneMatch(v -> v.getVin().equals(REJECT_NEW)), "VIN nuovo rimosso dal catalogo UI");
            show(fx(() -> seller(user))); selectProposal(REJECT_KNOWN); press("Rifiuta Richiesta");
            check(dialog().contains("storico del veicolo è conservato"), "messaggio rifiuto con storico");
            screenshot("seller-rejected"); closeAlert();
            check(ctx.getCatalogService().getVehicleById(vehicle(REJECT_KNOWN)).orElseThrow().getState()==VehicleState.SOLD, "VIN storico ripristinato SOLD");
            check(ctx.getOperationQueryService().getClientSales(1L).size()==2, "vendita e acquisizione conservate");

            var client = (Client) ctx.getAuthService().login("mario.rossi@email.it", "password");
            show(fx(() -> new ClientDashboardView(client, ctx.getAuthService(), ctx.getPurchaseService(), ctx.getSaleToDealershipService(),
                    ctx.getRentalService(), ctx.getTestDriveService(), ctx.getCatalogService(), ctx.getOperationQueryService(), ctx.getPriceCalculatorService(), () -> {})));
            check(fx(() -> ((Label)stage.getScene().getRoot().lookup(".balance-badge")).getText()).contains(
                    UiUtils.formatCurrency(new BigDecimal("17000"))), "cliente vede il saldo accreditato");
            screenshot("client-balance");

            var manager = (Manager) ctx.getAuthService().login("vittorio.manager@drivehub.it", "password");
            show(fx(() -> new ManagerDashboardView(manager, ctx.getAuthService(), ctx.getCatalogService(), ctx.getOperationQueryService(), () -> {})));
            TabPane tabs = fx(() -> (TabPane)((ManagerDashboardView)stage.getScene().getRoot()).getCenter());
            fx(() -> { tabs.requestFocus(); return null; });
            for (int i=0; i<tabs.getTabs().size()-1; i++) key(KeyCode.RIGHT);
            check(fx(() -> tabs.getSelectionModel().getSelectedItem().getText()).equals("Registro economico"), "registro raggiunto con frecce");
            var table = fx(() -> ledger(stage.getScene().getRoot()));
            fx(() -> {
                check(table.getItems().size()==5, "registro riletto con tutte le cinque operazioni");
                for (Sale sale:table.getItems()) {
                    String expected = switch(sale.getType()) {
                        case PURCHASE -> "VENDITA A CLIENTE";
                        case ACQUISITION -> "ACQUISIZIONE DA CLIENTE";
                        case PENALTY -> "PENALE SCADENZA (Quota trattenuta)";
                    };
                    check(saleCell(table,sale,"Tipologia").equals(expected), "tipo esplicito "+sale.getType()+" "+sale.getAmount());
                    check(saleCell(table,sale,"Importo concessionaria").equals(UiUtils.formatCurrency(sale.getAmount())), "importo del registro "+sale.getAmount());
                }
                check(table.getItems().stream().anyMatch(s -> s.getType()==SaleType.ACQUISITION && s.getAmount().equals(new BigDecimal("-16000.00"))), "acquisizione -16000");
                return null;
            });
            focusTable(table); key(KeyCode.END);
            check(fx(() -> table.getSelectionModel().getSelectedIndex())==4, "registro navigabile fino all'ultima operazione");
            screenshot("manager-ledger");
            System.out.println("UI CHECK SUCCESS");
        } finally {
            fx(() -> { stage.close(); return null; }); Platform.exit();
        }
    }
}
