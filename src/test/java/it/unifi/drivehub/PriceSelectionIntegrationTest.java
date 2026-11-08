package it.unifi.drivehub;

import it.unifi.drivehub.domain.vehicle.Model;
import it.unifi.drivehub.domain.vehicle.PriceMode;
import it.unifi.drivehub.domain.vehicle.Vehicle;
import it.unifi.drivehub.domain.vehicle.VehicleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Year;
import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@Tag("postgres")
class PriceSelectionIntegrationTest {
    private AppContext ctx;
    private Model model;
    private final int year = Year.now().getValue();

    @BeforeEach
    void setUp() throws Exception {
        ctx = new AppContext(TestDatabase.fromEnvironment().reset());
        model = ctx.getCatalogService().addModel("Fiat", "Ticket05", new BigDecimal("20000"), year);
        ctx.getCatalogService().setModelDiscount(model.getId(), 10);
    }


    @Test
    void existingCessionKeepsManagerBaseSeparateFromEstimate() {
        Vehicle vehicle = ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000002", "AB123CD", 0,
                new BigDecimal("10000"), year, VehicleState.SOLD);
        // La fixture SOLD rappresenta una vendita effettiva al proponente, conservata nello storico.
        ctx.getDatabaseManager().executeInTransaction(conn ->
                new it.unifi.drivehub.dao.postgres.PostgresSaleDao(ctx.getDatabaseManager()).save(
                        it.unifi.drivehub.domain.operation.Sale.createPurchaseRecord(
                                vehicle.getId(), 16L, 1L, new BigDecimal("7200.00")), conn));
        var request = ctx.getSaleToDealershipService().submitSaleProposal(1L, model.getId(), vehicle.getVin(), "AB123CD", 0, year);
        assertEquals(new BigDecimal("14400.00"), request.getDeposit());
        Vehicle reloaded = ctx.getCatalogService().getVehicleById(vehicle.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("10000").compareTo(reloaded.getPrice()));
        assertEquals(PriceMode.MANUAL, reloaded.getPriceMode());
        assertEquals(new BigDecimal("7200.00"), ctx.getCatalogService().getEffectiveVehiclePrice(reloaded));
    }

    @Test
    void fractionalCentBaseHasSameEffectivePriceBeforeAndAfterReload() {
        Vehicle saved = ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000008", null, 0,
                new BigDecimal("10000.005"), year, VehicleState.SALE);
        assertEquals(new BigDecimal("9000.01"), ctx.getCatalogService().getEffectiveVehiclePrice(saved));
        assertEquals(new BigDecimal("9000.01"), ctx.getCatalogService().getEffectiveVehiclePrice(reload(saved)));
        ctx.getCatalogService().updateVehiclePricing(saved.getId(), new BigDecimal("20000.005"));
        assertEquals(new BigDecimal("18000.01"), ctx.getCatalogService().getEffectiveVehiclePrice(reload(saved)));
    }

    @Test
    void catalogPersistsManualBaseAndAppliesPromotionOnce() {
        Vehicle saved = ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000001", null, 0,
                new BigDecimal("10000"), year, VehicleState.SALE);
        Vehicle reloaded = new AppContext(ctx.getDatabaseManager()).getCatalogService()
                .getVehicleById(saved.getId()).orElseThrow();
        assertEquals(new BigDecimal("9000.00"), ctx.getCatalogService().getEffectiveVehiclePrice(reloaded));
        assertEquals(PriceMode.MANUAL, saved.getPriceMode());
        assertEquals(PriceMode.MANUAL, reloaded.getPriceMode());
        assertEquals(0, new BigDecimal("10000").compareTo(reloaded.getPrice()));
    }

    @Test
    void equalManualBaseAndReturnToRecommendedSurviveReloadAndModelChange() throws Exception {
        Vehicle vehicle = ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000003", null, 0,
                null, year, VehicleState.SALE);
        assertEquals(PriceMode.AUTOMATIC, reload(vehicle).getPriceMode());
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), new BigDecimal("20000"));
        assertEquals(PriceMode.MANUAL, reload(vehicle).getPriceMode());
        assertEquals(new BigDecimal("18000.00"), ctx.getCatalogService().getEffectiveVehiclePrice(reload(vehicle)));
        // La modifica della base del modello non è esposta dal servizio: fixture nel solo DB dedicato.
        try (var conn = ctx.getDatabaseManager().getConnection();
             var ps = conn.prepareStatement("UPDATE model SET release_price = 30000 WHERE id = ?")) {
            ps.setLong(1, model.getId());
            assertEquals(1, ps.executeUpdate());
        }
        assertEquals(new BigDecimal("18000.00"), ctx.getCatalogService().getEffectiveVehiclePrice(reload(vehicle)));
        assertEquals(new BigDecimal("27000.00"), ctx.getCatalogService().getRecommendedVehiclePrice(reload(vehicle)));
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), null);
        Vehicle automatic = reload(vehicle);
        assertEquals(PriceMode.AUTOMATIC, automatic.getPriceMode());
        assertEquals(new BigDecimal("27000.00"), ctx.getCatalogService().getEffectiveVehiclePrice(automatic));
        assertEquals(new BigDecimal("30000.00"), ctx.getCatalogService().getVehicleBasePrice(automatic));
        assertEquals(PriceMode.AUTOMATIC, ctx.getCatalogService().getAllVehicles().stream()
                .filter(v -> v.getId().equals(vehicle.getId())).findFirst().orElseThrow().getPriceMode());
    }

    @Test
    void invalidBaseDoesNotChangePersistedSelection() {
        Vehicle vehicle = ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000004", null, 0,
                null, year, VehicleState.SALE);
        RuntimeException invalid = assertThrows(RuntimeException.class,
                () -> ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), new BigDecimal("-1")));
        assertInstanceOf(IllegalArgumentException.class, invalid.getCause());
        assertEquals(PriceMode.AUTOMATIC, reload(vehicle).getPriceMode());
        RuntimeException missing = assertThrows(RuntimeException.class,
                () -> ctx.getCatalogService().updateVehiclePricing(Long.MAX_VALUE, new BigDecimal("10000")));
        assertInstanceOf(IllegalArgumentException.class, missing.getCause());
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), BigDecimal.ZERO);
        assertEquals(PriceMode.MANUAL, reload(vehicle).getPriceMode());
        assertEquals(new BigDecimal("0.00"), ctx.getCatalogService().getEffectiveVehiclePrice(reload(vehicle)));
    }

    @ParameterizedTest
    @CsvSource({"10000, 9000.00, 1800.00", ", 18000.00, 3600.00"})
    void purchaseUsesCatalogPriceInBothModes(BigDecimal base, BigDecimal price, BigDecimal deposit) {
        Vehicle vehicle = ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000005", null, 0,
                base, year, VehicleState.SALE);
        assertEquals(price, ctx.getCatalogService().getEffectiveVehiclePrice(reload(vehicle)));
        var request = ctx.getPurchaseService().initiatePurchase(vehicle.getId(), 1L);
        assertEquals(deposit, request.getDeposit());
        ctx.getPurchaseService().approvePurchase(request.getId(), 16L);
        ctx.getPurchaseService().completePurchase(request.getId());
        var sale = ctx.getOperationQueryService().getAllSales().stream()
                .filter(s -> s.getVehicleId().equals(vehicle.getId())).findFirst().orElseThrow();
        assertEquals(price, sale.getAmount());
        assertEquals(VehicleState.SOLD, reload(vehicle).getState());
        assertEquals(base == null ? PriceMode.AUTOMATIC : PriceMode.MANUAL, reload(vehicle).getPriceMode());
    }

    @ParameterizedTest
    @CsvSource({"10000, 9.00, 25.67", ", 18.00, 51.35"})
    void rentalUsesCatalogPriceWithGeometricFormulaAndInclusiveDays(BigDecimal base, BigDecimal daily, BigDecimal total) {
        Vehicle vehicle = ctx.getCatalogService().addVehicle(model.getId(), "T0500000000000006", null, 0,
                base, year, VehicleState.RENT);
        Vehicle listed = ctx.getCatalogService().getVehiclesByState(VehicleState.RENT).stream()
                .filter(v -> v.getId().equals(vehicle.getId())).findFirst().orElseThrow();
        BigDecimal effective = ctx.getCatalogService().getEffectiveVehiclePrice(listed);
        assertEquals(daily, ctx.getPriceCalculatorService().calculateDailyRentalRate(effective));
        LocalDate start = LocalDate.now().plusDays(5);
        assertEquals(daily, ctx.getRentalService().calculateRentalCost(vehicle.getId(), start, start));
        assertEquals(total, ctx.getRentalService().calculateRentalCost(vehicle.getId(), start, start.plusDays(2)));
        var rent = ctx.getRentalService().bookRental(vehicle.getId(), 1L, start, start.plusDays(2));
        assertEquals(total, rent.getAmount());
    }

    @Test
    void newCessionUsesRecommendedBaseAndKeepsEstimateInRequest() {
        var request = ctx.getSaleToDealershipService().submitSaleProposal(1L, model.getId(),
                "T0500000000000007", "AB123CD", 0, year);
        assertEquals(new BigDecimal("14400.00"), request.getDeposit());
        Vehicle vehicle = ctx.getCatalogService().getVehicleById(request.getVehicleId()).orElseThrow();
        assertEquals(PriceMode.AUTOMATIC, vehicle.getPriceMode());
        assertEquals(new BigDecimal("20000.00"), vehicle.getPrice());
        assertEquals(new BigDecimal("14400.00"), ctx.getCatalogService().getEffectiveVehiclePrice(vehicle));
        ctx.getSaleToDealershipService().approveSaleProposal(request.getId(), 16L);
        ctx.getCatalogService().updateVehiclePricing(vehicle.getId(), new BigDecimal("10000"));
        assertEquals(new BigDecimal("7200.00"), ctx.getCatalogService().getEffectiveVehiclePrice(reload(vehicle)));
        assertEquals(new BigDecimal("14400.00"), ctx.getCatalogService().getRecommendedVehiclePrice(reload(vehicle)));
        var acquisition = ctx.getOperationQueryService().getAllSales().stream()
                .filter(s -> s.getVehicleId().equals(vehicle.getId())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("-14400.00"), acquisition.getAmount());
    }

    private Vehicle reload(Vehicle vehicle) {
        return new AppContext(ctx.getDatabaseManager()).getCatalogService().getVehicleById(vehicle.getId()).orElseThrow();
    }
}
