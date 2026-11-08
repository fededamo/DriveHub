package it.unifi.drivehub.domain.operation;

/** Distingue la vendita al cliente dalle altre operazioni, indipendentemente dall'importo. */
public enum SaleType {
    SALE, PURCHASE, PENALTY
}
