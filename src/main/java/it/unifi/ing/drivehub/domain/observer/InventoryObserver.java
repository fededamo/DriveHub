package it.unifi.ing.drivehub.domain.observer;

@FunctionalInterface
public interface InventoryObserver {
    void onInventoryEvent(InventoryEvent event);
}
