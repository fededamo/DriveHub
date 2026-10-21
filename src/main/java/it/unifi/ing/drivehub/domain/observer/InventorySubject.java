package it.unifi.ing.drivehub.domain.observer;

public interface InventorySubject {
    void subscribe(InventoryObserver observer);

    void unsubscribe(InventoryObserver observer);
}
