package it.unifi.drivehub.domain.operation;

/**
 * Tipologia di richiesta operativa.
 * Mappata sull'enum PostgreSQL request_type.
 */
public enum RequestType {
    PURCHASE,   // Il cliente richiede di acquistare un'auto versando un acconto
    SALE        // Il cliente propone di vendere la sua auto alla concessionaria
}

