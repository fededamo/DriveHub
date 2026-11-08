package it.unifi.drivehub.domain.operation;

/**
 * Tipologia di richiesta dal punto di vista della concessionaria.
 * Mappata sull'enum PostgreSQL request_type.
 */
public enum RequestType {
    SALE,       // La concessionaria vende al cliente che versa un acconto
    PURCHASE    // La concessionaria acquista il veicolo proposto dal cliente
}

