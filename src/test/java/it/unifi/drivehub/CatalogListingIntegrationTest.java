package it.unifi.drivehub;

import it.unifi.drivehub.domain.vehicle.*;
import it.unifi.drivehub.service.CatalogService;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.Year;
import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class CatalogListingIntegrationTest {
    private AppContext ctx;
    private Model model;
    private Vehicle vehicle;

    @BeforeEach void setUp() throws Exception {
        ctx = new AppContext(TestDatabase.fromEnvironment().reset());
        model = ctx.getCatalogService().addModel("Fiat", "Listino14", new BigDecimal("20000"), Year.now().getValue()-2);
        ctx.getCatalogService().setModelDiscount(model.getId(), 10);
        vehicle = ctx.getCatalogService().addVehicle(model.getId(), "T1400000000000001", null, 0,
                null, Year.now().getValue(), VehicleState.SALE);
    }
    CatalogService.VehicleRow row() {
        return ctx.getCatalogService().getVehicleRows(VehicleState.SALE).stream()
                .filter(row -> row.vehicle().getId().equals(vehicle.getId())).findFirst().orElseThrow();
    }
    @Test void automaticAndManualUseTheSameFormulaAfterRefresh() {
        assertEquals(new BigDecimal("18000.00"), row().effective());
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), new BigDecimal("10000"));
        var manual=row();
        assertEquals(PriceMode.MANUAL, manual.vehicle().getPriceMode());
        assertEquals(new BigDecimal("10000.00"), manual.base());
        assertEquals(new BigDecimal("18000.00"), manual.recommended());
        assertEquals(new BigDecimal("9000.00"), manual.effective());
        ctx.getCatalogService().setVehicleDiscount(vehicle.getId(), 20);
        assertEquals(new BigDecimal("8000.00"), row().effective());
        assertEquals(new BigDecimal("16000.00"), row().recommended());
        ctx.getCatalogService().removeVehicleDiscount(vehicle.getId());
        ctx.getCatalogService().removeModelDiscount(model.getId());
        assertEquals(new BigDecimal("10000.00"), row().effective());
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), null);
        assertEquals(PriceMode.AUTOMATIC, row().vehicle().getPriceMode());
        assertEquals(new BigDecimal("20000.00"), row().effective());
    }
    @Test void nextRefreshReadsNewModelBaseAndNameWithoutChangingManualBase() throws Exception {
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), new BigDecimal("10000.005"));
        var before=row();
        try(var conn=ctx.getDatabaseManager().getConnection();var sql=conn.prepareStatement("UPDATE model SET release_price=30000,name='Listino nuovo' WHERE id=?")) {
            sql.setLong(1,model.getId());assertEquals(1,sql.executeUpdate());
        }
        var after=row();
        assertEquals("Fiat Listino nuovo",after.modelName());
        assertEquals(new BigDecimal("9000.01"),after.effective());
        assertEquals(new BigDecimal("27000.00"),after.recommended());
        assertEquals("Fiat Listino14",before.modelName());
        assertEquals(new BigDecimal("18000.00"),before.recommended());
        assertTrue(ctx.getCatalogService().getModelDiscountRows().stream().anyMatch(r -> r.modelName().equals("Fiat Listino nuovo")));
    }
    @Test void stateFilteringEmptyResultsAndNewSessionRemainCorrect() {
        assertFalse(ctx.getCatalogService().getVehicleRows(VehicleState.RENT).stream().anyMatch(r -> r.vehicle().getId().equals(vehicle.getId())));
        var fresh=new AppContext(ctx.getDatabaseManager()).getCatalogService().getVehicleRows(null).stream()
                .filter(r->r.vehicle().getId().equals(vehicle.getId())).findFirst().orElseThrow();
        assertEquals("Fiat Listino14",fresh.modelName());
        assertEquals(new BigDecimal("18000.00"),fresh.effective());
        assertTrue(ctx.getCatalogService().getVehicleRows(VehicleState.WAITING).stream().noneMatch(r->r.vehicle().getId().equals(vehicle.getId())));
    }
    @Test void usedVehicleAndDailyRateKeepDepreciationAndMaximumDiscount() {
        Vehicle used=ctx.getCatalogService().addVehicle(model.getId(),"T1400000000000002","AB123CD",20000,
                new BigDecimal("10000"),Year.now().getValue()-1,VehicleState.RENT);
        ctx.getCatalogService().setVehicleDiscount(used.getId(),20);
        var row=ctx.getCatalogService().getVehicleRows(VehicleState.RENT).stream().filter(r->r.vehicle().getId().equals(used.getId())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("5776.00"),row.effective());
        assertEquals(new BigDecimal("11552.00"),row.recommended());
        assertEquals(new BigDecimal("5.78"),row.dailyRate());
    }
}
