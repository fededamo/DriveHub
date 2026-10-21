package it.unifi.ing.drivehub.business.services;

import it.unifi.ing.drivehub.dao.interfaces.*;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.business.banking.IbanGenerator;
import it.unifi.ing.drivehub.domain.sales.Request;
import it.unifi.ing.drivehub.domain.sales.Sale;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import it.unifi.ing.drivehub.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SalesServiceTest {
    private UnitOfWork unit;
    private VehicleDao vehicles;
    private RequestDao requests;
    private SaleDao sales;
    private BankAccountDao bankAccounts;
    private PricingService pricingService;
    private SalesService service;

    @BeforeEach
    void setUp() {
        DaoFactory factory = mock(DaoFactory.class);
        unit = mock(UnitOfWork.class);
        vehicles = mock(VehicleDao.class);
        requests = mock(RequestDao.class);
        sales = mock(SaleDao.class);
        bankAccounts = mock(BankAccountDao.class);
        DiscountDao discounts = mock(DiscountDao.class);

        when(factory.begin()).thenReturn(unit);
        when(unit.vehicles()).thenReturn(vehicles);
        when(unit.requests()).thenReturn(requests);
        when(unit.sales()).thenReturn(sales);
        when(unit.bankAccounts()).thenReturn(bankAccounts);
        when(unit.discounts()).thenReturn(discounts);

        pricingService = new PricingService(factory);
        service = new SalesService(factory, pricingService);
    }

    @Test
    @DisplayName("Initiate purchase: debits deposit, blocks vehicle, creates request")
    void initiatePurchaseSuccess() {
        Vehicle vehicle = TestFixtures.saleVehicle(1L, "AA111AA");
        when(vehicles.findById(1L)).thenReturn(Optional.of(vehicle));

        String iban = IbanGenerator.generate();
        BankAccount account = new BankAccount(iban, 10L, new BigDecimal("10000.00"));
        when(bankAccounts.findByClientId(10L)).thenReturn(Optional.of(account));
        when(bankAccounts.findByIbanForUpdate(iban)).thenReturn(Optional.of(account));

        when(requests.save(any())).thenAnswer(inv -> {
            Request r = inv.getArgument(0);
            r.assignId(100L);
            return r;
        });

        Request created = service.initiatePurchase(10L, 1L, new BigDecimal("4000.00"));

        assertNotNull(created);
        assertEquals(new BigDecimal("6000.00"), account.balance());
        verify(vehicles).updateState(1L, VehicleState.BLOCKED);
        verify(bankAccounts).updateBalance(account.iban(), account.balance());
        verify(requests).save(any(Request.class));
        verify(unit).commit();
    }

    @Test
    @DisplayName("Cancel purchase: restores SALE, dealership retains 20% penalty, refunds 80%, records default sale")
    void cancelPurchaseAppliesPenalty() {
        Vehicle vehicle = new Vehicle(1L, 20L, "VIN1", "AA111AA", 10000, new BigDecimal("40000.00"), 2023, VehicleState.BLOCKED);
        when(vehicles.findById(1L)).thenReturn(Optional.of(vehicle));

        Request request = new Request(50L, 1L, 10L, null, new BigDecimal("1000.00"), LocalDate.now());
        when(requests.findById(50L)).thenReturn(Optional.of(request));

        String iban = IbanGenerator.generate();
        BankAccount account = new BankAccount(iban, 10L, new BigDecimal("5000.00"));
        when(bankAccounts.findByClientId(10L)).thenReturn(Optional.of(account));
        when(bankAccounts.findByIbanForUpdate(iban)).thenReturn(Optional.of(account));

        service.cancelPurchase(50L);

        verify(vehicles).updateState(1L, VehicleState.SALE);
        // 80% refund of 1000 = 800 -> new balance = 5800
        assertEquals(new BigDecimal("5800.00"), account.balance());
        verify(requests).delete(50L);
        // Default sale recorded with vehicle=null and salesman=null, amount = 20% penalty = 200.00
        verify(sales).save(argThat(sale ->
                sale.vehicleId() == null && sale.salesmanId() == null &&
                sale.clientId() == 10L && sale.amount().compareTo(new BigDecimal("200.00")) == 0));
        verify(unit).commit();
    }
}
