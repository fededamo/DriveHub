package it.unifi.ing.drivehub.functional;

import it.unifi.ing.drivehub.domain.rentals.Rent;
import it.unifi.ing.drivehub.domain.sales.Request;
import it.unifi.ing.drivehub.domain.sales.Sale;
import it.unifi.ing.drivehub.domain.users.BankAccount;
import it.unifi.ing.drivehub.domain.vehicles.Vehicle;
import it.unifi.ing.drivehub.domain.vehicles.VehicleState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutFunctionalTest extends FunctionalTestSupport {

    @Test
    @DisplayName("Complete purchase workflow: deposit, unlock, full payment, vehicle set to SOLD and sale recorded")
    void completePurchaseWorkflow() {
        BigDecimal deposit = new BigDecimal("4000.00");
        BankAccount initialAcc = bankAccounts.getAccountByClientId(customer.requireId());
        BigDecimal initialBal = initialAcc.balance();

        // 1. Cliente blocca il veicolo con acconto
        Request req = sales.initiatePurchase(customer.requireId(), saleVehicle.requireId(), deposit);
        assertNotNull(req);
        assertNull(req.expDate());

        Vehicle blocked = transactions.execute(u -> u.vehicles().findById(saleVehicle.requireId()).orElseThrow());
        assertEquals(VehicleState.BLOCKED, blocked.state());

        BankAccount postDepositAcc = bankAccounts.getAccountByClientId(customer.requireId());
        assertEquals(initialBal.subtract(deposit), postDepositAcc.balance());

        // 2. Venditore autorizza il pagamento completo
        sales.authorizePurchase(req.requireId(), salesman.requireId());
        Request unlocked = transactions.execute(u -> u.requests().findById(req.requireId()).orElseThrow());
        assertNotNull(unlocked.expDate());
        assertEquals(LocalDate.now().plusMonths(1), unlocked.expDate());

        // 3. Cliente salda il pagamento rimanente (40000 - 4000 = 36000)
        Sale sale = sales.completePurchase(req.requireId(), salesman.requireId());
        assertNotNull(sale);
        assertEquals(new BigDecimal("40000.00"), sale.amount());
        assertEquals(customer.requireId(), sale.clientId());
        assertEquals(salesman.requireId(), sale.salesmanId());

        // Il veicolo venduto passa a SOLD e rimane nella base dati
        Vehicle sold = transactions.execute(u -> u.vehicles().findById(saleVehicle.requireId()).orElseThrow());
        assertEquals(VehicleState.SOLD, sold.state());

        // La request è stata rimossa
        boolean requestDeleted = transactions.execute(u -> u.requests().findById(req.requireId()).isEmpty());
        assertTrue(requestDeleted);

        // Saldo finale del cliente
        BankAccount finalAcc = bankAccounts.getAccountByClientId(customer.requireId());
        assertEquals(initialBal.subtract(new BigDecimal("40000.00")), finalAcc.balance());
    }

    @Test
    @DisplayName("Cancelled purchase workflow: request deleted, vehicle restored to SALE, 20% penalty, 80% refunded")
    void cancelPurchaseWorkflowWithPenalty() {
        BigDecimal deposit = new BigDecimal("4000.00");
        BankAccount initialAcc = bankAccounts.getAccountByClientId(customer.requireId());
        BigDecimal initialBal = initialAcc.balance();

        // 1. Cliente versa acconto e blocca il veicolo
        Request req = sales.initiatePurchase(customer.requireId(), saleVehicle.requireId(), deposit);

        // 2. Annullamento acquisto con penale
        sales.cancelPurchase(req.requireId());

        // Il veicolo torna disponibile come SALE
        Vehicle restored = transactions.execute(u -> u.vehicles().findById(saleVehicle.requireId()).orElseThrow());
        assertEquals(VehicleState.SALE, restored.state());

        // La richiesta è stata cancellata
        boolean requestDeleted = transactions.execute(u -> u.requests().findById(req.requireId()).isEmpty());
        assertTrue(requestDeleted);

        // Trattenuta 20% di 4000 = 800€ come penale concessionaria, rimborso 80% = 3200€ sul conto del cliente
        // Saldo finale = initialBal - 800€
        BankAccount finalAcc = bankAccounts.getAccountByClientId(customer.requireId());
        assertEquals(initialBal.subtract(new BigDecimal("800.00")), finalAcc.balance());

        // Registrazione in Sale con vehicle=null e salesman=null (penale)
        List<Sale> allSales = sales.allSales();
        Sale defaultSale = allSales.stream()
                .filter(s -> s.clientId() == customer.requireId() && s.isDefaultFailure())
                .findFirst().orElse(null);
        assertNotNull(defaultSale);
        assertNull(defaultSale.vehicleId());
        assertNull(defaultSale.salesmanId());
        assertEquals(new BigDecimal("800.00"), defaultSale.amount());
    }

    @Test
    @DisplayName("Rental booking: debits client account and registers Rent with compound 5-day discount")
    void rentalBookingWorkflow() {
        BankAccount initialAcc = bankAccounts.getAccountByClientId(customer.requireId());
        BigDecimal initialBal = initialAcc.balance();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(4); // 5 giorni totali compresi

        Rent rent = rentals.bookRental(customer.requireId(), rentalVehicle.requireId(), start, end);
        assertNotNull(rent);

        BigDecimal expectedCost = pricing.calculateRentalPrice(rentalVehicle, 5);
        assertEquals(expectedCost, rent.amount());

        BankAccount postRentAcc = bankAccounts.getAccountByClientId(customer.requireId());
        assertEquals(initialBal.subtract(expectedCost), postRentAcc.balance());
    }

    @Test
    @DisplayName("Customer sells car: enters WAITING, salesman approves to SALE, client balance credited")
    void customerProposeAndApprove() {
        BankAccount initialAcc = bankAccounts.getAccountByClientId(customer.requireId());
        BigDecimal initialBal = initialAcc.balance();

        Vehicle proposed = sales.proposeVehicleForSale(customer.requireId(), model.requireId(),
                "DD400DD", "VIN-PROP-1", new BigDecimal("18000.00"), 2021, 55000);

        assertEquals(VehicleState.WAITING, proposed.state());

        // Venditore approva: accredita conto cliente e imposta stato veicolo a SALE
        sales.approveVehicle(proposed.requireId());

        Vehicle approved = transactions.execute(u -> u.vehicles().findById(proposed.requireId()).orElseThrow());
        assertEquals(VehicleState.SALE, approved.state());

        // Il conto del cliente è stato incrementato del valore dell'auto
        BankAccount postApprovalAcc = bankAccounts.getAccountByClientId(customer.requireId());
        assertTrue(postApprovalAcc.balance().compareTo(initialBal) > 0);
    }
}
