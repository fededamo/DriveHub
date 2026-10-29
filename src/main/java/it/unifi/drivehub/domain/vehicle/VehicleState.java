package it.unifi.drivehub.domain.vehicle;

/**
 * Stati in cui può trovarsi un veicolo nel dominio DriveHub.
 * Gestiti tramite un'enumerazione anche su PostgreSQL (vehicle_state).
 */
public enum VehicleState {
    SALE,       // Disponibile per la vendita
    RENT,       // Disponibile per il noleggio
    BLOCKED,    // Riservato per una vendita (acconto versato, operazione in corso)
    WAITING,    // Proposto dal cliente per la vendita alla concessionaria, in attesa di approvazione
    SOLD        // Venduto (mantenuto nel DB per storicizzazione)
}

