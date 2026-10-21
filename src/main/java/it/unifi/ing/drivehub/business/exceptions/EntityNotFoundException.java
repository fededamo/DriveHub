package it.unifi.ing.drivehub.business.exceptions;

public final class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, long id) {
        super(entity + " " + id + " was not found");
    }

    public EntityNotFoundException(String entity, Object key) {
        super(entity + " " + key + " was not found");
    }
}
